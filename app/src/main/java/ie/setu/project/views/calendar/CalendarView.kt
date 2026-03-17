package ie.setu.project.views.calendar

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ie.setu.project.ui.theme.ClosetOrganiserTheme

@AndroidEntryPoint
class CalendarView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClosetOrganiserTheme {
                CalendarScreen(onBack = { finish() })
            }
        }
    }
}