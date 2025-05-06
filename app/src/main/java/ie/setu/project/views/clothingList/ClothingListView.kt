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
import ie.setu.project.adapters.carousel.CarouselAdapter
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.adapters.SearchResultsAdapter
import ie.setu.project.databinding.ActivityClothingListBinding
import ie.setu.project.models.clothing.ClosetOrganiserModel
import ie.setu.project.models.outfit.OutfitModel
import ie.setu.project.models.weather.WeatherCondition
import ie.setu.project.models.weather.WeatherResponse
import ie.setu.project.views.clothing.ClothingView
import ie.setu.project.views.outfit.OutfitView

class ClothingListView : AppCompatActivity(), ClosetItemListener {
    private lateinit var binding: ActivityClothingListBinding
    private lateinit var presenter: ClothingListPresenter
    private lateinit var viewPager: ViewPager2
    private lateinit var carouselAdapter: CarouselAdapter
    private lateinit var searchResultsAdapter: SearchResultsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClothingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        presenter = ClothingListPresenter(this)
        presenter.fetchWeather()


        setupCarousel()
        setupSearchRecyclerView()
        setupSearchListener()

        binding.btnClothes.setOnClickListener {
            startActivity(Intent(this, ClothingView::class.java))
        }

        binding.btnOutfits.setOnClickListener {
            startActivity(Intent(this, OutfitView::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshCarousel()
    }

    override fun onOptionsItemSelected(item: MenuItem) = super.onOptionsItemSelected(item)

    private fun setupCarousel() {
        viewPager = binding.carouselViewPager
        carouselAdapter = CarouselAdapter(emptyList(), this)
        viewPager.adapter = carouselAdapter
    }

    fun refreshCarousel() {
        (binding.carouselViewPager.adapter as? CarouselAdapter)?.submitList(presenter.getCarouselItems())
    }

    override fun onClosetItemClick(item: ClosetOrganiserModel) {
        presenter.onClosetItemClick(item)
    }

    override fun onDeleteItemClick(item: ClosetOrganiserModel) {
        presenter.onDeleteItemClick(item)
        refreshCarousel()
    }

    fun showSnackbar(message: String, duration: Int) {
        Snackbar.make(binding.root, message, duration).show()
    }

    @SuppressLint("SetTextI18n")
    fun updateWeatherUI(weather: WeatherResponse) {
        val current = weather.current_weather
        val condition = WeatherCondition.fromCode(current.weathercode, current.is_day)

        binding.weatherTemperature.text = "${current.temperature}°C"
        binding.weatherDescription.text = condition.description
        binding.weatherIcon.setImageResource(
            if (current.is_day == 1) condition.dayIcon else condition.nightIcon
        )
    }

    @SuppressLint("SetTextI18n")
    fun showWeatherError(message: String) {
        runOnUiThread {
            binding.weatherTemperature.text = "Error"
            binding.weatherDescription.text = message
            showSnackbar(message, Snackbar.LENGTH_SHORT)
        }
    }

    private fun setupSearchRecyclerView() {
        searchResultsAdapter = SearchResultsAdapter { item ->
            when (item) {
                is ClosetOrganiserModel -> openClothingDetails(item)
                is OutfitModel -> openOutfitDetails(item)
            }
            binding.searchResultsRecyclerView.visibility = View.GONE
        }

        binding.searchResultsRecyclerView.apply {
            adapter = searchResultsAdapter
            layoutManager = LinearLayoutManager(this@ClothingListView)
        }
    }

    private fun setupSearchListener() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    val results = presenter.performSearch(query)
                    searchResultsAdapter.updateList(results)
                    binding.searchResultsRecyclerView.visibility = View.VISIBLE
                } else {
                    binding.searchResultsRecyclerView.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    private fun openClothingDetails(item: ClosetOrganiserModel) {
        startActivity(Intent(this, ClothingView::class.java).apply {
            putExtra("closet_item_edit", item)
        })
    }

    private fun openOutfitDetails(item: OutfitModel) {
        startActivity(Intent(this, OutfitView::class.java).apply {
            putExtra("outfit_item_edit", item)
        })
    }
}
