# FairMoment Launcher

<p align="center">
  <strong>A mindful, customizable, and distraction-free Android Launcher.</strong><br>
  Built with Kotlin & Jetpack Compose • Designed for Digital Wellbeing • 100% Offline-First
</p>

---

## 📖 About FairMoment

**FairMoment** is a modern Android home screen launcher focused on intentional phone usage, clarity, and elegant personalization. It combines distraction-free "Moments" (Zen/Focus modes) with an iOS-style widget ecosystem, smart stacks, native Android app widget hosting, and a categorized app library.

---

## ⚖️ Attribution & Open Source License

This software is an independent derivative work based on the open-source **Fairphone Moments** launcher, originally developed by **FairPhone B.V.**

- **Original Project:** Fairphone Moments (by FairPhone B.V.)
- **License:** [European Union Public Licence v. 1.2 (EUPL-1.2)](LICENSE)
- **Modifications & Enhancements:**
  - Integrated iOS-style Smart Stack & interactive Widget Gallery.
  - Complete Android `AppWidgetHost` & `AppWidgetManager` integration allowing any 3rd-party installed app widget on the home screen.
  - High-performance in-memory caching for installed apps, icons (`LruCache`), and widget metadata to eliminate UI frame drops.
  - Quick Notes and customizable feed tiles with persistent memory caching.
  - Custom wallpaper background engine supporting solid tones, gradients, and custom gallery photos with dynamic Dim & Blur adjustments.
  - Minute-aligned CPU wakeups reducing background battery and CPU consumption.

*Disclaimer: Fairphone is a registered trademark of Fairphone B.V. FairMoment is an independent community project and is neither endorsed by nor directly affiliated with Fairphone B.V.*

---

## ✨ Key Features

- **🧘 Moments (Focus & Zen Profiles):** Create distinct home screen modes (Work, Essentials, Zen, Social) with customized allowed apps and contacts.
- **📱 Smart Stack & Widgets:**
  - Multi-card stackable widgets with vertical swipe interaction.
  - Battery, Screen Time, Weather, Calendar, Quick Notes, Daily Focus, and News/Feed cards.
  - **Native 3rd-party Widget Support:** Host any installed Android application's widget seamlessly inside stacks or standalone.
- **📂 Categorized App Library:**
  - Automated categories (Productivity, Social, Media, Tools, etc.)
  - Real-time instant search by app name or package.
- **🎨 Wallpaper & Theming:**
  - Material You dynamic coloring support (Android 12+).
  - Custom wallpapers with real-time Dim Alpha and Blur controls.
- **🔒 Private & Offline-First:**
  - No ads, no third-party tracking, no telemetries, no data leaves your device.

---

## 🛠️ Building the Project

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- JDK 17 / 21
- Android SDK Platform 36 (Android 15/16)

### Compile Debug APK
```bash
./gradlew assembleDebug
```

### Build Android App Bundle (AAB) for Google Play
```bash
./gradlew bundleRelease
```

---

## 📄 License

This project is licensed under the **European Union Public Licence (EUPL-1.2)**.  
See the [LICENSE](LICENSE) file for the full license text.
