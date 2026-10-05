package com.example.config

/**
 * MM TOOLS CONFIGURATION
 * -------------------------------------------------------------
 * All central URLs, labels, and text configurations are kept here.
 * You can easily edit any of these values without searching through
 * the rest of the project.
 */
object MMToolsConfig {

    // --- APPLICATION IDENTITY ---
    const val APP_NAME = "MM Tools"
    const val APP_TAGLINE = "Utility Browser & Manager"

    // --- WEB NAVIGATION TARGETS ---

    /**
     * Default home URL loaded on app launch or floating FB button click.
     */
    const val FACEBOOK_HOME_URL = "https://www.facebook.com/"

    /**
     * Dedicated URL for creating Facebook accounts.
     */
    const val CREATE_ACCOUNT_URL =
        "https://limited.facebook.com/reg/?is_two_steps_login=0&cid=103&refsrc=deprecated&soft=hjk"

    /**
     * URL directly opening Facebook Two-Factor Authentication (2FA) security settings.
     */
    const val TWO_FACTOR_AUTH_URL =
        "https://accountscenter.facebook.com/password_and_security/two_factor"

    // --- COMMUNITY & UPDATES ---

    /**
     * Telegram Channel URL for user community and app updates.
     * [PLACEHOLDER] Replace this URL with your actual Telegram channel or group link!
     */
    const val TELEGRAM_CHANNEL_URL = "https://t.me/mmtools_official_placeholder"

    // --- FIRST LAUNCH WELCOME POPUP ---
    const val WELCOME_POPUP_TITLE = "Welcome to MM Tools"
    const val WELCOME_POPUP_MESSAGE =
        "Thank you for using our app. Join our Telegram channel to receive updates."
    const val WELCOME_POPUP_BTN_TELEGRAM = "Join Telegram"
    const val WELCOME_POPUP_BTN_CLOSE = "Close"

    // --- BROWSER USER-AGENTS ---

    /**
     * Standard Modern Desktop User-Agent string used when Desktop Mode is enabled.
     */
    const val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
}
