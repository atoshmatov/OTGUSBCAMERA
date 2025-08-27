# OTG USB Camera RTMP Streaming App

📹 Android ilovasi OTG orqali ulangan **USB UVC kameradan video oqimini olish** va uni **RTMP serverga uzatish** uchun yozilgan.  
Ilova ichida **video preview** hamda **Foreground Service** orqali RTMP streaming qo‘llab-quvvatlanadi.  

---

## ✨ Xususiyatlar
- OTG orqali ulangan UVC kamerani aniqlash va ulash
- Kameradan **real-time video preview** ko‘rsatish
- **RTMP serverga live stream** uzatish
- Foreground Service orqali background’da ishlash
- Streamni boshlash/to‘xtatish uchun oddiy UI tugma
- Runtime permissionlar (`Camera`, `Microphone`, `Notifications`) bilan ishlash
- Lifecycle-aware service binding (`lifecycleScope`) yordamida xavfsiz servis boshqaruvi

---

## 📂 Loyihaning asosiy qismi
Ushbu loyiha **asl ilova** asosida qayta yozildi va quyidagi GitHub loyihasidan foydalangan:  
🔗 [android-uvc-rtmp-stream (alejandrorosas)](https://github.com/alejandrorosas/android-uvc-rtmp-stream.git)  

O‘zgarishlar:
- UI **Jetpack Compose** asosida yozilgan
- Service lifecycle boshqaruvi yaxshilangan
- Android 14+ permission talablariga moslashtirilgan
- Kutilmagan xatoliklarni oldini olish uchun `ServiceConnection` xavfsiz tekshirish qo‘shilgan

---

## 🛠️ Texnologiyalar
- **Kotlin**
- **Jetpack Compose** (UI uchun)
- **Foreground Service** (background streaming uchun)
- **Lifecycle Service Binding** (`lifecycleScope`)
- **libusb** va **libuvc** (UVC camera access uchun, C++/JNI orqali)
- [AndroidUSBCamera](https://github.com/saki4510t/UVCCamera) OTG orqali kamera oqimini olish uchun
- **RTMP** protokoli[RTMP](https://github.com/pedroSG94/RootEncoder) (video oqimini uzatish uchun)

---

## 📱 Talablar
- Android 8.0 (API 26) yoki undan yuqori
- OTG qo‘llab-quvvatlovchi Android qurilma
- UVC mos keluvchi USB kamera
- RTMP server (misol: `rtmp://your-server/live/stream`)

---

## 🚀 Ishga tushirish
1. Loyihani klon qiling:
   ```bash
   git clone https://github.com/USERNAME/otg-usb-camera-rtmp.git
2. Android Studio orqali oching
3. Qurilmangizni OTG orqali UVC kameraga ulang
4. RTMP server manzilini konfiguratsiya qiling
5. Build qilib, ilovani ishga tushiring

## 📖 Ruxsatlar
Ilova quyidagi ruxsatlarni talab qiladi:
- CAMERA
- RECORD_AUDIO
- FOREGROUND_SERVICE_CAMERA
- FOREGROUND_SERVICE_MICROPHONE
- POST_NOTIFICATIONS (Android 13+ uchun)

## 📸 Foydalanish
- OTG kamera ulangandan so‘ng ilova avtomatik aniqlaydi.
- "Start Stream" tugmasi orqali RTMP oqimni boshlash mumkin.
- "Stop Stream" tugmasi orqali oqimni to‘xtatish mumkin.

## 📚 Asosiy manbalar
Ushbu loyiha [android-uvc-rtmp-stream (alejandrorosas)](https://github.com/alejandrorosas/android-uvc-rtmp-stream.git)
kutubxonasi asosida yozilgan va qayta ishlab chiqilgan.

