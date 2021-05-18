package com.example.betplus.models

import android.app.Notification
import android.app.NotificationManager
import java.io.Serializable

public class NotificationPayload(val notificationManager: NotificationManager,
val notificationID: Int,
val notification: Notification) : Serializable {
}