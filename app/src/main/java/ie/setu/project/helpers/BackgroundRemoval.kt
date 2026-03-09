package ie.setu.project.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix

import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import dev.eren.removebg.RemoveBg
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

suspend fun removeBackgroundAndSave(
    context: Context,
    inputUri: android.net.Uri
): android.net.Uri {


    val rotation = context.contentResolver.openInputStream(inputUri).use { stream ->
        requireNotNull(stream)
        val exif = ExifInterface(stream)
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }

    val inputBitmap: Bitmap = context.contentResolver.openInputStream(inputUri).use { stream ->
        requireNotNull(stream) { "Couldn't open input stream for $inputUri" }
        BitmapFactory.decodeStream(stream)
            ?: throw IllegalArgumentException("Couldn't decode bitmap from $inputUri")
    }


    val correctedBitmap = if (rotation != 0f) {
        val matrix = Matrix().apply { postRotate(rotation) }
        Bitmap.createBitmap(inputBitmap, 0, 0, inputBitmap.width, inputBitmap.height, matrix, true)
    } else {
        inputBitmap
    }

    val outputBitmap: Bitmap = RemoveBg(context)
        .clearBackground(correctedBitmap)
        .first()
        ?: throw IllegalStateException("Background removal failed (output bitmap was null)")

    val outFile = File(context.cacheDir, "nobg_${UUID.randomUUID()}.png")
    FileOutputStream(outFile).use { fos ->
        outputBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        outFile
    )
}