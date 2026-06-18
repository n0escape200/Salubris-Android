import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.salubris.ui.screens.pages.Home
import com.example.salubris.ui.screens.pages.Meals
import com.example.salubris.ui.screens.pages.Products
import com.example.salubris.ui.screens.pages.Settings
import com.example.salubris.ui.screens.pages.Tracking
import com.example.salubris.utils.Vocabulary

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeTabsScreen(
    favorites: List<String>,
    pagerState: PagerState,
    currentPage: MutableState<String>,
    overridePage: String?,
    onCloseOverride: () -> Unit,
    onNavigateToPage: (String) -> Unit,  // called when user selects a page from FAB
    modifier: Modifier = Modifier
) {
    val screenMap = mapOf(
        "Home" to @Composable { Home() },
        "Tracking" to @Composable { Tracking() },
        "Products" to @Composable { Products() },
        "Meals" to @Composable { Meals() },
        "Settings" to @Composable { Settings() }
    )

    if (overridePage != null) {
        // Show override page with a close button
        Box(modifier = modifier.fillMaxSize()) {
            screenMap[overridePage]?.invoke() ?: Home()
            // Close button to exit override mode
            IconButton(
                onClick = onCloseOverride,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = Vocabulary.get().close,
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    } else {
        // Normal pager with only favorites
        HorizontalPager(
            state = pagerState,
            modifier = modifier.fillMaxSize()
        ) { page ->
            val pageName = favorites[page]
            screenMap[pageName]?.invoke() ?: Home()
        }
    }

    // Update currentPage only when not in override
    LaunchedEffect(pagerState.currentPage, overridePage) {
        if (overridePage == null) {
            currentPage.value = favorites.getOrNull(pagerState.currentPage) ?: "Home"
        } else {
            currentPage.value = overridePage
        }
    }
}