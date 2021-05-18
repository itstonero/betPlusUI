package com.example.betplus.ui.gallery

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.betplus.R

private const val TAG = "AlarmReceiver"
public class AlarmReceiver : BroadcastReceiver() {
    private val CHANNEL_ID = "303"
    private lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "Received Transaction for Alarm Service")
        initChannel(context)
        val notifyIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://mobile.bet9ja.com/mobile/liveBetting"))
        notifyIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingItent = PendingIntent.getActivities(context, 0, arrayOf(notifyIntent), PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = context?.let {
            NotificationCompat.Builder(it, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(intent?.getStringExtra(AlarmReceiver.NF_TEAMS))
                .setContentText(intent?.getStringExtra(AlarmReceiver.NF_SCHEDULE))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingItent)
                .setPriority(NotificationCompat.PRIORITY_HIGH )
        }

        intent?.getStringExtra(AlarmReceiver.NF_ID)?.let { notificationManager.notify(it?.toInt(), builder?.build()) }
    }

    private fun initChannel(context: Context?){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(CHANNEL_ID, "BET PLUS", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Live Notification to BET"
            }
            notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object{
        val NF_TEAMS = "NF_TEAMS"
        val NF_SCHEDULE = "NF_SCHEDULE"
        val NF_ID = "NF_ID"
    }
}
