package ie.setu.project.activities


import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

import ie.setu.project.R
import ie.setu.project.closet.main.MainApp
import ie.setu.project.databinding.ActivityClothingListBinding
import ie.setu.project.databinding.ActivityMainBinding
import ie.setu.project.databinding.CardClothingBinding
import ie.setu.project.models.ClosetOrganiserModel

class ClothingListActivity : AppCompatActivity() {

    lateinit var app: MainApp
    private lateinit var binding: ActivityClothingListBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClothingListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)

        app = application as MainApp

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = ClosetAdapter(app.closetItems)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add -> {
                val launcherIntent = Intent(this, MainActivity::class.java)
                getResult.launch(launcherIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                (binding.recyclerView.adapter)?.
                notifyItemRangeChanged(0,app.closetItems.size)
            }
            if (it.resultCode == Activity.RESULT_CANCELED) {
                Snackbar.make(binding.root, "Clothing Item Add Cancelled", Snackbar.LENGTH_LONG).show()
            }
        }
}



class ClosetAdapter constructor(private var closetItems: List<ClosetOrganiserModel>) :
    RecyclerView.Adapter<ClosetAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardClothingBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val closetItem = closetItems[holder.adapterPosition]
        holder.bind(closetItem)
    }

    override fun getItemCount(): Int = closetItems.size

    class MainHolder(private val binding : CardClothingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(closetItem: ClosetOrganiserModel) {
            binding.clothingItemTitle.text = closetItem.title
            binding.clothingDescription.text = closetItem.description
        }
    }
}