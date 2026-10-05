package com.example.util

import android.webkit.CookieManager

object UidExtractor {

    private val NUMERIC_UID_REGEX = Regex("^\\d{6,25}$")

    // Matches ...?id=100012345678901 or &id=100012345678901
    private val ID_QUERY_PARAM_REGEX = Regex("[?&]id=(\\d{6,25})(?:&|#|$)", RegexOption.IGNORE_CASE)

    // Matches facebook.com/people/Some-Name/100012345678901
    private val PEOPLE_PATH_REGEX =
        Regex("(?:facebook\\.com|fb\\.com)/people/[^/?#]+/(\\d{6,25})(?:[/?#]|$)", RegexOption.IGNORE_CASE)

    // Matches facebook.com/100012345678901
    private val NUMERIC_PATH_REGEX =
        Regex("(?:facebook\\.com|fb\\.com)/(\\d{6,25})(?:[/?#]|$)", RegexOption.IGNORE_CASE)

    // Matches c_user=100012345678901 in Facebook cookie header
    private val C_USER_COOKIE_REGEX =
        Regex("(?:^|;\\s*)c_user=(\\d{6,25})(?:;|$)", RegexOption.IGNORE_CASE)

    /**
     * Extracts a verified numeric Facebook Profile/Page UID from a URL if present.
     * Returns null if a legitimate numeric UID cannot be reliably extracted.
     */
    fun extractFromUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null

        val lower = url.lowercase()
        if (!lower.contains("facebook.com") && !lower.contains("fb.com")) {
            return null
        }

        // 1. Check ?id= query param (e.g. profile.php?id=100089283746192)
        val paramMatch = ID_QUERY_PARAM_REGEX.find(url)
        if (paramMatch != null) {
            val candidate = paramMatch.groupValues[1]
            if (candidate.matches(NUMERIC_UID_REGEX)) return candidate
        }

        // 2. Check /people/.../UID path
        val peopleMatch = PEOPLE_PATH_REGEX.find(url)
        if (peopleMatch != null) {
            val candidate = peopleMatch.groupValues[1]
            if (candidate.matches(NUMERIC_UID_REGEX)) return candidate
        }

        // 3. Check /UID direct path (e.g. facebook.com/100012345678901)
        val directMatch = NUMERIC_PATH_REGEX.find(url)
        if (directMatch != null) {
            val candidate = directMatch.groupValues[1]
            if (candidate.matches(NUMERIC_UID_REGEX)) return candidate
        }

        return null
    }

    /**
     * Extracts the logged-in Facebook User ID (c_user) from a raw cookie string.
     */
    fun extractFromCookieString(cookieString: String?): String? {
        if (cookieString.isNullOrBlank()) return null
        val match = C_USER_COOKIE_REGEX.find(cookieString)
        return match?.groupValues?.get(1)?.let { sanitizeCandidate(it) }
    }

    /**
     * Queries Android's CookieManager across Facebook domains to retrieve the active
     * logged-in account's numeric User ID (c_user).
     */
    fun extractLoggedInUidFromCookieManager(): String? {
        return try {
            val cookieManager = CookieManager.getInstance()
            val domains = listOf(
                "https://m.facebook.com",
                "https://facebook.com",
                "https://www.facebook.com",
                "https://touch.facebook.com",
                "https://mbasic.facebook.com",
                "https://web.facebook.com"
            )
            for (domain in domains) {
                val cookieStr = cookieManager.getCookie(domain)
                val uid = extractFromCookieString(cookieStr)
                if (!uid.isNullOrBlank()) return uid
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Sanitizes and verifies a UID candidate extracted from DOM meta elements or JS.
     * Strips any surrounding quotes, whitespace, and validates numeric integrity.
     */
    fun sanitizeCandidate(candidate: String?): String? {
        if (candidate.isNullOrBlank()) return null
        val clean = candidate.trim().removeSurrounding("\"").removeSurrounding("'")
        return if (clean.matches(NUMERIC_UID_REGEX)) clean else null
    }

    /**
     * Enhanced JavaScript snippet to inspect:
     * 1. In-page document.cookie for c_user
     * 2. Facebook global JavaScript environment variables (window.Env.USER_ID, window.__user)
     * 3. OpenGraph / App Links meta tags for profile/page numeric ID
     * 4. Canonical profile links and navigation links
     */
    const val JS_EXTRACT_UID_SNIPPET = """
        (function() {
            try {
                // 1. Check document.cookie for logged-in c_user
                var cookieMatch = document.cookie.match(/(?:^|;\s*)c_user=(\d{6,25})(?:;|$)/);
                if (cookieMatch && cookieMatch[1]) return cookieMatch[1];

                // 2. Check Facebook global JS variables
                if (window.Env && window.Env.USER_ID && /^\d{6,25}$/.test(String(window.Env.USER_ID))) {
                    return String(window.Env.USER_ID);
                }
                if (window.__user && /^\d{6,25}$/.test(String(window.__user)) && String(window.__user) !== '0') {
                    return String(window.__user);
                }
                if (window._requireLazy) {
                    try {
                        var cuid = window.require ? window.require('CurrentUserInitialData') : null;
                        if (cuid && cuid.USER_ID && /^\d{6,25}$/.test(String(cuid.USER_ID))) {
                            return String(cuid.USER_ID);
                        }
                    } catch (e) {}
                }

                // 3. Check AppLinks android url meta (contains fb://profile/<uid>)
                var androidUrl = document.querySelector('meta[property="al:android:url"]')?.getAttribute('content') || '';
                var m = androidUrl.match(/fb:\/\/(?:profile|page)\/(\d{6,25})/);
                if (m && m[1]) return m[1];

                // 4. Check AppLinks ios url meta
                var iosUrl = document.querySelector('meta[property="al:ios:url"]')?.getAttribute('content') || '';
                var m2 = iosUrl.match(/fb:\/\/(?:profile|page)\/(\d{6,25})/);
                if (m2 && m2[1]) return m2[1];

                // 5. Check entity_id or profile_id in meta
                var entityMeta = document.querySelector('meta[property="fb:profile_id"]')?.getAttribute('content') ||
                                 document.querySelector('meta[property="fb:pages"]')?.getAttribute('content') || '';
                if (entityMeta && /^\d{6,25}$/.test(entityMeta.trim())) {
                    return entityMeta.trim();
                }

                // 6. Check canonical link for ?id=
                var canonical = document.querySelector('link[rel="canonical"]')?.getAttribute('href') || '';
                if (canonical) {
                    var m3 = canonical.match(/[?&]id=(\d{6,25})/);
                    if (m3 && m3[1]) return m3[1];
                }

                // 7. Check profile links in header or navigation
                var profileLink = document.querySelector('a[href*="/profile.php?id="]');
                if (profileLink) {
                    var m4 = profileLink.href.match(/[?&]id=(\d{6,25})/);
                    if (m4 && m4[1]) return m4[1];
                }
            } catch (e) {}
            return '';
        })();
    """
}
