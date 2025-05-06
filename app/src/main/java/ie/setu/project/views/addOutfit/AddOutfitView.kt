package ie.setu.project.views.addOutfit

import android.os.Bundle
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

        val seasonSpinner: Spinner = findViewById(R.id.outfitSeason)
        ArrayAdapter.createFromResource(
            this,
            R.array.seasons_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            seasonSpinner.adapter = adapter
        }

        presenter = AddOutfitPresenter(this)

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
        val seasonsArray = resources.getStringArray(R.array.seasons_array)


        try {
            val seasonPosition = seasonsArray.indexOf(outfit.season)
            if (seasonPosition >= 0) {
                seasonSpinner.setSelection(seasonPosition)
            }
        } catch (e: Exception) {
            seasonSpinner.setSelection(0)
        }


        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.lastWorn.setText(sdf.format(outfit.lastWorn))
        } catch (e: Exception) {
            binding.lastWorn.setText("")
        }
    }

    fun updateLastWornDate(date: String) {
        binding.lastWorn.setText(date)
    }
}