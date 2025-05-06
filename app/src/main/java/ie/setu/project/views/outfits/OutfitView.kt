package ie.setu.project.views.outfit

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.adapters.outfit.OutfitAdapter
import ie.setu.project.databinding.ActivityOutfitBinding


class OutfitView : AppCompatActivity() {
    private lateinit var binding: ActivityOutfitBinding
    private lateinit var presenter: OutfitPresenter
    private lateinit var adapter: OutfitAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutfitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)

        presenter = OutfitPresenter(this)
        setupRecyclerView()
        loadOutfits()
    }

    private fun setupRecyclerView() {
        adapter = OutfitAdapter(mutableListOf()) { outfit ->
            presenter.onOutfitClick(outfit)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OutfitView)
            adapter = this@OutfitView.adapter
        }
    }

    fun loadOutfits() {
        adapter.updateItems(presenter.getOutfits())
    }

    fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return presenter.handleMenuSelection(item.itemId) || super.onOptionsItemSelected(item)
    }
}