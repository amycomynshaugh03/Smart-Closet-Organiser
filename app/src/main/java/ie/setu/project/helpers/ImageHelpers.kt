package ie.setu.project.helpers

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import ie.setu.project.R

/**
 * Helper function to launch an image picker intent.
 * This function opens the system's file chooser for selecting an image file.
 *
 * @param intentLauncher The ActivityResultLauncher used to start the intent.
 */
fun showImagePicker(intentLauncher: ActivityResultLauncher<Intent>) {
    // Create an intent to open the image picker (document picker).
    var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)

    // Set the MIME type to images only.
    chooseFile.type = "image/*"

    // Create a chooser so the user can select an app to handle the intent.
    chooseFile = Intent.createChooser(chooseFile, R.string.select_clothing_image.toString())

    // Launch the image picker.
    intentLauncher.launch(chooseFile)
}
