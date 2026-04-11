package ie.setu.project.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import ie.setu.project.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

data class ClothingAnalysisResult(
    val title: String = "",
    val description: String = "",
    val colour: String = "",
    val category: String = ""
)

suspend fun analyseClothingImage(
    context: Context,
    imageUri: Uri
): Result<ClothingAnalysisResult> = withContext(Dispatchers.IO) {
    try {
        val bitmap = loadSampledBitmap(context, imageUri)
            ?: return@withContext Result.failure(Exception("Could not decode image"))

        val model = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = ""
        )

        val prompt = """
            You are a clothing recognition assistant for a wardrobe organiser app.
            Analyse the clothing item in this image and respond with ONLY a JSON object
            in the exact format below, no markdown, no explanation, no extra text:

            {
              "title": "<short item name, e.g. 'White Linen Shirt' or 'Black Skinny Jeans'>",
              "description": "<one concise sentence describing style, fabric or notable details>",
              "colour": "<primary colour or pattern, e.g. 'Navy Blue' or 'Red Floral'>",
              "category": "<exactly one of: Tops, Bottoms, Dress, Shoes, Jackets>"
            }

            Rules:
            - title: 2–5 words, capitalise each word
            - description: max 15 words
            - colour: 1–3 words
            - category: must match one of the listed values exactly
            - If you cannot identify the item respond with all empty strings
        """.trimIndent()

        val inputContent = content {
            image(bitmap)
            text(prompt)
        }

        val response = model.generateContent(inputContent)
        val raw = response.text?.trim()
            ?: return@withContext Result.failure(Exception("Empty response"))

        Timber.d("Gemini clothing analysis raw: $raw")
        Result.success(parseAnalysisJson(raw))

    } catch (e: Exception) {
        Timber.e(e, "ClothingImageAnalyzer failed")
        Result.failure(e)
    }
}

private fun loadSampledBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
        val maxDim = 1024
        opts.inSampleSize = calculateInSampleSize(opts, maxDim, maxDim)
        opts.inJustDecodeBounds = false
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
    } catch (e: Exception) {
        Timber.e(e, "loadSampledBitmap failed for $uri")
        null
    }
}

private fun calculateInSampleSize(opts: BitmapFactory.Options, reqW: Int, reqH: Int): Int {
    val (h, w) = opts.outHeight to opts.outWidth
    var inSampleSize = 1
    if (h > reqH || w > reqW) {
        val halfH = h / 2
        val halfW = w / 2
        while (halfH / inSampleSize >= reqH && halfW / inSampleSize >= reqW) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

private fun parseAnalysisJson(raw: String): ClothingAnalysisResult {
    val json = raw
        .removePrefix("```json").removePrefix("```")
        .removeSuffix("```")
        .trim()

    fun extract(key: String): String {
        val pattern = Regex(""""$key"\s*:\s*"([^"]*?)"""")
        return pattern.find(json)?.groupValues?.get(1)?.trim() ?: ""
    }

    val category = extract("category").let { raw ->
        val valid = listOf("Tops", "Bottoms", "Dress", "Shoes", "Jackets", "Other")
        valid.firstOrNull { it.equals(raw, ignoreCase = true) } ?: "Tops"
    }

    return ClothingAnalysisResult(
        title       = extract("title"),
        description = extract("description"),
        colour      = extract("colour"),
        category    = category
    )
}