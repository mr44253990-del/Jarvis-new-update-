package com.example.action

import java.util.regex.Pattern

object ActionParser {
    fun parse(input: String): DeviceAction {
        val text = input.trim().lowercase()

        // 1. Phone Call ("017... কল করো", "কল করো 018...", "call 123456")
        val phonePattern = Pattern.compile("(\\+?[0-9]{5,15})")
        val phoneMatcher = phonePattern.matcher(text)
        if (text.contains("কল") || text.contains("call") || text.contains("ডায়াল") || text.contains("ফোন")) {
            if (phoneMatcher.find()) {
                val number = phoneMatcher.group(1) ?: ""
                if (number.length >= 5) {
                    return DeviceAction.CallPhone(number)
                }
            }
        }

        // 2. YouTube ("ইউটিউব ওপেন করো", "গান চালাও", "play ... on youtube", "ইউটিউবে ... সার্চ করো")
        if (text.contains("ইউটিউব") || text.contains("youtube") || text.contains("গান বাজাও") || text.contains("গান চালাও") || text.contains("play song")) {
            val query = text
                .replace("ইউটিউব ওপেন করো", "")
                .replace("ইউটিউবে", "")
                .replace("ইউটিউব", "")
                .replace("গান বাজাও", "")
                .replace("গান চালাও", "")
                .replace("চালিয়ে দাও", "")
                .replace("play", "")
                .replace("on youtube", "")
                .trim()
            return DeviceAction.OpenYouTube(query)
        }

        // 3. Browser / Search ("গুগলে সার্চ করো", "ব্রাউজার খোলো", "ওয়েবসাইটে যাও", "search on google")
        if (text.contains("ব্রাউজার") || text.contains("browser") || text.contains("গুগল") || text.contains("ওয়েবসাইটে") || text.contains("সার্চ করো")) {
            val query = text
                .replace("ব্রাউজার ওপেন করো", "")
                .replace("ব্রাউজারে ঢোকো", "")
                .replace("গুগলে সার্চ করো", "")
                .replace("ওয়েবসাইটে যাও", "")
                .replace("সার্চ করো", "")
                .replace("open browser", "")
                .replace("search", "")
                .trim()
            return DeviceAction.OpenBrowser(query)
        }

        // 4. Camera ("ক্যামেরা ওপেন করো", "ছবি তোলো", "ক্যামেরা খোলো", "take photo", "open camera")
        if (text.contains("ক্যামেরা") || text.contains("ছবি তোলো") || text.contains("camera") || text.contains("take photo")) {
            return DeviceAction.OpenCamera
        }

        // 5. Flashlight / Torch ("টর্চ জ্বালাও", "লাইট অন করো", "টর্চ বন্ধ করো", "flashlight")
        if (text.contains("টর্চ") || text.contains("লাইট") || text.contains("flashlight") || text.contains("torch")) {
            val turnOn = !text.contains("বন্ধ") && !text.contains("off") && !text.contains("নিভিয়ে")
            return DeviceAction.ToggleFlashlight(turnOn)
        }

        // 6. Send SMS ("এসএমএস পাঠাও", "মেসেজ পাঠাও", "send sms")
        if (text.contains("এসএমএস") || text.contains("মেসেজ") || text.contains("sms") || text.contains("message")) {
            if (phoneMatcher.find()) {
                val number = phoneMatcher.group(1) ?: ""
                val msg = text.replace(number, "")
                    .replace("এসএমএস পাঠাও", "")
                    .replace("মেসেজ পাঠাও", "")
                    .replace("send sms", "")
                    .trim()
                return DeviceAction.SendSms(number, if (msg.isEmpty()) "Hello from Archer AI" else msg)
            }
        }

        // 7. Battery ("ব্যাটারি কত", "চার্জ কত", "battery level", "battery status")
        if (text.contains("ব্যাটারি") || text.contains("চার্জ কত") || text.contains("battery")) {
            return DeviceAction.CheckBattery
        }

        // 8. Device Settings / WiFi / Bluetooth ("ওয়াইফাই", "ব্লুটুথ", "সেটিংস খোলো")
        if (text.contains("ওয়াইফাই") || text.contains("wifi")) {
            return DeviceAction.OpenSettings("wifi")
        }
        if (text.contains("ব্লুটুথ") || text.contains("bluetooth")) {
            return DeviceAction.OpenSettings("bluetooth")
        }
        if (text.contains("সেটিংস খোলো") || text.contains("open settings")) {
            return DeviceAction.OpenSettings("general")
        }

        // 9. Task Management ("টাস্ক যোগ করো", "লিস্টে রাখো", "add task")
        if (text.contains("টাস্ক যোগ") || text.contains("কাজ যোগ") || text.contains("add task") || text.contains("লিস্টে রাখো")) {
            val task = text
                .replace("টাস্ক যোগ করো", "")
                .replace("কাজ যোগ করো", "")
                .replace("লিস্টে রাখো", "")
                .replace("add task", "")
                .trim()
            if (task.isNotEmpty()) {
                return DeviceAction.AddTask(task)
            }
        }

        // 10. Memory ("মনে রাখো ...", "remember that ...")
        if (text.contains("মনে রাখো") || text.contains("remember that") || text.contains("মনে রেখো")) {
            val fact = text
                .replace("মনে রাখো যে", "")
                .replace("মনে রাখো", "")
                .replace("মনে রেখো", "")
                .replace("remember that", "")
                .trim()
            if (fact.isNotEmpty()) {
                return DeviceAction.SaveMemory("User Fact", fact)
            }
        }

        // 11. Minimize to Floating Overlay ("মিনিমাইজ করো", "স্ক্রিন শেয়ার মোড", "floating orb", "minimize")
        if (text.contains("মিনিমাইজ") || text.contains("স্ক্রিন শেয়ার") || text.contains("minimize") || text.contains("overlay")) {
            return DeviceAction.MinimizeToOverlay
        }

        // 12. Volume ("ভলিউম বাড়াও", "ভলিউম কমাও", "volume up", "volume down")
        if (text.contains("ভলিউম বাড়াও") || text.contains("volume up") || text.contains("সাউন্ড বাড়াও")) {
            return DeviceAction.AdjustVolume(true)
        }
        if (text.contains("ভলিউম কমাও") || text.contains("volume down") || text.contains("সাউন্ড কমাও")) {
            return DeviceAction.AdjustVolume(false)
        }

        // 13. Current Time ("কয়টা বাজে", "সময় কত", "what time is it")
        if (text.contains("কয়টা বাজে") || text.contains("সময় কত") || text.contains("what time")) {
            return DeviceAction.GetCurrentTime
        }

        // 14. Email ("ইমেইল পাঠাও", "মেইল পাঠাও", "send email")
        if (text.contains("ইমেইল") || text.contains("মেইল") || text.contains("email")) {
            return DeviceAction.SendEmail("", "Message via Archer AI", "Sent from Archer AI assistant.")
        }

        return DeviceAction.None
    }
}
