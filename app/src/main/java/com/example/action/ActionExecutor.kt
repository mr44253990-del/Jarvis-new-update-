package com.example.action

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.data.local.dao.ActionLogDao
import com.example.data.local.dao.MemoryDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.ActionLogEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.TaskEntity
import com.example.service.ArcherFloatingOverlayService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActionExecutor(
    private val context: Context,
    private val taskDao: TaskDao,
    private val memoryDao: MemoryDao,
    private val actionLogDao: ActionLogDao
) {
    suspend fun execute(action: DeviceAction): String {
        return try {
            when (action) {
                is DeviceAction.CallPhone -> {
                    val uri = Uri.parse("tel:${action.phoneNumber}")
                    val hasCallPermission = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CALL_PHONE
                    ) == PackageManager.PERMISSION_GRANTED

                    val intent = if (hasCallPermission) {
                        Intent(Intent.ACTION_CALL, uri)
                    } else {
                        Intent(Intent.ACTION_DIAL, uri)
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)

                    logAction("Phone Call", "Calling ${action.phoneNumber}")
                    "স্যার, ${action.phoneNumber} নাম্বারে কল করা হচ্ছে।"
                }

                is DeviceAction.SendSms -> {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${action.phoneNumber}")).apply {
                        putExtra("sms_body", action.message)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    logAction("Send SMS", "To: ${action.phoneNumber}, Msg: ${action.message}")
                    "স্যার, ${action.phoneNumber} নাম্বারে এসএমএস পাঠানোর জন্য মেসেঞ্জার ওপেন করা হয়েছে।"
                }

                is DeviceAction.OpenYouTube -> {
                    val query = action.query.trim()
                    if (query.isNotBlank()) {
                        val webUri = Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
                        val ytIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                            setPackage("com.google.android.youtube")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(ytIntent)
                        } catch (e: Exception) {
                            // Fallback to default browser or any video app
                            val browserIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(browserIntent)
                        }
                        logAction("YouTube", "Query: $query")
                        "স্যার, ইউটিউবে '$query' চালানো হচ্ছে।"
                    } else {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(launchIntent)
                        } else {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(browserIntent)
                        }
                        logAction("YouTube", "Opened YouTube app/web")
                        "স্যার, ইউটিউব ওপেন করা হয়েছে।"
                    }
                }

                is DeviceAction.OpenBrowser -> {
                    val target = action.queryOrUrl
                    val uri = if (target.startsWith("http://") || target.startsWith("https://")) {
                        Uri.parse(target)
                    } else if (target.contains(".") && !target.contains(" ")) {
                        Uri.parse("https://$target")
                    } else {
                        Uri.parse("https://www.google.com/search?q=${Uri.encode(target)}")
                    }
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    logAction("Browser", "Target: $target")
                    "স্যার, ব্রাউজারে '$target' ওপেন করা হয়েছে।"
                }

                is DeviceAction.OpenCamera -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    logAction("Camera", "Launched camera capture")
                    "স্যার, ক্যামেরা ওপেন করা হয়েছে।"
                }

                is DeviceAction.ToggleFlashlight -> {
                    val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    val cameraId = cm.cameraIdList.firstOrNull { id ->
                        cm.getCameraCharacteristics(id).get(
                            android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
                        ) == true
                    }
                    if (cameraId != null) {
                        cm.setTorchMode(cameraId, action.turnOn)
                        val statusStr = if (action.turnOn) "চালু" else "বন্ধ"
                        logAction("Flashlight", "Torch set to ${action.turnOn}")
                        "স্যার, ফ্ল্যাশলাইট $statusStr করা হয়েছে।"
                    } else {
                        "ডিভাইসে কোনো ফ্ল্যাশলাইট পাওয়া যায়নি।"
                    }
                }

                is DeviceAction.SendEmail -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${action.recipient}")
                        putExtra(Intent.EXTRA_SUBJECT, action.subject)
                        putExtra(Intent.EXTRA_TEXT, action.body)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    logAction("Email", "Subject: ${action.subject}")
                    "স্যার, ইমেইল পাঠানোর অ্যাপ্লিকেশন ওপেন করা হয়েছে।"
                }

                is DeviceAction.CheckBattery -> {
                    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                    val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    logAction("Battery", "Level: $level%")
                    "স্যার, বর্তমান ব্যাটারি চার্জ $level%।"
                }

                is DeviceAction.OpenSettings -> {
                    val actionName = when (action.settingType) {
                        "wifi" -> Settings.ACTION_WIFI_SETTINGS
                        "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
                        else -> Settings.ACTION_SETTINGS
                    }
                    val intent = Intent(actionName).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    logAction("Settings", "Type: ${action.settingType}")
                    "স্যার, সেটিংস মেনু ওপেন করা হয়েছে।"
                }

                is DeviceAction.AddTask -> {
                    taskDao.insertTask(TaskEntity(title = action.taskTitle, isCompleted = false))
                    logAction("Add Task", action.taskTitle)
                    "স্যার, আপনার আজকের টাস্ক লিস্টে '${action.taskTitle}' যুক্ত করা হয়েছে।"
                }

                is DeviceAction.SaveMemory -> {
                    memoryDao.insertMemory(MemoryEntity(factKey = action.key, factValue = action.value))
                    logAction("Save Memory", "${action.key}: ${action.value}")
                    "স্যার, আমি এটি মনে রেখেছি: '${action.value}'।"
                }

                is DeviceAction.MinimizeToOverlay -> {
                    if (Settings.canDrawOverlays(context)) {
                        val serviceIntent = Intent(context, ArcherFloatingOverlayService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                        logAction("Minimize", "Started Floating Overlay")
                        "স্যার, Archer AI স্ক্রিন ওভারলে মোডে সক্রিয় করা হয়েছে।"
                    } else {
                        "স্যার, ফ্লোটিং অরবের জন্য 'Display over other apps' পারমিশন প্রয়োজন।"
                    }
                }

                is DeviceAction.AdjustVolume -> {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val direction = if (action.increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
                    val dirText = if (action.increase) "বাড়ানো" else "কমানো"
                    logAction("Volume", "Adjusted: $dirText")
                    "স্যার, মিডিয়া ভলিউম $dirText হয়েছে।"
                }

                is DeviceAction.GetCurrentTime -> {
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val timeStr = sdf.format(Date())
                    "স্যার, এখন সময় $timeStr।"
                }

                is DeviceAction.SetAlarm -> {
                    val intent = Intent(android.provider.AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(android.provider.AlarmClock.EXTRA_HOUR, action.hour)
                        putExtra(android.provider.AlarmClock.EXTRA_MINUTES, action.minute)
                        putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, action.message)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    logAction("Alarm", "Set for ${action.hour}:${action.minute}")
                    "স্যার, অ্যালার্ম সেট করার জন্য ক্লক অ্যাপ ওপেন করা হয়েছে।"
                }

                is DeviceAction.CreateTextFile -> {
                    val file = java.io.File(context.filesDir, action.fileName)
                    file.writeText(action.content)
                    // Also store as memory so AI remembers it
                    memoryDao.insertMemory(MemoryEntity(factKey = "File: ${action.fileName}", factValue = action.content))
                    logAction("Create File", "Created ${action.fileName} (${action.content.length} chars)")
                    "স্যার, '${action.fileName}' ফাইলটি সফলভাবে সংরক্ষণ করা হয়েছে।"
                }

                is DeviceAction.ReadTextFile -> {
                    val file = java.io.File(context.filesDir, action.fileName)
                    if (file.exists()) {
                        val content = file.readText()
                        logAction("Read File", "Read ${action.fileName}")
                        "স্যার, '${action.fileName}' ফাইলের বিষয়বস্তু হলো:\n$content"
                    } else {
                        "দুঃখিত স্যার, '${action.fileName}' ফাইলটি পাওয়া যায়নি।"
                    }
                }

                DeviceAction.None -> ""
            }
        } catch (e: Exception) {
            logAction("Action Error", e.localizedMessage ?: "Failed", status = "FAILED")
            "কমান্ড নির্বাহে সমস্যা হয়েছে: ${e.localizedMessage}"
        }
    }

    private suspend fun logAction(name: String, details: String, status: String = "SUCCESS") {
        try {
            actionLogDao.insertLog(
                ActionLogEntity(
                    actionName = name,
                    details = details,
                    status = status
                )
            )
        } catch (_: Exception) {}
    }

    private fun intentResolves(intent: Intent): Boolean {
        return intent.resolveActivity(context.packageManager) != null
    }
}
