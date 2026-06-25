package com.example.salubris.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Vocabulary {
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    fun get(): Strings = when (_currentLanguage.value) {
        "en" -> englishStrings
        "es" -> spanishStrings
        "fr" -> frenchStrings
        "de" -> germanStrings
        "ro" -> romanianStrings
        else -> englishStrings
    }

    // ---------- Strings container ----------
    class Strings {
        // General
        var appName: String by notNull()
        var close: String by notNull()
        var cancel: String by notNull()
        var confirm: String by notNull()
        var save: String by notNull()
        var update: String by notNull()
        var delete: String by notNull()
        var dismiss: String by notNull()
        var retry: String by notNull()
        var loading: String by notNull()
        var noInternet: String by notNull()
        var apiError: String by notNull()

        // Products screen
        var addProduct: String by notNull()
        var scanBarcode: String by notNull()
        var noProductsYet: String by notNull()
        var productName: String by notNull()
        var barcodeOptional: String by notNull()
        var caloriesPer100g: String by notNull()
        var proteinPer100g: String by notNull()
        var carbsPer100g: String by notNull()
        var fatsPer100g: String by notNull()
        var nutritionalValuesPer100g: String by notNull()
        var addProductTitle: String by notNull()
        var editProductTitle: String by notNull()
        var productAlreadyExists: String by notNull()
        var productNotFound: String by notNull()
        var dataFromOpenFoodFacts: String by notNull()
        var visitOpenFoodFacts: String by notNull()
        var saveProduct: String by notNull()
        var updateProduct: String by notNull()

        // Meals screen
        var addMeal: String by notNull()
        var handsFree: String by notNull()
        var noMealsYet: String by notNull()
        var addMealTitle: String by notNull()
        var handsFreeMealTitle: String by notNull()
        var mealName: String by notNull()
        var products: String by notNull()
        var selectProduct: String by notNull()
        var quantityGrams: String by notNull()
        var addProductButton: String by notNull()
        var saveMeal: String by notNull()
        var addedProducts: String by notNull()
        var draftProducts: String by notNull()
        var resolve: String by notNull()
        var removeDraft: String by notNull()
        var noProductsAdded: String by notNull()
        var noDrafts: String by notNull()
        var resolveDraftTitle: String by notNull()
        var mapExisting: String by notNull()
        var createNew: String by notNull()
        var newProductName: String by notNull()
        var calories: String by notNull()
        var protein: String by notNull()
        var carbs: String by notNull()
        var fats: String by notNull()
        var totals: String by notNull()
        var per100g: String by notNull()
        var ingredients: String by notNull()
        var ingredientsPlural: String by notNull()
        var deleteMeal: String by notNull()
        var remove: String by notNull()
        var quantityLabel: String by notNull()

        // Voice recognition
        var ready: String by notNull()
        var microphonePermissionDenied: String by notNull()
        var listening: String by notNull()
        var speaking: String by notNull()
        var processing: String by notNull()
        var notListening: String by notNull()
        var commandNotRecognized: String by notNull()
        var noProductName: String by notNull()
        var pleaseSayQuantity: String by notNull()
        var addedProduct: String by notNull()
        var productNotFoundAddedDraft: String by notNull()
        var resolvedProduct: String by notNull()
        var couldNotUnderstand: String by notNull()
        var noCommandRecognized: String by notNull()
        var recognizerBusy: String by notNull()
        var errorWithCode: String by notNull()
        var resolved: String by notNull()
        var drafts: String by notNull()
        var pleaseEnterMealName: String by notNull()
        var addAtLeastOneProduct: String by notNull()
        var warning: String by notNull()
        var link: String by notNull()
        var speechPrompt: String by notNull()

        // Chat
        var aiAssistant: String by notNull()
        var aiAssistantLoading: String by notNull()
        var failedToLoadModel: String by notNull()
        var unknownError: String by notNull()
        var copy: String by notNull()
        var source: String by notNull()
        var askMeAnything: String by notNull()
        var send: String by notNull()
        var chatGreeting: String by notNull()
        var chatHelpGuide: String by notNull()
        var chatPromptForQuantities: String by notNull()
        var chatInvalidQuantityFormat: String by notNull()
        var chatNoDataForIngredients: String by notNull()
        var chatNutritionAssistantFallback: String by notNull()
        var chatErrorTemplate: String by notNull()
        var noReliableData: String by notNull()
        var addToMyProducts: String by notNull()
        var valuesPer100g: String by notNull()
        var openSource: String by notNull()
        var addToProducts: String by notNull()
        var copyMessage: String by notNull()
        var failedToGenerateResponse: String by notNull()
        var defaultFallbackResponse: String by notNull()

        // Barcode scanning
        var barcodeScanner: String by notNull()
        var lookingUpProduct: String by notNull()
        var noInternetConnection: String by notNull()
        var productAlreadyExistsSnack: String by notNull()
        var productNotFoundSnack: String by notNull()
        var apiErrorSnack: String by notNull()

        // Navigation / Footer
        var home: String by notNull()
        var tracking: String by notNull()
        var productsNav: String by notNull()
        var mealsNav: String by notNull()
        var settingsNav: String by notNull()
        var menu: String by notNull()
        var customizeFooter: String by notNull()
        var pages: String by notNull()
        var maximumPagesSelected: String by notNull()
        var quickActions: String by notNull()
        var selected: String by notNull()
        var notSelected: String by notNull()

        // Settings
        var settings: String by notNull()
        var profileSetup: String by notNull()
        var language: String by notNull()
        var selectLanguage: String by notNull()
        var english: String by notNull()
        var spanish: String by notNull()
        var french: String by notNull()
        var german: String by notNull()
        var romanian: String by notNull()

        // Home screen
        var welcomeBack: String by notNull()
        var userDefault: String by notNull()
        var userIcon: String by notNull()
        var steps: String by notNull()
        var stepsIcon: String by notNull()
        var remainingSteps: String by notNull()
        var stepSensorNotAvailable: String by notNull()
        var water: String by notNull()
        var waterIcon: String by notNull()
        var mlRemaining: String by notNull()
        var todaysIntake: String by notNull()
        var caloriesIcon: String by notNull()
        var noDataThisWeek: String by notNull()
        var analytics: String by notNull()
        var analyticsIcon: String by notNull()
        var weeklyCaloricIntake: String by notNull()
        var refreshIcon: String by notNull()
        var caloriesLeft: String by notNull()
        var exceededGoal: String by notNull()
        var moreCaloriesNeeded: String by notNull()
        var goalAchieved: String by notNull()
        var caloriesRemaining: String by notNull()
        var exceededBy: String by notNull()
        var perfect: String by notNull()
        var progressPercent: String by notNull()

        // Profile Setup tab
        var yourNutritionalProfile: String by notNull()
        var profileComplete: String by notNull()
        var personalInfo: String by notNull()
        var activityAndGoal: String by notNull()
        var ageLabel: String by notNull()
        var sexLabel: String by notNull()
        var heightLabel: String by notNull()
        var weightLabel: String by notNull()
        var activityLabel: String by notNull()
        var goalLabel: String by notNull()
        var years: String by notNull()
        var cm: String by notNull()
        var kg: String by notNull()
        var recommendedDailyCalories: String by notNull()
        var disclaimerText: String by notNull()
        var noProfileDataFound: String by notNull()
        var updateProfile: String by notNull()
        var setupProfile: String by notNull()

        // Activity level strings
        var sedentary: String by notNull()
        var lightExercise: String by notNull()
        var moderateExercise: String by notNull()
        var active: String by notNull()
        var veryActive: String by notNull()

        // Goal strings
        var extremeLoss: String by notNull()
        var moderateLoss: String by notNull()
        var maintain: String by notNull()
        var moderateGain: String by notNull()
        var extremeGain: String by notNull()

        // Macros screen
        var macros: String by notNull()
        var macrosFor: String by notNull()
        var kcalShort: String by notNull()
        var proteinShort: String by notNull()
        var carbsShort: String by notNull()
        var fatsShort: String by notNull()
        var amountLabel: String by notNull()
        var quantityLabelMeal: String by notNull()
        var ok: String by notNull()
        var afterAddingDeficit: String by notNull()
        var afterAddingExceeded: String by notNull()
        var afterAddingSurplus: String by notNull()
        var afterAddingGoalMet: String by notNull()
        var remainingToMaintain: String by notNull()
        var exceedMaintenance: String by notNull()
        var perfectMaintain: String by notNull()
        var effectOnGoal: String by notNull()
        var noGoalSet: String by notNull()
        var recommendSetGoal: String by notNull()
        var previewMacroIntake: String by notNull()
        var pleaseSelectProduct: String by notNull()
        var addToToday: String by notNull()
        var selectMeal: String by notNull()
        var mealContains: String by notNull()
        var totalMealWeight: String by notNull()
        var quantityConsumed: String by notNull()
        var servingFactor: String by notNull()
        var macroPreview: String by notNull()
        var pleaseSelectMeal: String by notNull()
        var amountGrams: String by notNull()

        // Steps screen
        var stepTracker: String by notNull()
        var stepsCount: String by notNull()
        var goalSteps: String by notNull()
        var percentCompleted: String by notNull()
        var insights: String by notNull()
        var lowActivity: String by notNull()
        var goodProgress: String by notNull()
        var greatJob: String by notNull()
        var excellentGoalAchieved: String by notNull()
        var remainingStepsLabel: String by notNull()
        var history: String by notNull()
        var historyPlaceholder: String by notNull()

        // UserDataSetupModal
        var basicInformation: String by notNull()
        var yourName: String by notNull()
        var ageYears: String by notNull()
        var sex: String by notNull()
        var male: String by notNull()
        var female: String by notNull()
        var heightCm: String by notNull()
        var weightKg: String by notNull()
        var activityLevelTitle: String by notNull()
        var howOftenExercise: String by notNull()
        var yourGoal: String by notNull()
        var baseMaintenance: String by notNull()
        var completePreviousSteps: String by notNull()
        var caloriesPerDayWeeklyChange: String by notNull()
        var goBackFillInfo: String by notNull()
        var back: String by notNull()
        var next: String by notNull()
        var calculate: String by notNull()

        // Water screen
        var totalWaterIntake: String by notNull()
        var mlValue: String by notNull()
        var editCupSizes: String by notNull()
        var recommendedIntake: String by notNull()
        var percentOfDailyGoal: String by notNull()
        var todaysHistory: String by notNull()
        var editCupSizesTitle: String by notNull()
        var cupLabel: String by notNull()
        var setRecommendedIntake: String by notNull()
        var recommendedMl: String by notNull()
        var notifications: String by notNull()

        // Utils / ProductNutritionLabel
        var nutritionPer100g: String by notNull()

        // MainActivity / Permissions
        var permissionsRequiredTitle: String by notNull()
        var permissionsRequiredDescription: String by notNull()
        var grantPermissions: String by notNull()

        // Camera / Barcode scanner
        var cameraPermissionNeeded: String by notNull()
        var grantPermission: String by notNull()
        var cameraPermissionDenied: String by notNull()
        var scanningProgress: String by notNull()
        var startingCamera: String by notNull()
        var errorPrefix: String by notNull()
        var positionBarcode: String by notNull()

        // Added: meals (for Macros screen button)
        var meals: String by notNull()

        // NEW: Health Report
        var healthReport: String by notNull()

        // NEW: Meal label for tracked entries
        var mealLabel: String by notNull()
    }

    // ------------------------------------------------------------------
    // ENGLISH
    // ------------------------------------------------------------------
    private val englishStrings: Strings = Strings().apply {
        appName = "Salubris"
        close = "Close"
        cancel = "Cancel"
        confirm = "Confirm"
        save = "Save"
        update = "Update"
        delete = "Delete"
        dismiss = "Dismiss"
        retry = "Retry"
        loading = "Loading..."
        noInternet = "No internet connection"
        apiError = "API error – please try again"

        addProduct = "Add Product"
        scanBarcode = "Scan Barcode"
        noProductsYet = "No products yet"
        productName = "Product name"
        barcodeOptional = "Barcode (optional)"
        caloriesPer100g = "Calories (kcal)"
        proteinPer100g = "Protein (g)"
        carbsPer100g = "Carbs (g)"
        fatsPer100g = "Fats (g)"
        nutritionalValuesPer100g = "Nutritional values are per 100g"
        addProductTitle = "Add a product"
        editProductTitle = "Edit Product"
        productAlreadyExists = "Product already exists"
        productNotFound = "Product not found"
        dataFromOpenFoodFacts =
            "Data sourced from Open Food Facts. It may be outdated or incomplete."
        visitOpenFoodFacts = "Visit Open Food Facts"
        saveProduct = "Save Product"
        updateProduct = "Update Product"

        addMeal = "Add Meal"
        handsFree = "Hands‑free"
        noMealsYet = "No meals yet"
        addMealTitle = "Add a meal"
        handsFreeMealTitle = "Hands‑free Meal"
        mealName = "Meal name"
        products = "Products"
        selectProduct = "Select a product"
        quantityGrams = "Quantity (g)"
        addProductButton = "ADD PRODUCT"
        saveMeal = "Save Meal"
        addedProducts = "Added products"
        draftProducts = "Draft products"
        resolve = "Resolve"
        removeDraft = "Remove draft"
        noProductsAdded = "No products added yet"
        noDrafts = "No drafts"
        resolveDraftTitle = "Resolve draft: "
        mapExisting = "Map to existing product"
        createNew = "Create new product"
        newProductName = "Product name"
        calories = "Calories"
        protein = "Protein"
        carbs = "Carbs"
        fats = "Fats"
        totals = "Totals:"
        per100g = "per 100g"
        ingredients = "ingredient"
        ingredientsPlural = "ingredients"
        deleteMeal = "Delete meal"
        remove = "Remove"
        quantityLabel = "Quantity:"

        ready = "Ready"
        microphonePermissionDenied = "Microphone permission denied"
        listening = "Listening..."
        speaking = "Speaking..."
        processing = "Processing..."
        notListening = "Not listening"
        commandNotRecognized = "Command not recognized (start with 'add')"
        noProductName = "No product name"
        pleaseSayQuantity = "Please say quantity for {product} (e.g., '100 grams')"
        addedProduct = "Added {quantity}g of {product}"
        productNotFoundAddedDraft = "{product} not found – added as draft"
        resolvedProduct = "Resolved: {product}"
        couldNotUnderstand = "Could not understand"
        noCommandRecognized = "No command recognized"
        recognizerBusy = "Recognizer busy"
        errorWithCode = "Error: {error}"
        resolved = "Resolved"
        drafts = "Drafts"
        pleaseEnterMealName = "Please enter a meal name"
        addAtLeastOneProduct = "Add at least one product or draft"
        warning = "Warning"
        link = "Link"
        speechPrompt = "Say 'add product quantity'"

        aiAssistant = "AI Assistant"
        aiAssistantLoading = "Loading AI Assistant..."
        failedToLoadModel = "Failed to load model"
        unknownError = "Unknown error"
        copy = "Copy"
        source = "Source"
        askMeAnything = "Ask me anything..."
        send = "Send"
        chatGreeting =
            "👋 Hi! I'm your Salubris assistant. I search trusted nutrition sources so you don't have to. Ask me about macros, calories, or meal totals!\n\nType 'Help' for a guide and examples."
        chatHelpGuide =
            "🤖 **Salubris Nutrition Assistant**\n\nI search trusted sources (USDA, Open Food Facts, and the web) to give you accurate macronutrient data per 100 grams. I can also calculate meal totals when you provide quantities.\n\nHere's what you can ask me:\n\n• **Single food macros**\n   - \"macros for chicken breast per 100g\"\n   - \"calories in avocado 100 grams\"\n   - \"protein in salmon per 100g\"\n\n• **Meal calculation** (add quantities)\n   - \"200g chicken breast, 150g rice, 100g broccoli\"\n   - \"300g steak, 200g potatoes, 50g butter\"\n\n• **General nutrition & health questions**\n   - \"How much protein do I need per day?\"\n   - \"What are healthy sources of fat?\"\n   - \"Is intermittent fasting effective?\"\n\nJust type your question normally – I'll detect what you need and search the web for verified data. I never invent numbers."
        chatPromptForQuantities =
            "Please provide quantities for each food so I can calculate the meal macros.\n\nExample:\n200g chicken breast\n150g rice"
        chatInvalidQuantityFormat =
            "I couldn't understand the quantities. Please use the format: 200g chicken breast."
        chatNoDataForIngredients = "Could not find nutrition data for those ingredients."
        chatNutritionAssistantFallback =
            "🤔 I'm a nutrition assistant. I can help with food macros, meal calculations, and general nutrition questions.\n\nType 'Help' to see what I can do!"
        chatErrorTemplate = "❌ Something went wrong: %s"
        noReliableData = "🤔 I couldn't find reliable nutrition data for that food."
        addToMyProducts = "Add to My Products"
        valuesPer100g = "⚠️ Values are per 100 grams."
        openSource = "Open source"
        addToProducts = "Add to products"
        copyMessage = "Copy message"
        failedToGenerateResponse = "🤔 I couldn't generate a response."
        defaultFallbackResponse =
            "I'm sorry, I couldn't process that request. Please try asking about a specific food or type 'Help' for examples."

        barcodeScanner = "Barcode scanner"
        lookingUpProduct = "Looking up product..."
        noInternetConnection = "No internet connection"
        productAlreadyExistsSnack = "Product already exists"
        productNotFoundSnack = "Product not found in Open Food Facts"
        apiErrorSnack = "API error – please try again"

        home = "Home"
        tracking = "Tracking"
        productsNav = "Products"
        mealsNav = "Meals"
        settingsNav = "Settings"
        menu = "Menu"
        customizeFooter = "Customize Footer"
        pages = "Pages"
        maximumPagesSelected = "Maximum 4 pages selected"
        quickActions = "Quick Actions"
        selected = "Selected"
        notSelected = "Not selected"

        settings = "Settings"
        profileSetup = "Profile Setup"
        language = "Language"
        selectLanguage = "Select your language"
        english = "English"
        spanish = "Spanish"
        french = "French"
        german = "German"
        romanian = "Romanian"

        welcomeBack = "Welcome back"
        userDefault = "User"
        userIcon = "User icon"
        steps = "Steps"
        stepsIcon = "Steps"
        remainingSteps = "steps remaining"
        stepSensorNotAvailable = "Step sensor not available"
        water = "Water"
        waterIcon = "Water"
        mlRemaining = "ml remaining"
        todaysIntake = "Today's Intake"
        caloriesIcon = "Calories"
        noDataThisWeek = "No data this week"
        analytics = "Analytics"
        analyticsIcon = "Analytics"
        weeklyCaloricIntake = "Weekly caloric intake"
        refreshIcon = "Refresh"
        caloriesLeft = "🎯 %d calories left"
        exceededGoal = "⚠️ You've exceeded your goal"
        moreCaloriesNeeded = "💪 %d more calories needed"
        goalAchieved = "✅ Goal achieved!"
        caloriesRemaining = "%d calories remaining"
        exceededBy = "Exceeded by %d calories"
        perfect = "Perfect!"
        progressPercent = "Progress: %.1f%%"

        yourNutritionalProfile = "Your nutritional profile"
        profileComplete = "Profile complete"
        personalInfo = "Personal Info"
        activityAndGoal = "Activity & Goal"
        ageLabel = "Age"
        sexLabel = "Sex"
        heightLabel = "Height"
        weightLabel = "Weight"
        activityLabel = "Activity"
        goalLabel = "Goal"
        years = "years"
        cm = "cm"
        kg = "kg"
        recommendedDailyCalories = "Recommended daily calories: %d kcal"
        disclaimerText =
            "⚠️ Disclaimer: These are only recommendations. Consult a healthcare professional before making significant dietary changes."
        noProfileDataFound =
            "No profile data found. Click below to set up your age, sex, height, weight, goals and activity level."
        updateProfile = "Update profile"
        setupProfile = "Set up profile"

        sedentary = "Sedentary (little or no exercise)"
        lightExercise = "Light exercise (1-3 days/week)"
        moderateExercise = "Moderate exercise (3-5 days/week)"
        active = "Active (6-7 days/week)"
        veryActive = "Very active (hard daily exercise)"

        extremeLoss = "Extreme weight loss (1000 kcal deficit)"
        moderateLoss = "Moderate weight loss (500 kcal deficit)"
        maintain = "Maintain weight"
        moderateGain = "Moderate weight gain (500 kcal surplus)"
        extremeGain = "Extreme weight gain (1000 kcal surplus)"

        macros = "Macros"
        macrosFor = "Macros for "
        kcalShort = "Kcal"
        proteinShort = "Protein"
        carbsShort = "Carbs"
        fatsShort = "Fats"
        amountLabel = "Amount: %.1fg"
        quantityLabelMeal = "Quantity: %.1fg"
        ok = "OK"
        afterAddingDeficit = "After adding: 🎯 %d kcal left to stay in deficit"
        afterAddingExceeded = "⚠️ After adding: You will exceed your calorie goal"
        afterAddingSurplus = "After adding: 💪 %d more kcal needed to reach surplus"
        afterAddingGoalMet = "✅ After adding: You will meet or exceed your surplus goal"
        remainingToMaintain = "%d kcal remaining to maintain weight"
        exceedMaintenance = "Will exceed maintenance by %d kcal"
        perfectMaintain = "Perfect! You'll exactly hit your maintenance goal"
        effectOnGoal = "Effect on your daily goal"
        noGoalSet = "No goal set"
        recommendSetGoal =
            "We recommend that you set a main goal inside the settings section for a better experience"
        previewMacroIntake = "Preview macro intake"
        pleaseSelectProduct = "Please select a product to continue"
        addToToday = "Add to today"
        selectMeal = "Select a meal"
        mealContains = "Meal contains:"
        totalMealWeight = "Total meal weight: %sg"
        quantityConsumed = "Quantity consumed (g)"
        servingFactor = "Serving factor: %sx"
        macroPreview = "Macro preview"
        pleaseSelectMeal = "Please select a meal"
        amountGrams = "Amount (g)"

        stepTracker = "Step Tracker"
        stepsCount = "%d steps"
        goalSteps = "Goal: %d steps"
        percentCompleted = "%d%% completed"
        insights = "Insights"
        lowActivity = "Low activity — try taking a short walk."
        goodProgress = "Good progress — you're getting active."
        greatJob = "Great job — almost at your goal!"
        excellentGoalAchieved = "Excellent — goal achieved 🎉"
        remainingStepsLabel = "Remaining: %d steps"
        history = "History"
        historyPlaceholder = "Daily tracking history will appear here once persistence is added."

        basicInformation = "Basic information"
        yourName = "Your name"
        ageYears = "Age (years)"
        sex = "Sex"
        male = "Male"
        female = "Female"
        heightCm = "Height (cm)"
        weightKg = "Weight (kg)"
        activityLevelTitle = "Activity level"
        howOftenExercise = "How often do you exercise?"
        yourGoal = "Your goal"
        baseMaintenance = "Base maintenance: %d kcal/day"
        completePreviousSteps = "Complete previous steps first"
        caloriesPerDayWeeklyChange = "%d kcal/day · %s"
        goBackFillInfo = "Please go back and ensure all information is filled."
        back = "Back"
        next = "Next"
        calculate = "Calculate"

        totalWaterIntake = "Total Water Intake"
        mlValue = "%d ml"
        editCupSizes = "Edit cup sizes"
        recommendedIntake = "Recommended Intake"
        percentOfDailyGoal = "%d%% of daily goal"
        todaysHistory = "Today's History"
        editCupSizesTitle = "Edit Cup Sizes (ml)"
        cupLabel = "Cup %d"
        setRecommendedIntake = "Set Recommended Daily Intake (ml)"
        recommendedMl = "Recommended ml"
        notifications = "Notifications"

        nutritionPer100g = "Nutrition per 100g"

        permissionsRequiredTitle = "Permissions Required"
        permissionsRequiredDescription =
            "This app needs activity recognition and health service permissions to count your steps."
        grantPermissions = "Grant Permissions"

        cameraPermissionNeeded = "Camera permission is needed to scan barcodes"
        grantPermission = "Grant Permission"
        cameraPermissionDenied = "Camera permission denied"
        scanningProgress = "Scanning... (%d/3)"
        startingCamera = "Starting camera..."
        errorPrefix = "Error: "
        positionBarcode =
            "Position barcode inside the frame\nScanning multiple times for accuracy..."

        meals = "Meals"

        // NEW: Health Report (English)
        healthReport = "Health Report"

        // NEW: Meal label
        mealLabel = "Meal"
    }

    // ------------------------------------------------------------------
    // SPANISH
    // ------------------------------------------------------------------
    private val spanishStrings: Strings = Strings().apply {
        // ... copy all Spanish strings as before, plus add:
        mealLabel = "Comida"
    }

    // ------------------------------------------------------------------
    // FRENCH
    // ------------------------------------------------------------------
    private val frenchStrings: Strings = Strings().apply {
        // ... copy all French strings as before, plus add:
        mealLabel = "Repas"
    }

    // ------------------------------------------------------------------
    // GERMAN
    // ------------------------------------------------------------------
    private val germanStrings: Strings = Strings().apply {
        // ... copy all German strings as before, plus add:
        mealLabel = "Mahlzeit"
    }

    // ------------------------------------------------------------------
    // ROMANIAN
    // ------------------------------------------------------------------
    private val romanianStrings: Strings = Strings().apply {
        // ... copy all Romanian strings as before, plus add:
        mealLabel = "Masă"
    }

    // Helper delegate (unchanged)
    private fun <T> notNull() = object : kotlin.properties.ReadWriteProperty<Any?, T> {
        private var value: T? = null
        override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T {
            return value ?: throw IllegalStateException("Property ${property.name} not initialized")
        }

        override fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: T) {
            this.value = value
        }
    }
}