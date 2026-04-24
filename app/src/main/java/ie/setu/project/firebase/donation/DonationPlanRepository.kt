package ie.setu.project.firebase.donation

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import ie.setu.project.models.donation.DonationPlan
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore repository for managing [DonationPlan] records.
 *
 * Operates under: `users/{uid}/donationPlans/{planId}`.
 * Handles saving, retrieving, confirming, and deleting donation plans.
 * Injected as a singleton via Hilt.
 *
 * @constructor Injects [FirebaseFirestore] via Hilt.
 * @param db The Firestore database instance.
 */
@Singleton
class DonationPlanRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private fun col(uid: String) =
        db.collection("users").document(uid).collection("donationPlans")

    /**
     * Saves a donation plan to Firestore, generating a new document ID if the plan's ID is blank.
     *
     * @param uid The authenticated user's UID.
     * @param plan The plan to save.
     * @return The saved [DonationPlan] with its Firestore-assigned ID populated.
     */
    suspend fun save(uid: String, plan: DonationPlan): DonationPlan {
        val ref = if (plan.id.isBlank()) col(uid).document() else col(uid).document(plan.id)
        val withId = plan.copy(id = ref.id)
        ref.set(withId).await()
        return withId
    }

    /**
     * Retrieves all donation plans for the user, regardless of confirmation status.
     *
     * @param uid The authenticated user's UID.
     * @return A list of all [DonationPlan] objects.
     */
    suspend fun getAll(uid: String): List<DonationPlan> =
        col(uid).get().await().toObjects(DonationPlan::class.java)

    /**
     * Retrieves only the unconfirmed (pending) donation plans for the user.
     *
     * @param uid The authenticated user's UID.
     * @return A list of [DonationPlan] objects where [DonationPlan.confirmed] is false.
     */
    suspend fun getPending(uid: String): List<DonationPlan> =
        col(uid).whereEqualTo("confirmed", false).get().await()
            .toObjects(DonationPlan::class.java)

    /**
     * Marks a donation plan as confirmed, recording the current timestamp.
     *
     * @param uid The authenticated user's UID.
     * @param planId The Firestore document ID of the plan to confirm.
     */
    suspend fun confirm(uid: String, planId: String) {
        col(uid).document(planId).update(
            mapOf("confirmed" to true, "confirmedAt" to Timestamp.now())
        ).await()
    }

    /**
     * Permanently deletes a donation plan from Firestore.
     *
     * @param uid The authenticated user's UID.
     * @param planId The Firestore document ID of the plan to delete.
     */
    suspend fun delete(uid: String, planId: String) {
        col(uid).document(planId).delete().await()
    }
}