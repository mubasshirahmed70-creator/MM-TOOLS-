# MM Tools - Native Android Application

**MM Tools** is a native Android utility browser built with Kotlin and Jetpack Compose. It features a compact 6-icon top toolbar inspired by the layout of HBX TEAM with custom MM Tools dark navy, blue, and cyan branding, a full-featured WebView for Facebook, a floating Facebook Home button, one-click 2FA and registration shortcuts, safe numeric UID extraction, and complete data clearing capabilities.

---

## Features

1. **Compact 6-Icon Top Toolbar**:
   - **Desktop Mode**: One-tap toggle switching between standard mobile browsing and modern desktop user-agent.
   - **Create Account**: Instantly navigates to the registration URL (`https://limited.facebook.com/reg/?is_two_steps_login=0&cid=103&refsrc=deprecated&soft=hjk`).
   - **Copy UID**: Safely and reliably copies verified Facebook numeric profile/page IDs directly from profile URLs or AppLinks metadata. Never guesses or copies unverified IDs.
   - **2FA Security**: Quick shortcut directly to Facebook Accounts Center Two-Factor Authentication security settings.
   - **Clear Data**: Confirmation dialog to securely purge WebView cookies, cache, local WebStorage, and browsing history.
   - **Reload**: Refreshes the currently loaded page.
2. **Floating Facebook Button**:
   - Quick-access floating action button in the bottom-right corner to return to Facebook Home (`https://www.facebook.com/`).
3. **Session Persistence**:
   - Cookies and sessions are retained locally across app launches without extracting or transmitting credentials.
4. **First-Launch Welcome Popup**:
   - Greets users on initial startup with a Telegram community join button and close option.
5. **Centralized Configuration**:
   - All URLs, text labels, and popup content are centralized in `app/src/main/java/com/example/config/MMToolsConfig.kt` for effortless customization.

---

## Central Configuration (`MMToolsConfig.kt`)

All primary URLs and application strings are defined in a single file:
`app/src/main/java/com/example/config/MMToolsConfig.kt`

```kotlin
object MMToolsConfig {
    const val APP_NAME = "MM Tools"
    const val APP_TAGLINE = "Utility Browser & Manager"

    // Facebook Navigation URLs
    const val FACEBOOK_HOME_URL = "https://www.facebook.com/"
    const val CREATE_ACCOUNT_URL =
        "https://limited.facebook.com/reg/?is_two_steps_login=0&cid=103&refsrc=deprecated&soft=hjk"
    const val TWO_FACTOR_AUTH_URL =
        "https://accountscenter.facebook.com/password_and_security/two_factor"

    // Community / Telegram URL (Replace with your actual link)
    const val TELEGRAM_CHANNEL_URL = "https://t.me/mmtools_official_placeholder"

    // First Launch Welcome Popup
    const val WELCOME_POPUP_TITLE = "Welcome to MM Tools"
    const val WELCOME_POPUP_MESSAGE =
        "Thank you for using our app. Join our Telegram channel to receive updates."
    const val WELCOME_POPUP_BTN_TELEGRAM = "Join Telegram"
    const val WELCOME_POPUP_BTN_CLOSE = "Close"

    // User-Agent for Desktop Mode
    const val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
}
```

---

## Project Structure & Generated Files

```
├── .github/
│   └── workflows/
│       └── build-apk.yml               # Automated cloud build workflow for GitHub Actions
├── app/
│   ├── build.gradle.kts                # App module build configuration
│   ├── proguard-rules.pro              # Proguard configuration
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml     # Permissions (INTERNET, ACCESS_NETWORK_STATE) & Activity
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt     # Main entry point with Edge-to-Edge and MMToolsTheme
│       │   │   ├── config/
│       │   │   │   └── MMToolsConfig.kt # Central URLs and text configuration
│       │   │   ├── ui/
│       │   │   │   ├── MMToolsScreen.kt # Scaffold layout combining Toolbar, WebView & FAB
│       │   │   │   ├── MMToolsViewModel.kt # State management, UID extraction & dialog controls
│       │   │   │   ├── components/
│       │   │   │   │   ├── CompactToolbar.kt # Compact 6-action toolbar with MM branding
│       │   │   │   │   ├── MMWebView.kt      # Native WebView with desktop mode & safe browsing
│       │   │   │   │   ├── FloatingFacebookButton.kt # Floating action button for FB home
│       │   │   │   │   ├── WelcomeDialog.kt  # First-launch welcome popup
│       │   │   │   │   └── ClearDataDialog.kt # Destructive action confirmation dialog
│       │   │   │   └── theme/
│       │   │   │       ├── Color.kt          # Dark navy, blue, and cyan palette
│       │   │   │       ├── Theme.kt          # MMToolsTheme Material 3 theme wrapper
│       │   │   │       └── Type.kt           # Typography definitions
│       │   │   └── util/
│       │   │       ├── PreferencesManager.kt # First-launch & preference persistence
│       │   │       └── UidExtractor.kt       # Reliable numeric UID regex & DOM extraction
│       │   └── res/
│       │       ├── drawable/
│       │       │   ├── ic_launcher_background.xml # Navy gradient background
│       │       │   └── ic_launcher_foreground.xml # Geometric MM shield foreground
│       │       ├── mipmap-anydpi-v26/
│       │       │   ├── ic_launcher.xml
│       │       │   └── ic_launcher_round.xml
│       │       ├── mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ # Custom raster launcher PNGs
│       │       └── values/
│       │           ├── colors.xml
│       │           ├── strings.xml           # Localized app strings
│       │           └── themes.xml
│       └── test/java/com/example/
│           ├── ExampleUnitTest.kt          # Unit tests verifying UID extraction & config
│           └── ExampleRobolectricTest.kt   # Local JVM test for app name and resources
├── build.gradle.kts                    # Root build configuration
├── gradle/
│   └── libs.versions.toml              # Version catalog
├── gradle.properties
├── metadata.json                       # Platform metadata
├── settings.gradle.kts                 # Project name: MM Tools
└── README.md                           # Documentation
```

---

## How to Build the APK in GitHub Actions (Cloud Build)

You can build the APK in the cloud without needing Android Studio or a PC:

1. **Push or Upload the Repository to GitHub**:
   - Ensure the repository contains the `.github/workflows/build-apk.yml` file.
2. **Open the Actions Tab**:
   - On GitHub (using Chrome/Firefox on your Android phone or computer), navigate to your repository.
   - Click on the **Actions** tab at the top.
3. **Trigger Workflow**:
   - Select **Build MM Tools Debug APK** on the left.
   - Click **Run workflow** -> Select `main` branch -> Click the green **Run workflow** button.
   - (The workflow also triggers automatically whenever you push code).
4. **Download the APK Artifact**:
   - When the workflow completes (green checkmark), tap on the workflow run.
   - Scroll down to the **Artifacts** section at the bottom.
   - Tap **mm-tools-debug-apk** to download the ZIP file containing `app-debug.apk`.
   - On your Android phone, extract the ZIP and install `app-debug.apk`.

---

## How to Upload All Files to GitHub from an Android Phone

If you do not have a computer, follow these simple steps on your Android device:

### Option 1: Export ZIP and Upload via Mobile Browser (Desktop Site)
1. In Google AI Studio, tap the **Project Menu (three dots or gear icon)** in the top right.
2. Select **Export as ZIP** or **Download Project**.
3. On your phone, open your browser (Chrome/Brave) and go to [github.com](https://github.com).
4. In browser settings, check **Desktop site**.
5. Tap **New repository**, enter `MM-Tools`, and choose Public or Private.
6. Once created, tap **uploading an existing file** link on the empty repository page.
7. Select the project files from your download folder and commit changes.

### Option 2: Push Directly Using AI Studio's GitHub Integration
1. In the AI Studio top toolbar, click the **Git / GitHub** button.
2. Authorize your GitHub account if prompted.
3. Select **Create new repository** or push directly to an existing repository.
4. The workflow in `.github/workflows/build-apk.yml` will automatically start building your APK!
