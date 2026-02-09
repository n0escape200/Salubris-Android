import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.snapshotFlow
import androidx.navigation.NavController
import com.example.salubris.ui.screens.pages.Home
import com.example.salubris.ui.screens.pages.Macros
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeTabsScreen(
    navController: NavController,
    pagerState: PagerState,               // Pass from parent
    currentPage: MutableState<String>? = null,
    modifier: Modifier = Modifier
) {
    // Update currentPage whenever pager scrolls
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collectLatest { pageIndex ->
            currentPage?.value = when (pageIndex) {
                0 -> "Home"
                1 -> "Tracking"
                else -> "Home"
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> Home(navController)
            1 -> Macros(navController)
        }
    }
}
