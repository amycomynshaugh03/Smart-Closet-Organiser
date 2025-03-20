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
import ie.setu.project.databinding.ActivityClothingListBinding
import ie.setu.project.models.ClosetOrganiserModel
import timber.log.Timber.i

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
    }

    override fun onClosetItemClick(item: ClosetOrganiserModel) {
        i("Hello")
    }
}

