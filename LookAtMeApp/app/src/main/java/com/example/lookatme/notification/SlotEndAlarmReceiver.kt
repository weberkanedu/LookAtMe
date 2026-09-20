package com.example.lookatme.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.lookatme.data.DatabaseProvider
import com.example.lookatme.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Fires when a slot's end time arrives. Checks if already completed —
 * if not, sends the "Yapıldı mı?" notification.
 */
class SlotEndAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val slotId    = intent.getLongExtra(EXTRA_SLOT_ID, -1L)
        val dateStr   = intent.getStringExtra(EXTRA_SLOT_DATE) ?: return
        val notifId   = intent.getIntExtra(EXTRA_NOTIF_ID, 0)
        val slotTitle = intent.getStringExtra("slot_title") ?: "Görev"
        val slotEmoji = intent.getStringExtra("slot_emoji") ?: "📌"

        val repo = Repository(DatabaseProvider.get(context))

        CoroutineScope(Dispatchers.IO).launch {
            val date   = LocalDate.parse(dateStr)
            val existing = repo.completionsForDateOnce(date).find { it.slotId == slotId }
            if (existing != null) return@launch  // already marked, no notification needed

            sendSlotCheckNotification(context, slotId, slotTitle, slotEmoji, dateStr, notifId)
        }
    }
}
