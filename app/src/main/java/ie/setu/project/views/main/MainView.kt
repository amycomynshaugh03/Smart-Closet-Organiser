package ie.setu.project.views.main

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import ie.setu.project.R
import ie.setu.project.databinding.ActivityMainBinding
import ie.setu.project.models.clothing.ClosetOrganiserModel
import timber.log.Timber.i
import java.text.SimpleDateFormat
import java.util.Locale

class MainView : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var presenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topAppBar.title = title
        setSupportActionBar(binding.topAppBar)

        presenter = MainPresenter(this)

        // Season spinner setup
        val seasonSpinner: Spinner = findViewById(R.id.clothingSeason)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.seasons_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        seasonSpinner.adapter = adapter

        binding.lastWorn.setOnClickListener {
            presenter.showDatePicker()
        }

        binding.btnAdd.setOnClickListener {
            if (binding.clothingItemTitle.text.toString().isEmpty()) {
                Snackbar.make(
                    it,
                    getString(R.string.please_enter_missing_item),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                presenter.doAddOrSave(
                    binding.clothingItemTitle.text.toString(),
                    binding.clothingDescription.text.toString(),
                    binding.clothingColour.text.toString(),
                    binding.clothingSize.text.toString(),
                    seasonSpinner.selectedItem.toString()
                )
            }
        }

        binding.chooseImage.setOnClickListener {
            presenter.doSelectImage()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_clothing_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_cancel -> presenter.doCancel()
        }
        return super.onOptionsItemSelected(item)
    }

    fun showClosetItem(item: ClosetOrganiserModel) {
        binding.clothingItemTitle.setText(item.title)
        binding.clothingDescription.setText(item.description)
        binding.clothingColour.setText(item.colourPattern)
        binding.clothingSize.setText(item.size)

        val seasonSpinner: Spinner = findViewById(R.id.clothingSeason)
        val adapter = seasonSpinner.adapter as ArrayAdapter<String>
        val seasonPosition = adapter.getPosition(item.season)
        seasonSpinner.setSelection(seasonPosition)

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = sdf.format(item.lastWorn)
        binding.lastWorn.setText(formattedDate)

        Picasso.get()
            .load(item.image)
            .resize(600, 600)
            .rotate(90f)
            .into(binding.clothingImage)

        if (item.image != Uri.EMPTY) {
            binding.chooseImage.setText(R.string.change_clothing_image)
        }

        binding.btnAdd.text = getString(R.string.save_clothing_item)
    }

    fun updateImage(image: Uri) {
        i("Got Result $image")
        Picasso.get()
            .load(image)
            .rotate(90f)
            .resize(600, 600)
            .into(binding.clothingImage)
        binding.chooseImage.setText(R.string.change_clothing_image)
    }

    fun updateLastWornDate(date: String) {
        binding.lastWorn.setText(date)
    }
}