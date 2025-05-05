package ie.setu.project.views.addOutfit

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ie.setu.project.R
import ie.setu.project.databinding.ActivityAddOutfitBinding
import ie.setu.project.models.OutfitModel
import java.text.SimpleDateFormat
import java.util.Locale

class AddOutfitView : AppCompatActivity() {
    private lateinit var binding: ActivityAddOutfitBinding
    private lateinit var presenter: AddOutfitPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddOutfitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = AddOutfitPresenter(this)

        // Setup season spinner
        val seasonSpinner: Spinner = findViewById(R.id.outfitSeason)
        ArrayAdapter.createFromResource(
            this,
            R.array.seasons_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            seasonSpinner.adapter = adapter
        }

        binding.lastWorn.setOnClickListener {
            presenter.showDatePicker()
        }

        binding.chooseClothing.setOnClickListener {
            presenter.launchClothingSelection()
        }

        binding.btnAdd.setOnClickListener {
            if (binding.outfitTitle.text.isNullOrEmpty()) {
                Snackbar.make(it, R.string.please_enter_missing_item, Snackbar.LENGTH_LONG).show()
            } else {
                presenter.doAddOrSave(
                    binding.outfitTitle.text.toString(),
                    binding.outfitDescription.text.toString(),
                    seasonSpinner.selectedItem.toString()
                )
            }
        }
    }

    fun showOutfit(outfit: OutfitModel) {
        binding.outfitTitle.setText(outfit.title)
        binding.outfitDescription.setText(outfit.description)

        val seasonSpinner: Spinner = findViewById(R.id.outfitSeason)
        val adapter = seasonSpinner.adapter as ArrayAdapter<String>
        val seasonPosition = adapter.getPosition(outfit.season)
        seasonSpinner.setSelection(seasonPosition)

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.lastWorn.setText(sdf.format(outfit.lastWorn))
    }

    fun updateLastWornDate(date: String) {
        binding.lastWorn.setText(date)
    }
}