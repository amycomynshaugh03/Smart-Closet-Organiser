package ie.setu.project.views.donation

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * A Hilt-injected [CoroutineWorker] that fires a local notification reminding
 * the user to drop off a scheduled clothing donation.
 *
 * Scheduled by [DonationViewModel.scheduleDonation] via [WorkManager] with a delay
 * calculated from the planned donation date. The notification deep-links into the
 * Donation tab of the app.
 *
 * Requires the "donation_reminders" notification channel to exist (created in [MainApp]).
 *
 * Input data keys:
 * - `"item_title"` — The title of the clothing item being donated.
 * - `"location_name"` — The name of the chosen donation location.
 */
@HiltWorker
class DonationReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val itemTitle  = inputData.getString("item_title")  ?: "your item"
        val locationName = inputData.getString("location_name") ?: "the donation spot"

        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("open_donation_tab", true)
            }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "donation_reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Donation Day!")
            .setContentText("Don't forget to drop off $itemTitle at $locationName today")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Don't forget to drop off $itemTitle at $locationName today. Open the app to confirm your donation."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(itemTitle.hashCode(), notification)

        return Result.success()
    }
}