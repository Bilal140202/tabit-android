package app.tabit.tracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight as GlanceFontWeight

class TabitGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }
}

@Composable
private fun WidgetContent() {
    GlanceTheme {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(8.dp).background(GlanceTheme.colors.surface),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(text = "Tabit", style = TextStyle(color = GlanceTheme.colors.onSurface, fontWeight = GlanceFontWeight.Bold))
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(text = "Tap to open", style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant))
        }
    }
}

class TabitGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TabitGlanceWidget()
}
