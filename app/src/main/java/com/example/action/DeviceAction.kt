package com.example.action

sealed class DeviceAction {
    data class CallPhone(val phoneNumber: String) : DeviceAction()
    data class SendSms(val phoneNumber: String, val message: String) : DeviceAction()
    data class OpenYouTube(val query: String) : DeviceAction()
    data class OpenBrowser(val queryOrUrl: String) : DeviceAction()
    object OpenCamera : DeviceAction()
    data class ToggleFlashlight(val turnOn: Boolean) : DeviceAction()
    data class SendEmail(val recipient: String, val subject: String, val body: String) : DeviceAction()
    object CheckBattery : DeviceAction()
    data class OpenSettings(val settingType: String) : DeviceAction() // "wifi", "bluetooth", "general"
    data class AddTask(val taskTitle: String) : DeviceAction()
    data class SaveMemory(val key: String, val value: String) : DeviceAction()
    object MinimizeToOverlay : DeviceAction()
    data class SetAlarm(val hour: Int, val minute: Int, val message: String) : DeviceAction()
    data class AdjustVolume(val increase: Boolean) : DeviceAction()
    object GetCurrentTime : DeviceAction()
    data class CreateTextFile(val fileName: String, val content: String) : DeviceAction()
    data class ReadTextFile(val fileName: String) : DeviceAction()
    object None : DeviceAction()
}
