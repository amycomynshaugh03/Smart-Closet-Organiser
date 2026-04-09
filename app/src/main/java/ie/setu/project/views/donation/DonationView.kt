package ie.setu.project.views.donation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ie.setu.project.ui.theme.ClosetOrganiserTheme

@AndroidEntryPoint
class DonationView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClosetOrganiserTheme {
                DonationScreen(onBack = { finish() })
            }
        }
    }
}