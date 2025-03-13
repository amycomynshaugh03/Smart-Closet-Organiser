package ie.setu.project.activities


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityMainBinding
import ie.setu.project.models.ClosetOrganiserModel
import timber.log.Timber.i

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var closetOrganiser = ClosetOrganiserModel()
    var app: MainApp? = null


//    private val weatherViewModel: WeatherViewModel by viewModels()
//    private val city = "London"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)

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

        app = application as MainApp


        binding.btnAdd.setOnClickListener {
            closetOrganiser.title = binding.clothingItemTitle.text.toString()
            closetOrganiser.description = binding.clothingDescription.text.toString()

            if (closetOrganiser.title.isNotEmpty()) {
                // Use clothingItems instead of closetItems to add the new clothing item
                app!!.clothingItems.create(closetOrganiser.copy())  // Adds to the correct store (clothingItems)

                i("Add Button Pressed: ${closetOrganiser.title}")

                // Log added clothing items (use clothingItems here, not closetItems)
                for (i in app!!.clothingItems.findAll().indices) {
                    i("Clothing Item[i]: ${app!!.clothingItems.findAll()[i].title}, ${app!!.clothingItems.findAll()[i].description}")
                }
                setResult(RESULT_OK)
                finish()
            } else {
                Snackbar.make(it, "Please Enter a clothing item and category", Snackbar.LENGTH_LONG).show()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_clothing_item, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_cancel -> {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }



}