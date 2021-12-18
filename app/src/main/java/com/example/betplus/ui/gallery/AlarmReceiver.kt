package com.example.betplus.ui.gallery

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.betplus.MainActivity
import com.example.betplus.R
import com.example.betplus.R.*

private const val TAG = "AlarmReceiver"
class AlarmReceiver : BroadcastReceiver() {
    private val channelID = "303"
    private lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "Received Transaction for Alarm Service")
        initChannel(context)
        val betIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://mobile.bet9ja.com/mobile/liveBetting"))
        val slipInfoIntent = Intent(context, MainActivity::class.java)
        betIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingBetIntent = PendingIntent.getActivity(context, 0, betIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pendingSlipIntent = PendingIntent.getActivity(context, 1, slipInfoIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //val previewBitMap = (drawable.preview as BitmapDrawable).bitmap

        val builder = context?.let {
            NotificationCompat.Builder(it, channelID)
                .setSmallIcon(drawable.stake)
                .setContentTitle(intent?.getStringExtra(NF_TEAMS))
                .setContentText(intent?.getStringExtra(NF_SCHEDULE))
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setOnlyAlertOnce(true)
                .addAction(drawable.preview, "Info", pendingSlipIntent)
                .addAction(drawable.stake, "Bet", pendingBetIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH )
        }

        intent?.getStringExtra(NF_ID)?.let { notificationManager.notify(it.toInt(), builder?.build()) }
    }

    private fun initChannel(context: Context?){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelID, "BET PLUS", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Live Notification to BET"
            }
            notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object{
        const val NF_TEAMS = "NF_TEAMS"
        const val NF_SCHEDULE = "NF_SCHEDULE"
        const val NF_ID = "NF_ID"
    }
}
