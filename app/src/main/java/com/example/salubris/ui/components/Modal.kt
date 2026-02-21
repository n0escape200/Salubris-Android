import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.salubris.ui.theme.ContainerBackground
import com.example.salubris.ui.theme.cancelColor
import com.example.salubris.ui.theme.submitColor

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Modal(
    open: Boolean,
    onClose: () -> Unit,
    onSubmit: (() -> Unit),
    title: String,
    content: @Composable (ColumnScope.() -> Unit),
) {
    // Animated visibility for modal
    AnimatedVisibility(
        visible = open, enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f, animationSpec = tween(300)
        ), exit = fadeOut(animationSpec = tween(200)) + scaleOut(
            targetScale = 0.8f, animationSpec = tween(200)
        )
    ) {
        // Background overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color(84, 84, 84, 255), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Modal container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ContainerBackground, RoundedCornerShape(10.dp))
                    .clickable(enabled = false) {} // prevent closing when clicking inside
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        title, color = Color.White, fontWeight = FontWeight.W600, fontSize = 32.sp
                    )

                    // Scrollable content area
                    Column(
                        modifier = Modifier
                            .weight(1f) // Takes remaining space
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Custom user content
                        content()
                    }


                    // Sticky bottom buttons
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                ContainerBackground, RoundedCornerShape(0f, 0f, 20f, 20f)
                            ) // Same color as modal background
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = onClose,
                                colors = ButtonDefaults.buttonColors(cancelColor),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.defaultMinSize(minWidth = 130.dp)
                            ) {
                                Text("Close")
                            }
                            Button(
                                onClick = {
                                    onSubmit();
                                },
                                colors = ButtonDefaults.buttonColors(submitColor),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.defaultMinSize(minWidth = 130.dp)
                            ) {
                                Text("Submit")
                            }

                        }
                    }
                }
            }
        }
    }
}