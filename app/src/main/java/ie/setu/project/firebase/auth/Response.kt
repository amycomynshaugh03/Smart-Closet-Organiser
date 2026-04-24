package ie.setu.project.firebase.auth

/**
 * A sealed class representing the state of an asynchronous Firebase operation.
 *
 * Used throughout the authentication and data layers to represent one of three states:
 * a successful result, a loading/in-progress state, or a failure with an exception.
 *
 * @param R The type of the successful result data.
 */
sealed class Response<out R> {

    /**
     * Indicates the operation completed successfully.
     * @property data The result of the operation.
     */
    data class Success<out R>(val data: R) : Response<R>()

    /**
     * Indicates the operation failed with an exception.
     * @property e The exception that caused the failure.
     */
    data class Failure(val e: Exception) : Response<Nothing>()

    /**
     * Indicates the operation is currently in progress.
     */
    object Loading : Response<Nothing>()
}