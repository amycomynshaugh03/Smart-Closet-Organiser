package ie.setu.project.activities


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.adapters.ClosetAdapter
import ie.setu.project.adapters.ClosetItemListener
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityClothingBinding
import ie.setu.project.models.ClosetOrganiserModel

class clothingActivity : AppCompatActivity(), ClosetItemListener {

    lateinit var app: MainApp
    private lateinit var binding: ActivityClothingBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClothingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)

        app = application as MainApp

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = ClosetAdapter(app.clothingItems.findAll(), this)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menuInflater.inflate(R.menu.menu_location, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.nav_to_main -> {
                val intent = Intent(this, ClothingListActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.item_add -> {
                val intent = Intent(this, MainActivity::class.java)
                getResult.launch(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                (binding.recyclerView.adapter)?.notifyItemRangeChanged(0, app.clothingItems.findAll().size)
            }
            if (result.resultCode == Activity.RESULT_CANCELED) {
                Snackbar.make(binding.root, "Clothing Item Add Cancelled", Snackbar.LENGTH_LONG).show()
            }
        }

    override fun onClosetItemClick(item: ClosetOrganiserModel) {
        val launcherIntent = Intent(this, MainActivity::class.java)
        launcherIntent.putExtra("closet_item_edit", item )
        getResult.launch(launcherIntent)

    }

    override fun onDeleteItemClick(item: ClosetOrganiserModel) {
        // Remove the item from the app's data store (e.g., database)
        app.clothingItems.delete(item)

        // Create a new list without the deleted item
        val updatedList = app.clothingItems.findAll().filter { it != item }

        // Pass the updated list to the adapter and notify it to refresh
        (binding.recyclerView.adapter as ClosetAdapter).updateItems(updatedList)

        // Show a Snackbar to inform the user about the deletion
        Snackbar.make(binding.root, "Clothing Item Deleted", Snackbar.LENGTH_LONG).show()
    }



}
