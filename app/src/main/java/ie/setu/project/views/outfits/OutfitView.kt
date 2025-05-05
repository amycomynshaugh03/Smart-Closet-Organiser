package ie.setu.project.views.outfit

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ie.setu.project.R
import ie.setu.project.adapters.OutfitAdapter
import ie.setu.project.adapters.OutfitListener
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityOutfitBinding
import ie.setu.project.models.OutfitModel

class OutfitView : AppCompatActivity(), OutfitListener {
    private lateinit var binding: ActivityOutfitBinding
    private lateinit var presenter: OutfitPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutfitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)

        presenter = OutfitPresenter(this)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = OutfitAdapter(
            (application as MainApp).outfitItems.findAll(),
            this
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_outfit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return presenter.handleMenuSelection(item.itemId) || super.onOptionsItemSelected(item)
    }

    override fun onOutfitClick(outfit: OutfitModel) {
        presenter.onOutfitClick(outfit)
    }

    override fun onDeleteOutfitClick(outfit: OutfitModel) {
        presenter.onDeleteOutfitClick(outfit)
        onRefresh()
    }

    private fun onRefresh() {
        (binding.recyclerView.adapter as OutfitAdapter)
            .updateItems(presenter.getOutfits())
    }

    fun notifyAdapterChanged() {
        (binding.recyclerView.adapter)?.notifyItemRangeChanged(0, binding.recyclerView.adapter?.itemCount ?: 0)
    }

    fun updateAdapter() {
        val updatedList = (application as MainApp).outfitItems.findAll()
        (binding.recyclerView.adapter as OutfitAdapter).updateItems(updatedList)
    }

    fun showSnackbar(message: String, duration: Int) {
        com.google.android.material.snackbar.Snackbar.make(binding.root, message, duration).show()
    }
}