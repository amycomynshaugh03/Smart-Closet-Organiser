package ie.setu.project.views.clothingList

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import ie.setu.project.R
import ie.setu.project.adapters.CarouselAdapter
import ie.setu.project.adapters.ClosetAdapter
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityClothingListBinding
import ie.setu.project.models.ClosetOrganiserModel
import ie.setu.project.views.clothing.ClothingView
import java.util.*

class ClothingListView : AppCompatActivity(), ClosetItemListener {
    private lateinit var binding: ActivityClothingListBinding
    private lateinit var presenter: ClothingListPresenter
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClothingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        presenter = ClothingListPresenter(this)

        setupClothingListRecyclerView()
        setupCarousel()

        findViewById<Button>(R.id.btnClothes).setOnClickListener {
            startActivity(Intent(this,  ClothingView::class.java))
        }
    }

    private fun setupClothingListRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = ClosetAdapter(
            (application as MainApp).clothingItems.findAll(),
            this
        )
    }

    private fun setupCarousel() {
        viewPager = binding.carouselViewPager
        viewPager.adapter = CarouselAdapter(presenter.getCarouselItems())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onClosetItemClick(item: ClosetOrganiserModel) {
        presenter.onClosetItemClick(item)
    }

    override fun onDeleteItemClick(item: ClosetOrganiserModel) {
        presenter.onDeleteItemClick(item)
    }

    fun showSnackbar(message: String, duration: Int) {
        com.google.android.material.snackbar.Snackbar.make(binding.root, message, duration).show()
    }

    fun notifyClothingListChanged() {
        binding.recyclerView.adapter?.notifyItemRangeChanged(
            0,
            (application as MainApp).clothingItems.findAll().size
        )
    }

    fun updateClothingList(items: List<ClosetOrganiserModel>) {
        (binding.recyclerView.adapter as ClosetAdapter).updateItems(items)
    }
}