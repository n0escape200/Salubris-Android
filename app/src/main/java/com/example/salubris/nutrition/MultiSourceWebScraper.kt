package com.example.salubris.nutrition

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URLEncoder

class MultiSourceWebScraper(
    private val usdaApiKey: String? = null
) : WebSearchService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun searchFood(query: String): List<FoodNutritionData> =
        withContext(Dispatchers.IO) {
            Log.d("Scraper", "=== Searching for: $query ===")
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            val candidates = mutableListOf<FoodNutritionData>()

            // 1. USDA FoodData Central (requires API key)
            if (!usdaApiKey.isNullOrBlank()) {
                val usda = fetchUSDA(encodedQuery)
                candidates.addAll(usda)
                Log.d("Scraper", "USDA products: ${usda.size}")
            } else {
                Log.w("Scraper", "USDA API key not provided – skipping")
            }

            // 2. Open Food Facts (retry up to 3 times)
            val offProducts = fetchOpenFoodFacts(encodedQuery, query)
            candidates.addAll(offProducts)
            Log.d("Scraper", "OFF products: ${offProducts.size}")

            // 3. Manual web scraping via Google → DDG Lite → Brave
            val scrapedWeb = searchAndScrapeWeb(query)
            candidates.addAll(scrapedWeb)
            Log.d("Scraper", "Web scraped: ${scrapedWeb.size}")

            if (candidates.isEmpty()) {
                Log.w("Scraper", "No candidates found – returning demo data")
                return@withContext listOf(demoFood(query))
            }

            // 4. Score all candidates and return the best one
            val best = selectBestMatch(query, candidates)
            Log.d("Scraper", "Best match: ${best.name}")
            listOf(best)
        }

    // ── USDA ────────────────────────────────────────────────────────────────

    private suspend fun fetchUSDA(encodedQuery: String): List<FoodNutritionData> {
        return try {
            val apiUrl =
                "https://api.nal.usda.gov/fdc/v1/foods/search?query=$encodedQuery&pageSize=5&api_key=$usdaApiKey"
            Log.d("Scraper", "USDA URL: $apiUrl")
            val request = Request.Builder()
                .url(apiUrl)
                .header("User-Agent", randomUserAgent())
                .build()
            val jsonString = httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() ?: ""
                else {
                    Log.e("Scraper", "USDA HTTP ${response.code}")
                    throw RuntimeException("USDA HTTP ${response.code}")
                }
            }
            parseUSDA(jsonString)
        } catch (e: Exception) {
            Log.e("Scraper", "USDA failed: ${e.message}")
            emptyList()
        }
    }

    private fun parseUSDA(jsonString: String): List<FoodNutritionData> {
        return try {
            val response = json.decodeFromString<USDASearchResponse>(jsonString)
            response.foods?.map { food ->
                FoodNutritionData(
                    name = food.description ?: "USDA food",
                    calories = food.foodNutrients?.find { it.nutrientId == 1008 }?.value ?: 0.0,
                    protein = food.foodNutrients?.find { it.nutrientId == 1003 }?.value ?: 0.0,
                    carbs = food.foodNutrients?.find { it.nutrientId == 1005 }?.value ?: 0.0,
                    fat = food.foodNutrients?.find { it.nutrientId == 1004 }?.value ?: 0.0,
                    fiber = food.foodNutrients?.find { it.nutrientId == 1079 }?.value,
                    sugar = food.foodNutrients?.find { it.nutrientId == 2000 }?.value,
                    sourceUrl = "https://fdc.nal.usda.gov/fdc-app.html#/food-details/${food.fdcId}",
                    sourceName = "USDA FoodData Central"
                )
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("Scraper", "USDA parse error: ${e.message}")
            emptyList()
        }
    }

    // ── Open Food Facts ──────────────────────────────────────────────────────

    private suspend fun fetchOpenFoodFacts(
        encodedQuery: String,
        fallbackName: String
    ): List<FoodNutritionData> {
        for (attempt in 1..3) {
            val result = try {
                val url =
                    "https://world.openfoodfacts.org/cgi/search.pl?search_terms=$encodedQuery&search_simple=1&json=1&page_size=20"
                Log.d("Scraper", "OFF URL: $url")
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", randomUserAgent())
                    .build()
                val jsonString = httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) response.body?.string() ?: ""
                    else {
                        Log.e("Scraper", "OFF HTTP ${response.code}")
                        throw RuntimeException("OFF HTTP ${response.code}")
                    }
                }
                parseOffResponse(jsonString, fallbackName)
            } catch (e: Exception) {
                Log.e("Scraper", "OFF attempt $attempt failed: ${e.message}")
                emptyList()
            }
            if (result.isNotEmpty()) return result
            if (attempt < 3) delay(2000L * attempt)
        }
        return emptyList()
    }

    private fun parseOffResponse(
        jsonString: String,
        fallbackName: String
    ): List<FoodNutritionData> {
        return try {
            val searchResponse = json.decodeFromString<OffSearchResponse>(jsonString)
            val products = searchResponse.products ?: emptyList()
            products.filter { it.productName != null && it.nutriments != null }
                .map { product ->
                    FoodNutritionData(
                        name = product.productName ?: fallbackName,
                        calories = product.nutriments?.energyKcal100g ?: 0.0,
                        protein = product.nutriments?.proteins_100g ?: 0.0,
                        carbs = product.nutriments?.carbohydrates_100g ?: 0.0,
                        fat = product.nutriments?.fat_100g ?: 0.0,
                        fiber = product.nutriments?.fiber_100g,
                        sugar = product.nutriments?.sugars_100g,
                        sourceUrl = product.url ?: "https://world.openfoodfacts.org",
                        sourceName = product.productName ?: "Open Food Facts"
                    )
                }
        } catch (e: Exception) {
            Log.e("Scraper", "Parse error: ${e.message}")
            emptyList()
        }
    }

    // ── Manual web scraping (Google → DDG Lite → Brave) ────────────────────────

    private suspend fun searchAndScrapeWeb(query: String): List<FoodNutritionData> {
        val results = mutableListOf<FoodNutritionData>()
        val links = mutableListOf<String>()

        // 1. Google
        val googleLinks = searchGoogle(query)
        links.addAll(googleLinks)
        Log.d("Scraper", "Google links: ${googleLinks.size}")

        // 2. DDG Lite fallback
        if (links.isEmpty()) {
            Log.w("Scraper", "Google gave no links, trying DDG Lite")
            val ddgLinks = searchDuckDuckGoLite(query)
            links.addAll(ddgLinks)
            Log.d("Scraper", "DDG Lite links: ${ddgLinks.size}")
        }

        // 3. Brave fallback
        if (links.isEmpty()) {
            Log.w("Scraper", "DDG Lite also failed, trying Brave")
            val braveLinks = searchBrave(query)
            links.addAll(braveLinks)
            Log.d("Scraper", "Brave links: ${braveLinks.size}")
        }

        Log.d("Scraper", "Manual links to scrape: ${links.size}")

        // Scrape up to 2 pages
        for (link in links.distinct().take(2)) {
            if (results.size >= 2) break
            val data = scrapePage(link)
            if (data != null) results.add(data)
            delay(300)  // be polite to servers
        }
        return results
    }

    private fun searchGoogle(query: String): List<String> {
        return try {
            val url = "https://www.google.com/search?q=${
                URLEncoder.encode(query, "UTF-8")
            }+nutrition+per+100g&hl=en"
            Log.d("Scraper", "Google URL: $url")
            val html = fetchHtml(url)
            if (html == null) {
                Log.e("Scraper", "Google returned null HTML")
                return emptyList()
            }
            Log.d("Scraper", "Google HTML length: ${html.length}")
            Log.d("Scraper", "Google HTML snippet (first 500 chars): ${html.take(500)}")

            if (html.contains("captcha", ignoreCase = true) || html.contains(
                    "unusual traffic",
                    ignoreCase = true
                )
            ) {
                Log.w("Scraper", "Google returned a CAPTCHA or block page")
                return emptyList()
            }

            val linkRegex = Regex("""/url\?q=(https?://[^&\s"]+)""")
            val links = linkRegex.findAll(html).map { it.groupValues[1] }
                .distinct()
                .filter { !it.contains("google.com") && !it.contains("youtube.com") }
                .take(5)
                .toList()
            Log.d("Scraper", "Google extracted ${links.size} links via regex")
            links
        } catch (e: Exception) {
            Log.e("Scraper", "Google search failed: ${e.message}", e)
            emptyList()
        }
    }

    private fun searchDuckDuckGoLite(query: String): List<String> {
        return try {
            val url = "https://lite.duckduckgo.com/lite/?q=${
                URLEncoder.encode(query, "UTF-8")
            }+nutrition+per+100g"
            Log.d("Scraper", "DDG Lite URL: $url")
            val html = fetchHtml(url)
            if (html == null) {
                Log.e("Scraper", "DDG Lite returned null HTML")
                return emptyList()
            }
            Log.d("Scraper", "DDG Lite HTML length: ${html.length}")
            Log.d("Scraper", "DDG Lite HTML snippet (first 500 chars): ${html.take(500)}")

            val doc = Jsoup.parse(html)
            val links = doc.select("a.result-link")
                .mapNotNull { it.absUrl("href") }
                .filter { it.startsWith("http") }
                .take(5)
            Log.d("Scraper", "DDG Lite extracted ${links.size} links")
            links
        } catch (e: Exception) {
            Log.e("Scraper", "DDG Lite error: ${e.message}", e)
            emptyList()
        }
    }

    private fun searchBrave(query: String): List<String> {
        return try {
            val url = "https://search.brave.com/search?q=${
                URLEncoder.encode(query, "UTF-8")
            }+nutrition+per+100g"
            Log.d("Scraper", "Brave URL: $url")
            val html = fetchHtml(url)
            if (html == null) {
                Log.e("Scraper", "Brave returned null HTML")
                return emptyList()
            }
            Log.d("Scraper", "Brave HTML length: ${html.length}")
            Log.d("Scraper", "Brave HTML snippet (first 500 chars): ${html.take(500)}")

            val doc = Jsoup.parse(html)
            val links = doc.select("a.snippet-title")
                .mapNotNull { it.absUrl("href") }
                .filter { it.startsWith("http") }
                .take(5)
            Log.d("Scraper", "Brave extracted ${links.size} links")
            links
        } catch (e: Exception) {
            Log.e("Scraper", "Brave search failed: ${e.message}", e)
            emptyList()
        }
    }

    // ── HTML fetching and scraping ────────────────────────────────────────

    private fun fetchHtml(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", randomUserAgent())
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else {
                    Log.e("Scraper", "fetchHtml ${response.code} for $url")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("Scraper", "fetchHtml failed: ${e.message}")
            null
        }
    }

    private fun scrapePage(url: String): FoodNutritionData? {
        val html = fetchHtml(url) ?: return null
        val doc = Jsoup.parse(html)
        val text = doc.body().text()

        val calories = extractFirstNumber(text, patterns.calories)
        val protein = extractFirstNumber(text, patterns.protein)
        val carbs = extractFirstNumber(text, patterns.carbs)
        val fat = extractFirstNumber(text, patterns.fat)
        val fiber = extractFirstNumber(text, patterns.fiber)
        val sugar = extractFirstNumber(text, patterns.sugar)

        if (calories == null) return null

        val title = doc.title().take(80).ifBlank { "Web source" }
        return FoodNutritionData(
            name = title,
            calories = calories,
            protein = protein ?: 0.0,
            carbs = carbs ?: 0.0,
            fat = fat ?: 0.0,
            fiber = fiber,
            sugar = sugar,
            sourceUrl = url,
            sourceName = title
        )
    }

    private fun extractFirstNumber(text: String, patternList: List<Regex>): Double? {
        for (pattern in patternList) {
            val match = pattern.find(text) ?: continue
            val value = match.groupValues.getOrNull(1)?.toDoubleOrNull() ?: continue
            return value
        }
        return null
    }

    // ── Scoring ──────────────────────────────────────────────────────────────

    private fun selectBestMatch(
        query: String,
        products: List<FoodNutritionData>
    ): FoodNutritionData {
        val queryWords = query.lowercase().split("\\s+".toRegex()).toSet()
        val processedWords = setOf(
            "cooked", "fried", "grilled", "roasted", "smoked", "breaded", "battered",
            "with", "sauce", "marinade", "seasoned", "spicy", "herb", "garlic", "lemon",
            "canned", "dried", "salted", "sweet", "honey", "bbq", "chunk", "cured", "baked",
            "dipper", "slice", "chargrilled", "flavoured", "flavored", "roast", "smoke"
        )
        val rawWords = setOf(
            "raw", "fresh", "uncooked", "natural", "unprepared", "plain", "whole", "boneless",
            "skinless", "fillet"
        )

        fun score(product: FoodNutritionData): Double {
            val name = product.name.lowercase()
            val nameWords = name.split("\\s+".toRegex()).toSet()
            val common = queryWords.intersect(nameWords).size.toDouble()

            var score = 1.0 + common * 0.6
            val hasProcessedQuery = queryWords.intersect(processedWords).isNotEmpty()
            if (!hasProcessedQuery) {
                val processedCount = nameWords.intersect(processedWords).size
                score -= processedCount * 0.6
            }
            score += nameWords.intersect(rawWords).size * 1.0
            score -= nameWords.size * 0.04

            val src = product.sourceName.lowercase()
            if (src.contains("usda")) score += 3.0
            else if (src.contains("open food facts")) score += 0.5

            return score
        }

        val scored = products.map { it to score(it) }
        scored.forEach { (p, s) ->
            Log.d(
                "Scraper",
                "Score ${"%.1f".format(s)} – ${p.name} (${p.sourceName})"
            )
        }
        return scored.maxByOrNull { it.second }?.first ?: products.first()
    }

    // ── Demo fallback ────────────────────────────────────────────────────────

    private fun demoFood(query: String) = FoodNutritionData(
        name = query,
        calories = 120.0,
        protein = 23.0,
        carbs = 0.0,
        fat = 2.6,
        sourceUrl = "https://fdc.nal.usda.gov/",
        sourceName = "USDA FoodData Central (demo)"
    )

    private fun randomUserAgent() = listOf(
        "Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
    ).random()

    // ── Data models ───────────────────────────────────────────────────────────

    @kotlinx.serialization.Serializable
    data class USDASearchResponse(
        val foods: List<USDAFood>? = null
    )

    @kotlinx.serialization.Serializable
    data class USDAFood(
        val fdcId: Int? = null,
        val description: String? = null,
        val foodNutrients: List<USDANutrient>? = null
    )

    @kotlinx.serialization.Serializable
    data class USDANutrient(
        val nutrientId: Int? = null,
        val value: Double? = null
    )

    @kotlinx.serialization.Serializable
    data class OffSearchResponse(
        val products: List<OffProduct>? = null
    )

    @kotlinx.serialization.Serializable
    data class OffProduct(
        @kotlinx.serialization.SerialName("product_name") val productName: String? = null,
        val nutriments: OffNutriments? = null,
        val url: String? = null
    )

    @kotlinx.serialization.Serializable
    data class OffNutriments(
        @kotlinx.serialization.SerialName("energy-kcal_100g") val energyKcal100g: Double? = null,
        val proteins_100g: Double? = null,
        val carbohydrates_100g: Double? = null,
        val fat_100g: Double? = null,
        val fiber_100g: Double? = null,
        val sugars_100g: Double? = null
    )

    // ── Regex patterns ────────────────────────────────────────────────────────

    private object patterns {
        val calories = listOf(
            Regex("""(?i)Calories\s*[:]*\s*(\d+\.?\d*)\s*kcal"""),
            Regex("""(?i)Energy\s*[:]*\s*(\d+\.?\d*)\s*kcal"""),
            Regex("""(?i)(\d+\.?\d*)\s*calories\s*per\s*100\s*g"""),
            Regex("""(?i)(\d+\.?\d*)\s*kcal\s*per\s*100\s*g"""),
            Regex("""(?i)calories\s*(\d+\.?\d*)""")
        )
        val protein = listOf(
            Regex("""(?i)Protein\s*[:]*\s*(\d+\.?\d*)\s*g"""),
            Regex("""(?i)(\d+\.?\d*)\s*g\s*protein\s*per\s*100\s*g"""),
            Regex("""(?i)protein\s*(\d+\.?\d*)\s*g""")
        )
        val carbs = listOf(
            Regex("""(?i)(?:Total\s+)?Carb(?:s|ohydrates?)\s*[:]*\s*(\d+\.?\d*)\s*g"""),
            Regex("""(?i)(\d+\.?\d*)\s*g\s*carb(?:s|ohydrates?)\s*per\s*100\s*g"""),
            Regex("""(?i)carb(?:s|ohydrates?)\s*(\d+\.?\d*)\s*g""")
        )
        val fat = listOf(
            Regex("""(?i)Fat\s*[:]*\s*(\d+\.?\d*)\s*g"""),
            Regex("""(?i)(\d+\.?\d*)\s*g\s*fat\s*per\s*100\s*g"""),
            Regex("""(?i)fat\s*(\d+\.?\d*)\s*g""")
        )
        val fiber = listOf(
            Regex("""(?i)Fiber\s*[:]*\s*(\d+\.?\d*)\s*g"""),
            Regex("""(?i)(\d+\.?\d*)\s*g\s*fiber\s*per\s*100\s*g"""),
            Regex("""(?i)fiber\s*(\d+\.?\d*)\s*g""")
        )
        val sugar = listOf(
            Regex("""(?i)Sugars?\s*[:]*\s*(\d+\.?\d*)\s*g"""),
            Regex("""(?i)(\d+\.?\d*)\s*g\s*sugars?\s*per\s*100\s*g"""),
            Regex("""(?i)sugars?\s*(\d+\.?\d*)\s*g""")
        )
    }
}