package ie.setu.project.activities


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import ie.setu.project.R
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityMainBinding
import ie.setu.project.helpers.showImagePicker
import ie.setu.project.models.ClosetOrganiserModel
import timber.log.Timber.i
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    var closetOrganiser = ClosetOrganiserModel()
    var app: MainApp? = null
    var edit = false



//    private val weatherViewModel: WeatherViewModel by viewModels()
//    private val city = "London"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)

        app = application as MainApp



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

        val seasonSpinner: Spinner = findViewById(R.id.clothingSeason)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.seasons_array,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        seasonSpinner.adapter = adapter



        if (intent.hasExtra("closet_item_edit")) {
            edit = true
            closetOrganiser = intent.getParcelableExtra("closet_item_edit")!!
            binding.clothingItemTitle.setText(closetOrganiser.title)
            binding.clothingDescription.setText(closetOrganiser.description)
            binding.clothingColour.setText(closetOrganiser.colourPattern)
            binding.clothingSize.setText(closetOrganiser.size)

            val seasonPosition = adapter.getPosition(closetOrganiser.season)
            seasonSpinner.setSelection(seasonPosition)

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = sdf.format(closetOrganiser.lastWorn)

            binding.lastWorn.setText(formattedDate)
            binding.btnAdd.text = getString(R.string.save_clothing_item)
        }

        binding.lastWorn.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Last Worn Date")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selection

                closetOrganiser.lastWorn = calendar.time

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val selectedDate = sdf.format(closetOrganiser.lastWorn)

                binding.lastWorn.setText(selectedDate)

                datePicker.dismiss()
            }

            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }


        binding.btnAdd.setOnClickListener {
            closetOrganiser.title = binding.clothingItemTitle.text.toString()
            closetOrganiser.description = binding.clothingDescription.text.toString()
            closetOrganiser.colourPattern = binding.clothingColour.text.toString()
            closetOrganiser.size = binding.clothingSize.text.toString()
            closetOrganiser.season = seasonSpinner.selectedItem.toString()
            //closetOrganiser.lastWorn = binding.lastWorn.text.toString()


            if (closetOrganiser.title.isNotEmpty()) {
                if (edit) {
                    app!!.clothingItems.update(closetOrganiser.copy())
                    i("Update Button Pressed: ${closetOrganiser.title}")
                } else if (closetOrganiser.id == 0L) {
                    app!!.clothingItems.create(closetOrganiser.copy())
                    i("Add Button Pressed: ${closetOrganiser.title}")

                    for (i in app!!.clothingItems.findAll().indices) {
                        i("Clothing Item[i]: ${app!!.clothingItems.findAll()[i].title}, ${app!!.clothingItems.findAll()[i].description}")
                    }
                }
                setResult(RESULT_OK)
                finish()
            } else {
                Snackbar.make(
                    it,
                    getString(R.string.please_enter_missing_item),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
        binding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }
        registerImagePickerCallback()

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

    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    RESULT_OK -> {
                        if (result.data != null) {
                            result.data?.data?.let { uri ->
                                i("Got Result: $uri")
                                closetOrganiser.image = uri
                                Picasso.get()
                                    .load(uri)
                                    .into(binding.clothingImage)
                            }
                        }
                        if (intent.hasExtra("closet_item_edit")) {
                            Picasso.get()
                                .load(closetOrganiser.image)
                                .into(binding.clothingImage)
                        }
                    }
                    RESULT_CANCELED -> {
                    }
                    else -> { }
                }
            }
    }




}