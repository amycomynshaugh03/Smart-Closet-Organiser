package ie.setu.project.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.databinding.ActivityMainBinding
import ie.setu.project.models.ClosetOrganiserModel
import timber.log.Timber
import timber.log.Timber.i
import java.nio.file.Files.copy

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var closetOrganiser = ClosetOrganiserModel()
    val closetItems = ArrayList<ClosetOrganiserModel>()

    //private var counter = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Timber.plant(Timber.DebugTree())
        i("Welcome to your Closet Organiser!")


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnAdd.setOnClickListener {
            closetOrganiser.title = binding.clothingItemTitle.text.toString()
            closetOrganiser.description = binding.clothingDescription.text.toString()

            if (closetOrganiser.title.isNotEmpty()) {
                closetItems.add(closetOrganiser.copy())
                i("Add Button Pressed: ${closetOrganiser.title}")
                i("Closet Item added to Array: ${closetOrganiser.title} with the category: ${closetOrganiser.description}")
            } else {
                Snackbar
                    .make(it, "Please Enter a clothing item and category", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }
}




