package ie.setu.project.helpers

import android.content.Context
import timber.log.Timber.e
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * Writes the provided data to a file in the app's private storage.
 *
 * @param context The context of the application used to access file storage.
 * @param fileName The name of the file to write the data to.
 * @param data The string data to be written to the file.
 */
fun write(context: Context, fileName: String, data: String) {
    try {
        val outputStreamWriter = OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE))
        outputStreamWriter.write(data)
        outputStreamWriter.close()
    } catch (e: Exception) {
        e("Cannot read file: %s", e.toString())
    }
}

/**
 * Reads the contents of a file in the app's private storage.
 *
 * @param context The context of the application used to access file storage.
 * @param fileName The name of the file to read from.
 * @return The contents of the file as a string.
 */
fun read(context: Context, fileName: String): String {
    var str = ""
    try {
        val inputStream = context.openFileInput(fileName)
        if (inputStream != null) {
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val partialStr = StringBuilder()
            var done = false
            while (!done) {
                val line = bufferedReader.readLine()
                done = (line == null)
                if (line != null) partialStr.append(line)
            }
            inputStream.close()
            str = partialStr.toString()
        }
    } catch (e: FileNotFoundException) {
        e("file not found: %s", e.toString())
    } catch (e: IOException) {
        e("cannot read file: %s", e.toString())
    }
    return str
}

/**
 * Checks if a file exists in the app's private storage.
 *
 * @param context The context of the application used to access file storage.
 * @param filename The name of the file to check.
 * @return True if the file exists, false otherwise.
 */
fun exists(context: Context, filename: String): Boolean {
    val file = context.getFileStreamPath(filename)
    return file.exists()
}
