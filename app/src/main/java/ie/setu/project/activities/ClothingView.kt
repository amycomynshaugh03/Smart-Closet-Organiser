package ie.setu.project.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ie.setu.project.R
import ie.setu.project.adapters.ClosetAdapter
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityClothingBinding
import ie.setu.project.models.ClosetOrganiserModel

class ClothingView : AppCompatActivity(), ClosetItemListener {
    private lateinit var binding: ActivityClothingBinding
    private lateinit var presenter: ClothingPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClothingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)

        presenter = ClothingPresenter(this)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = ClosetAdapter(
            (application as MainApp).clothingItems.findAll(),
            this
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menuInflater.inflate(R.menu.menu_location, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return presenter.handleMenuSelection(item.itemId) || super.onOptionsItemSelected(item)
    }

    override fun onClosetItemClick(item: ClosetOrganiserModel) {
        presenter.onClosetItemClick(item)
    }

    override fun onDeleteItemClick(item: ClosetOrganiserModel) {
        presenter.onDeleteItemClick(item)
    }

    fun navigateToMain() {
        startActivity(Intent(this, ClothingListView::class.java))
    }

    fun notifyAdapterChanged() {
        (binding.recyclerView.adapter)?.notifyItemRangeChanged(0, binding.recyclerView.adapter?.itemCount ?: 0)
    }

    fun updateAdapter() {
        val updatedList = (application as MainApp).clothingItems.findAll()
        (binding.recyclerView.adapter as ClosetAdapter).updateItems(updatedList)
    }

    fun showSnackbar(message: String, duration: Int) {
        com.google.android.material.snackbar.Snackbar.make(binding.root, message, duration).show()
    }
}
