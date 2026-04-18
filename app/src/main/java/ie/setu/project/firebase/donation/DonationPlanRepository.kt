package ie.setu.project.firebase.donation

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import ie.setu.project.models.donation.DonationPlan
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DonationPlanRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private fun col(uid: String) =
        db.collection("users").document(uid).collection("donationPlans")

    suspend fun save(uid: String, plan: DonationPlan): DonationPlan {
        val ref = if (plan.id.isBlank()) col(uid).document() else col(uid).document(plan.id)
        val withId = plan.copy(id = ref.id)
        ref.set(withId).await()
        return withId
    }

    suspend fun getAll(uid: String): List<DonationPlan> =
        col(uid).get().await().toObjects(DonationPlan::class.java)

    suspend fun getPending(uid: String): List<DonationPlan> =
        col(uid).whereEqualTo("confirmed", false).get().await()
            .toObjects(DonationPlan::class.java)

    suspend fun confirm(uid: String, planId: String) {
        col(uid).document(planId).update(
            mapOf("confirmed" to true, "confirmedAt" to Timestamp.now())
        ).await()
    }

    suspend fun delete(uid: String, planId: String) {
        col(uid).document(planId).delete().await()
    }
}