package com.example.lookatme.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lookatme.MainActivity
import com.example.lookatme.data.CompletionEntity
import com.example.lookatme.data.DatabaseProvider
import com.example.lookatme.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

import android.media.AudioAttributes
import android.net.Uri
import com.example.lookatme.R

const val CHANNEL_ID = "lookatme_slot_check_v2"
const val ACTION_MARK_DONE   = "com.example.lookatme.MARK_DONE"
const val ACTION_MARK_MISSED = "com.example.lookatme.MARK_MISSED"
const val EXTRA_SLOT_ID   = "slot_id"
const val EXTRA_SLOT_DATE = "slot_date"
const val EXTRA_NOTIF_ID  = "notif_id"

fun createNotificationChannel(context: Context) {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    try {
        nm.deleteNotificationChannel("lookatme_slot_check")
    } catch (_: Exception) {}

    val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.lookatme_bell}")
    val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
        .build()

    val channel = NotificationChannel(
        CHANNEL_ID,
        "Görev Bildirimleri",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "LookAtMe görev takip bildirimleri"
        enableVibration(true)
        setSound(soundUri, audioAttributes)
    }
    nm.createNotificationChannel(channel)
}

fun sendSlotCheckNotification(
    context: Context,
    slotId: Long,
    slotTitle: String,
    slotEmoji: String,
    dateStr: String,   // "yyyy-MM-dd"
    notifId: Int
) {
    val openIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val openPi = PendingIntent.getActivity(context, notifId, openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    fun actionPi(action: String) = PendingIntent.getBroadcast(
        context,
        notifId * 10 + if (action == ACTION_MARK_DONE) 1 else 2,
        Intent(action, null, context, NotificationActionReceiver::class.java).apply {
            putExtra(EXTRA_SLOT_ID, slotId)
            putExtra(EXTRA_SLOT_DATE, dateStr)
            putExtra(EXTRA_NOTIF_ID, notifId)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.lookatme_bell}")
    val notif = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle(slotTitle)
        .setContentText("Bu görev için durum bilgisini gir!")
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText("\"$slotTitle\"\nBu görev için durum bilgisini gir!"))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setSound(soundUri)
        .setContentIntent(openPi)
        .setAutoCancel(true)
        .addAction(android.R.drawable.checkbox_on_background, "Yapıldı",   actionPi(ACTION_MARK_DONE))
        .addAction(android.R.drawable.ic_delete,              "Yapılmadı", actionPi(ACTION_MARK_MISSED))
        .build()

    try {
        NotificationManagerCompat.from(context).notify(notifId, notif)
    } catch (_: SecurityException) { /* permission not granted */ }
}

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val slotId   = intent.getLongExtra(EXTRA_SLOT_ID, -1L)
        val dateStr  = intent.getStringExtra(EXTRA_SLOT_DATE) ?: return
        val notifId  = intent.getIntExtra(EXTRA_NOTIF_ID, 0)
        val status   = when (intent.action) {
            ACTION_MARK_DONE   -> "done"
            ACTION_MARK_MISSED -> "missed"
            else               -> return
        }

        // Dismiss notification
        NotificationManagerCompat.from(context).cancel(notifId)

        // Save to database
        val repo = Repository(DatabaseProvider.get(context))
        CoroutineScope(Dispatchers.IO).launch {
            val date = LocalDate.parse(dateStr)
            repo.upsertCompletion(slotId, date, status, "")
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule today's notifications after reboot
            CoroutineScope(Dispatchers.IO).launch {
                val repo = Repository(DatabaseProvider.get(context))
                NotificationScheduler.scheduleForToday(context, repo)
            }
        }
    }
}
