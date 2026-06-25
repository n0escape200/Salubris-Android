import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.salubris.ui.screens.pages.Home
import com.example.salubris.ui.screens.pages.Meals
import com.example.salubris.ui.screens.pages.Products
import com.example.salubris.ui.screens.pages.Settings
import com.example.salubris.ui.screens.pages.Tracking
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.utils.Vocabulary

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeTabsScreen(
    favorites: List<String>,
    pagerState: PagerState,
    currentPage: MutableState<String>,
    overridePage: String?,
    onCloseOverride: () -> Unit,
    onNavigateToPage: (String) -> Unit,
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
        Column(modifier = modifier.fillMaxSize()) {
            // Minimal top bar with close button – matches ContainerBackground
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(ContainerBackground),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onCloseOverride,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = Vocabulary.get().close,
                        tint = Color.White
                    )
                }
            }
            // Page content fills remaining space
            Box(modifier = Modifier.fillMaxSize()) {
                screenMap[overridePage]?.invoke() ?: Home()
            }
        }
    } else {
        HorizontalPager(
            state = pagerState,
            modifier = modifier.fillMaxSize()
        ) { page ->
            val pageName = favorites[page]
            screenMap[pageName]?.invoke() ?: Home()
        }
    }

    LaunchedEffect(pagerState.currentPage, overridePage) {
        if (overridePage == null) {
            currentPage.value = favorites.getOrNull(pagerState.currentPage) ?: "Home"
        } else {
            currentPage.value = overridePage
        }
    }
}