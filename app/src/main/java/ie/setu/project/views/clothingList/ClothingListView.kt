package ie.setu.project.views.clothingList

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.adapters.SearchResultsAdapter
import ie.setu.project.adapters.carousel.CarouselAdapter
import ie.setu.project.databinding.ActivityClothingListBinding
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.models.weather.WeatherCondition
import ie.setu.project.models.weather.WeatherResponse
import ie.setu.project.views.clothing.ClothingView
import ie.setu.project.views.outfit.OutfitView

/**
 * Activity for displaying a list of clothing items and outfits.
 * Includes a carousel for featured clothing items, a search feature, and weather information.
 */
class ClothingListView : AppCompatActivity(), ClosetItemListener {

    private lateinit var binding: ActivityClothingListBinding
    private lateinit var presenter: ClothingListPresenter
    private lateinit var viewPager: ViewPager2
    private lateinit var carouselAdapter: CarouselAdapter
    private lateinit var searchResultsAdapter: SearchResultsAdapter

    /**
     * Called when the activity is created. Initializes the view, sets up listeners, and fetches weather data.
     * @param savedInstanceState A Bundle containing the activity's previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClothingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        presenter = ClothingListPresenter(this)
        presenter.fetchWeather() // Fetch the weather information

        setupCarousel() // Setup the carousel view
        setupSearchRecyclerView() // Setup the search results recycler view
        setupSearchListener() // Setup the search listener for real-time searching

        binding.btnClothes.setOnClickListener {
            startActivity(Intent(this, ClothingView::class.java)) // Navigate to the clothing view
        }

        binding.btnOutfits.setOnClickListener {
            startActivity(Intent(this, OutfitView::class.java)) // Navigate to the outfit view
        }
    }

    /**
     * Called when the activity comes back into focus. Refreshes the carousel view.
     */
    override fun onResume() {
        super.onResume()
        refreshCarousel()
    }

    /**
     * Handles item selections from the options menu.
     * @param item The menu item that was selected.
     * @return True if the item was handled, otherwise false.
     */
    override fun onOptionsItemSelected(item: MenuItem) = super.onOptionsItemSelected(item)

    /**
     * Sets up the carousel view with the ViewPager2 widget and an empty list of items.
     */
    private fun setupCarousel() {
        viewPager = binding.carouselViewPager
        carouselAdapter = CarouselAdapter(emptyList(), this)
        viewPager.adapter = carouselAdapter
    }

    /**
     * Refreshes the carousel view with the latest list of items.
     */
    fun refreshCarousel() {
        (binding.carouselViewPager.adapter as? CarouselAdapter)?.submitList(presenter.getCarouselItems())
    }

    /**
     * Called when a closet item is clicked. Delegates the action to the presenter.
     * @param item The closet item that was clicked.
     */
    override fun onClosetItemClick(item: ClosetOrganiserModel) {
        presenter.onClosetItemClick(item)
    }

    /**
     * Called when a closet item delete action is triggered. Delegates the action to the presenter.
     * @param item The closet item to be deleted.
     */
    override fun onDeleteItemClick(item: ClosetOrganiserModel) {
        presenter.onDeleteItemClick(item)
        refreshCarousel() // Refresh carousel after deletion
    }

    /**
     * Displays a snackbar message to the user.
     * @param message The message to be displayed.
     * @param duration The duration for which the snackbar will be visible.
     */
    fun showSnackbar(message: String, duration: Int) {
        Snackbar.make(binding.root, message, duration).show()
    }

    /**
     * Updates the UI with the fetched weather information.
     * @param weather The weather response object containing the weather data.
     */
    @SuppressLint("SetTextI18n")
    fun updateWeatherUI(weather: WeatherResponse) {
        val current = weather.current_weather
        val condition = WeatherCondition.fromCode(current.weathercode, current.is_day)

        // Update weather UI with current temperature and condition
        binding.weatherTemperature.text = "${current.temperature}°C"
        binding.weatherDescription.text = condition.description
        binding.weatherIcon.setImageResource(
            if (current.is_day == 1) condition.dayIcon else condition.nightIcon
        )
    }

    /**
     * Displays an error message on the weather UI and shows a snackbar with the error.
     * @param message The error message to be displayed.
     */
    @SuppressLint("SetTextI18n")
    fun showWeatherError(message: String) {
        runOnUiThread {
            binding.weatherTemperature.text = "Error"
            binding.weatherDescription.text = message
            showSnackbar(message, Snackbar.LENGTH_SHORT)
        }
    }

    /**
     * Sets up the recycler view for displaying search results.
     */
    private fun setupSearchRecyclerView() {
        searchResultsAdapter = SearchResultsAdapter { item ->
            when (item) {
                is ClosetOrganiserModel -> openClothingDetails(item) // Open clothing details
                is OutfitModel -> openOutfitDetails(item) // Open outfit details
            }
            binding.searchResultsRecyclerView.visibility = View.GONE // Hide search results after selection
        }

        binding.searchResultsRecyclerView.apply {
            adapter = searchResultsAdapter
            layoutManager = LinearLayoutManager(this@ClothingListView)
        }
    }

    /**
     * Sets up the listener for the search EditText to handle text changes.
     * Updates search results dynamically based on the input.
     */
    private fun setupSearchListener() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    val results = presenter.performSearch(query)
                    searchResultsAdapter.updateList(results)
                    binding.searchResultsRecyclerView.visibility = View.VISIBLE // Show search results
                } else {
                    binding.searchResultsRecyclerView.visibility = View.GONE // Hide search results if query is empty
                }
            }

            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    /**
     * Opens the clothing details activity for the selected clothing item.
     * @param item The clothing item to view.
     */
    private fun openClothingDetails(item: ClosetOrganiserModel) {
        startActivity(
            Intent(this, ClothingView::class.java).apply {
                putExtra("closet_item_edit", item)
            }
        )
    }

    /**
     * Opens the outfit details activity for the selected outfit item.
     * @param item The outfit item to view.
     */
    private fun openOutfitDetails(item: OutfitModel) {
        startActivity(
            Intent(this, OutfitView::class.java).apply {
                putExtra("outfit_item_edit", item)
            }
        )
    }
}
