package ie.setu.project.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.support.media.ExifInterface
import androidx.core.content.FileProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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

    val imageBytes = java.io.ByteArrayOutputStream().use { baos ->
        correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        baos.toByteArray()
    }

    val client = OkHttpClient()
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "image_file",
            "image.jpg",
            imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        )
        .addFormDataPart("size", "auto")
        .build()

    val request = Request.Builder()
        .url("https://api.remove.bg/v1.0/removebg")
        .addHeader("X-Api-Key", "")
        .post(requestBody)
        .build()

    val responseBytes = client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw IllegalStateException("remove.bg API failed: ${response.code} ${response.message}")
        }
        response.body?.bytes()
            ?: throw IllegalStateException("remove.bg returned empty response")
    }

    val outFile = File(context.cacheDir, "nobg_${UUID.randomUUID()}.png")
    FileOutputStream(outFile).use { fos ->
        fos.write(responseBytes)
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        outFile
    )
}

suspend fun correctImageRotation(
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

    if (rotation == 0f) return inputUri

    val inputBitmap = context.contentResolver.openInputStream(inputUri).use { stream ->
        BitmapFactory.decodeStream(requireNotNull(stream))
    } ?: return inputUri

    val matrix = Matrix().apply { postRotate(rotation) }
    val corrected = Bitmap.createBitmap(inputBitmap, 0, 0, inputBitmap.width, inputBitmap.height, matrix, true)

    val outFile = File(context.cacheDir, "rotated_${UUID.randomUUID()}.jpg")
    FileOutputStream(outFile).use { fos ->
        corrected.compress(Bitmap.CompressFormat.JPEG, 95, fos)
    }

    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
}