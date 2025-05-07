package ie.setu.project.helpers

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import ie.setu.project.R

/**
 * Opens an image picker to allow the user to select an image from their device.
 *
 * This function creates an intent to open the document picker, which allows the user to
 * choose an image. It also sets necessary flags to grant URI permissions for the selected
 * image, and launches the image picker with the provided activity result launcher.
 *
 * @param intentLauncher The launcher used to start the activity for result.
 */
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
