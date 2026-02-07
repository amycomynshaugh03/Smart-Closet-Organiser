package ie.setu.project.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.FileProvider
import dev.eren.removebg.RemoveBg
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

suspend fun removeBackgroundAndSave(
    context: Context,
    inputUri: android.net.Uri
): android.net.Uri {

    val inputBitmap: Bitmap = context.contentResolver.openInputStream(inputUri).use { stream ->
        requireNotNull(stream) { "Couldn't open input stream for $inputUri" }

        BitmapFactory.decodeStream(stream)
            ?: throw IllegalArgumentException("Couldn't decode bitmap from $inputUri")
    }

    val outputBitmap: Bitmap = RemoveBg(context)
        .clearBackground(inputBitmap)
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
