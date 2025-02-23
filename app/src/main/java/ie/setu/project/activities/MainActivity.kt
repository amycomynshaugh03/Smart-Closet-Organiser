package ie.setu.project.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.github.ajalt.timberkt.BuildConfig
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.api.weather.WeatherViewModel
import ie.setu.project.databinding.ActivityMainBinding
import ie.setu.project.models.ClosetOrganiserModel
import timber.log.Timber
import timber.log.Timber.i

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var closetOrganiser = ClosetOrganiserModel()
    val closetItems = ArrayList<ClosetOrganiserModel>()

//    private val weatherViewModel: WeatherViewModel by viewModels()
//    private val city = "London"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val weatherTextView: TextView = findViewById(R.id.weatherTextView)


//        // Observe the weather data and update the UI
//        weatherViewModel.weatherData.observe(this, Observer { weatherData ->
//            weatherData?.let {
//                weatherTextView.text = "City: ${it.location.name}\n" +
//                        "Country: ${it.location.country}\n" +
//                        "Temperature: ${it.current.temp_c}°C\n" +
//                        "Condition: ${it.current.condition.text}\n" +
//                        "Humidity: ${it.current.humidity}%"
//            }
//        })

//        weatherViewModel.fetchWeather(city)

        Timber.plant(Timber.DebugTree())
        i("Welcome to your Closet Organiser!")

        // Handle the window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Code for adding items to the Closet Organiser (remains unchanged)
        binding.btnAdd.setOnClickListener {
            closetOrganiser.title = binding.clothingItemTitle.text.toString()
            closetOrganiser.description = binding.clothingDescription.text.toString()

            if (closetOrganiser.title.isNotEmpty()) {
                closetItems.add(closetOrganiser.copy())
                i("Add Button Pressed: ${closetOrganiser.title}")

                // Log added closet items
                for (i in closetItems.indices) {
                    i("Closet Item[$i] : ${closetItems[i].title}, ${closetItems[i].description}")
                }
            } else {
                Snackbar
                    .make(it, "Please Enter a clothing item and category", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }
}