<div align="center">
  <img width="210" height="auto" src="./.resources/images/ic_launcher.png" alt="Current Activity Logo" border="0">

  # Current Activity

  **🔍 App Activity Inspector: Real-Time Foreground Status Monitor**

  An essential tool for **Android Developers** and **Reverse Engineers**  
  Instantly displays the *package name* and *class name* of the application currently in the foreground.  
  Quickly inspect any active app on your device for development or debugging.

  [![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
  [![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
  [![GitHub release](https://img.shields.io/github/v/release/codehasan/Current-Activity?include_prereleases)](https://github.com/codehasan/Current-Activity/releases)

  <a href='https://play.google.com/store/apps/details?id=io.github.ratul.topactivity'>
    <img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width='200'/>
  </a>

</div>

## ⚠️ Important Note

> **Note:** Google prohibits the usage of **QUERY_ALL_PACKAGES** permission and **AccessibilityService** without a strong explanation. For that reason, the Play Store version of **Current Activity** won't have either of these features. While the app will still function, performance will vary. 
> 
> **💡 Recommendation:** It is strongly recommended to use the latest **Global** version from [**Releases**](https://github.com/codehasan/Current-Activity/releases) rather than from **Play Store**.

## ✨ Key Features

- **📱 Real-Time Tracking:** View the **package name** and the **class name** of the app currently visible to the user (i.e., the top app).
- **🛠️ Development & Debugging:** Quickly verify which components are launched, aid in deep-link development, and confirm third-party app interaction.
- **🔧 Reverse Engineering:** Identify key components and packages in external applications for analysis.

## ⚙️ How It Works

The monitoring is made possible with two main services: **PackageMonitoringService** and **AccessibilityMonitoringService**.

1. **PackageMonitoringService** actively checks for app state changes in the last 10 seconds, doing this every 500ms. Each time a state change is detected and confirmed to be in foreground, the popup window is updated to inform the user.

2. **AccessibilityMonitoringService** complements **PackageMonitoringService** by actively observing window state changes. If a change is detected and confirmed not to be a System class (e.g., `android.view.View`), it immediately updates the popup window.

## 📸 Screenshots

<div align="center">
  <img src="./.resources/images/screenshot_1.jpg" width="160" height="356" alt="Screenshot 1">
  <img src="./.resources/images/screenshot_2.jpg" width="160" height="356" alt="Screenshot 2">
  <img src="./.resources/images/screenshot_3.jpg" width="160" height="356" alt="Screenshot 3">
  <img src="./.resources/images/screenshot_4.jpg" width="160" height="356" alt="Screenshot 4">
  <img src="./.resources/images/screenshot_5.jpg" width="160" height="356" alt="Screenshot 5">
</div>

## 🤝 Contributing

Contributions are what makes the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give this project a star! Thanks again!

1. Fork the project
2. Create your feature branch. (`git checkout -b feature/AmazingFeature`)
3. Commit your changes. (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch. (`git push origin feature/AmazingFeature`)
5. Open a pull request.

## 💡 FAQs

### Unable to enable AccessibilityService for Current Activity?
<details>
<summary>Click to expand for the answer</summary>
<br />

1. Make sure you are using the global version, and not the version from Play Store.
* **Solution:** Download global version from [Releases](https://github.com/codehasan/Current-Activity/releases)
2. In Chinese roms, sideloaded third party apps are initially restricted from using AccessibilityService.
* **Solution:** Watch this video in [YouTube](https://youtube.com/shorts/3mw_3TIF0y4)

**Still having issues?** Please [open an issue here](https://github.com/codehasan/Current-Activity/issues) and include a screen recording or screenshot of the problem.
</details>

---

### Unable to install Current Activity?
<details>
<summary>Click to expand for the answer</summary>
<br />

1. If you can't install from Play Store, download and install from GitHub.
* **Solution:** Download global version from [Releases](https://github.com/codehasan/Current-Activity/releases)
2. If installation is [blocked by Play Protect](./.resources/images/blocked_by_play_protect.jpg),
    1. Open Play Store
    2. Click on your account icon or menu icon and find Play Protect
    3. Open settings in Play Protect and disable all the options

**Still having issues?** Please [open an issue here](https://github.com/codehasan/Current-Activity/issues) and include a screen recording or screenshot of the problem.
</details>

---

### AccessibilityService auto disabled or not working?
<details>
<summary>Click to expand for the answer</summary>
<br />

Some Chinese phones like Xiaomi are known to disable AccessibilityServices automatically.
  1. Open Settings
  2. Search for "autostart"
  3. You may see `Autostart` or `Background autostart` in results, click on it
  4. Find **Current Activity** and enable

Watch these videos for more help,
* Newer versions of Xiaomi - [YouTube](https://youtube.com/shorts/0zE2uj5-Dcg)
* Older versions of Xiaomi - [YouTube](https://youtube.com/shorts/DeBkBQnUH9A)

**Still having issues?** Please [open an issue here](https://github.com/codehasan/Current-Activity/issues) and include a screen recording or screenshot of the problem.
</details>

## 🙏 Credits

- [**Wen**](https://github.com/109021017) for [core resources](https://github.com/109021017/android-TopActivity)
- [**Muhtaseem Al Mahmud**](https://github.com/KingMahmud) for project optimization

## 📄 License

<div align="center">

  [![License](https://www.gnu.org/graphics/gplv3-with-text-136x68.png)](LICENSE)

  Current Activity is licensed under [**GNU General Public License v3.0**](https://www.gnu.org/licenses/gpl-3.0.html) or later.

</div>
