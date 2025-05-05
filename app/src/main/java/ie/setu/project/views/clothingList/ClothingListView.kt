package ie.setu.project.views.clothingList

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import ie.setu.project.R
import ie.setu.project.adapters.CarouselAdapter
import ie.setu.project.adapters.ClosetAdapter
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.databinding.ActivityClothingListBinding
import ie.setu.project.models.ClosetOrganiserModel
import ie.setu.project.views.clothing.ClothingView
import ie.setu.project.views.outfit.OutfitView

class ClothingListView : AppCompatActivity(), ClosetItemListener {
    private lateinit var binding: ActivityClothingListBinding
    private lateinit var presenter: ClothingListPresenter
    private lateinit var viewPager: ViewPager2
    private lateinit var carouselAdapter: CarouselAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClothingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        presenter = ClothingListPresenter(this)

        setupCarousel()

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
        com.google.android.material.snackbar.Snackbar.make(binding.root, message, duration).show()
    }
}