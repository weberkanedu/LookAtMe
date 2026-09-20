package com.example.lookatme.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.lookatme.data.Repository
import com.example.lookatme.data.SlotEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


object NotificationScheduler {

    private val ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * Schedule end-of-slot notifications for every slot today that:
     * - hasn't been completed yet
     * - ends in the future
     */
    suspend fun scheduleForToday(context: Context, repo: Repository) {
        val today = LocalDate.now()
        val slots = repo.slotsForDateOnce(today)
        val completions = repo.completionsForDateOnce(today)
        val completedIds = completions.map { it.slotId }.toSet()
        val now = LocalTime.now()

        slots.forEach { slot ->
            if (slot.id in completedIds) return@forEach
            val endTime = LocalTime.parse(slot.endTime)
            if (endTime.isAfter(now)) {
                scheduleSlotEndNotification(context, slot, today)
            }
        }
    }

    fun scheduleSlotEndNotification(context: Context, slot: SlotEntity, date: LocalDate) {
        val endTime = LocalTime.parse(slot.endTime)
        val triggerAt = LocalDateTime.of(date, endTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val now = System.currentTimeMillis()
        if (triggerAt <= now) return  // already past

        val notifId = (slot.id * 10000 + date.toEpochDay() % 10000).toInt()
        val dateStr = date.format(ISO)

        val intent = Intent(context, SlotEndAlarmReceiver::class.java).apply {
            putExtra(EXTRA_SLOT_ID, slot.id)
            putExtra(EXTRA_SLOT_DATE, dateStr)
            putExtra(EXTRA_NOTIF_ID, notifId)
            putExtra("slot_title", slot.title)
            putExtra("slot_emoji", slot.emoji)
        }

        val pi = PendingIntent.getBroadcast(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } catch (_: SecurityException) {
            // Fallback to inexact if exact alarms not permitted
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancelSlotNotification(context: Context, slotId: Long, date: LocalDate) {
        val notifId = (slotId * 10000 + date.toEpochDay() % 10000).toInt()
        val intent = Intent(context, SlotEndAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, notifId, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pi?.let {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(it)
            NotificationManagerCompat.from(context).cancel(notifId)
        }
    }
}
