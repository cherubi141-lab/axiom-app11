# AXIOM — Android (WebView) Project

این پروژه، اپ وب AXIOM (فایل `axiom.html`) را داخل یک اپ اندرویدی با `WebView` اجرا می‌کند.
اپلیکیشن وب به‌صورت آفلاین داخل `app/src/main/assets/axiom.html` بسته‌بندی شده و از مسیر
`file:///android_asset/axiom.html` بارگذاری می‌شود — یعنی خود اپ نیازی به هاست جداگانه ندارد
(فقط برای درخواست‌های چت/صوت به Gemini API به اینترنت نیاز دارد، که در مانیفست مجوزش داده شده).

## ساختار
- `app/src/main/assets/axiom.html` — همان فایل HTML شما (بدون تغییر)
- `app/src/main/java/com/axiom/app/MainActivity.kt` — هاست WebView (JS/localStorage فعال،
  دسترسی میکروفون برای دکمه‌ی ورودی صوتی، پشتیبانی از `<input type="file">`، دکمه‌ی Back که
  اول تاریخچه‌ی داخل WebView را برمی‌گرداند)
- `app/src/main/res/mipmap-*` — آیکون اختصاصی AXIOM (طرح مثلث/دلتای درخشان بنفش-مجنتا،
  هم‌رنگ پالت خود اپ) در تمام دانسیتی‌ها + آیکون تطبیقی (Adaptive Icon) برای اندروید ۸+

## چطور APK بسازید (نصب Android Studio لازم است)
1. Android Studio را باز کنید → **File > Open** → پوشه‌ی `AXIOM-Android` را انتخاب کنید.
2. صبر کنید تا Gradle Sync تمام شود (بار اول ممکن است Android Studio خودش نسخه‌ی Gradle و
   SDK لازم را دانلود کند — این پروژه `gradle-wrapper.properties` را دارد اما جار Wrapper را
   ندارد چون این محیط به اینترنت دسترسی نداشت؛ Android Studio به‌صورت خودکار آن را می‌سازد/همگام
   می‌کند، یا اگر خواستید از خط فرمان بسازید، یک‌بار در Android Studio: **File > Sync Project
   with Gradle Files** را بزنید تا wrapper کامل شود).
3. برای APK آزمایشی (debug): منوی **Build > Build App Bundle(s) / APK(s) > Build APK(s)**.
   خروجی در `app/build/outputs/apk/debug/app-debug.apk` قرار می‌گیرد و مستقیم روی گوشی نصب می‌شود.
4. برای APK نهایی (release) با امضای واقعی: **Build > Generate Signed Bundle / APK** و یک
   keystore جدید بسازید (یا از قبلی استفاده کنید).

## نکات مهم
- **applicationId**: `com.axiom.app` — اگر می‌خواهید یکتا باشد (برای انتشار در Play Store)،
  در `app/build.gradle` تغییرش دهید.
- **دسترسی میکروفون**: کد HTML شما از `getUserMedia`/`SpeechRecognition` برای ورودی صوتی استفاده
  می‌کند. `getUserMedia` (ضبط خام صدا) کاملاً در WebView کار می‌کند و مجوزش را از کاربر می‌گیرد.
  اما Web Speech API (`webkitSpeechRecognition` تشخیص گفتار مرورگر) در Android System WebView
  پشتیبانی نمی‌شود (محدودیت شناخته‌شده‌ی گوگل، فقط در Chrome کامل کار می‌کند) — اگر اپ برای
  تبدیل گفتار به متن به همین API متکی است، آن بخش خاص ممکن است در اپ نصب‌شده کار نکند، هرچند
  بقیه‌ی امکانات (چت، ضبط صدا برای TTS خودتان، و غیره) طبیعی کار می‌کنند.
- تمام رنگ‌ها/تم اپ اندرویدی با پالت --void/--purple خود HTML هماهنگ شده (پس‌زمینه‌ی status bar
  و splash هم بنفش تیره است، نه سفید).

## چرا خودم APK نساختم
این محیطی که من در آن کار می‌کنم Android SDK / Gradle / دسترسی اینترنت ندارد، پس نمی‌توانم
پروژه را واقعاً کامپایل کنم. آنچه تحویل داده‌ام یک پروژه‌ی کامل و آماده‌ی build است — با باز
کردنش در Android Studio طبق مراحل بالا، ظرف چند دقیقه APK را می‌گیرید.
