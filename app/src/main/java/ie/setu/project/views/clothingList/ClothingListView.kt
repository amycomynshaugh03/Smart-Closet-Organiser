package ie.setu.project.views.clothingList

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import ie.setu.project.R
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.databinding.ActivityClothingListBinding
import ie.setu.project.models.ClosetOrganiserModel
import ie.setu.project.views.clothing.ClothingView

class ClothingListView : AppCompatActivity(), ClosetItemListener {
    private lateinit var binding: ActivityClothingListBinding
    private lateinit var presenter: ClothingListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClothingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        presenter = ClothingListPresenter(this)

        val clothesButton: Button = findViewById(R.id.btnClothes)
        clothesButton.setOnClickListener {
            startActivity(Intent(this, ClothingView::class.java))
        }
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

    fun notifyAdapterDataChanged() {
        (binding.recyclerView.adapter)?.notifyItemRangeChanged(0, binding.recyclerView.adapter?.itemCount ?: 0)
    }
}