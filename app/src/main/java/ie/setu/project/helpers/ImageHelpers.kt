package ie.setu.project.helpers

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import ie.setu.project.R


fun showImagePicker(intentLauncher: ActivityResultLauncher<Intent>) {
    var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        type = "image/*"
    }

    // Create a chooser so the user can select an app to handle the intent.
    chooseFile = Intent.createChooser(chooseFile, R.string.select_clothing_image.toString())

    // Launch the image picker.
    intentLauncher.launch(chooseFile)
}
