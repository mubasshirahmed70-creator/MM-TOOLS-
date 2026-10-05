package com.example

import com.example.config.MMToolsConfig
import com.example.util.UidExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testConfigUrls_areValid() {
        assertEquals("MM Tools", MMToolsConfig.APP_NAME)
        assertTrue(MMToolsConfig.FACEBOOK_HOME_URL.startsWith("https://"))
        assertTrue(MMToolsConfig.CREATE_ACCOUNT_URL.contains("limited.facebook.com"))
        assertTrue(MMToolsConfig.TWO_FACTOR_AUTH_URL.contains("two_factor"))
        assertTrue(MMToolsConfig.TELEGRAM_CHANNEL_URL.startsWith("https://t.me/"))
    }

    @Test
    fun testUidExtractor_profilePhpParam() {
        val testUrl = "https://m.facebook.com/profile.php?id=100089283746192"
        val uid = UidExtractor.extractFromUrl(testUrl)
        assertEquals("100089283746192", uid)
    }

    @Test
    fun testUidExtractor_peoplePath() {
        val testUrl = "https://www.facebook.com/people/John-Doe/100098765432109"
        val uid = UidExtractor.extractFromUrl(testUrl)
        assertEquals("100098765432109", uid)
    }

    @Test
    fun testUidExtractor_numericPath() {
        val testUrl = "https://www.facebook.com/100012345678901"
        val uid = UidExtractor.extractFromUrl(testUrl)
        assertEquals("100012345678901", uid)
    }

    @Test
    fun testUidExtractor_vanityUsername_returnsNull() {
        // Must never guess or return vanity string as numeric UID
        val testUrl = "https://www.facebook.com/zuck"
        val uid = UidExtractor.extractFromUrl(testUrl)
        assertNull(uid)
    }

    @Test
    fun testUidExtractor_nonFacebookUrl_returnsNull() {
        val testUrl = "https://google.com/profile.php?id=100089283746192"
        val uid = UidExtractor.extractFromUrl(testUrl)
        assertNull(uid)
    }

    @Test
    fun testUidExtractor_cookieExtraction() {
        val cookieHeader = "datr=abc123xyz; sb=def456; c_user=61594950337557; xs=789%3Aabc; fr=012"
        val uid = UidExtractor.extractFromCookieString(cookieHeader)
        assertEquals("61594950337557", uid)
    }

    @Test
    fun testUidExtractor_cookieExtraction_atStartOrEnd() {
        assertEquals("100089283746192", UidExtractor.extractFromCookieString("c_user=100089283746192; datr=123"))
        assertEquals("100089283746192", UidExtractor.extractFromCookieString("datr=123; c_user=100089283746192"))
        assertEquals("100089283746192", UidExtractor.extractFromCookieString("c_user=100089283746192"))
        assertNull(UidExtractor.extractFromCookieString("datr=123; not_c_user=100089283746192"))
        assertNull(UidExtractor.extractFromCookieString(null))
        assertNull(UidExtractor.extractFromCookieString(""))
    }

    @Test
    fun testUidExtractor_sanitizeCandidate() {
        assertEquals("100012345678901", UidExtractor.sanitizeCandidate("\"100012345678901\""))
        assertEquals("100012345678901", UidExtractor.sanitizeCandidate(" 100012345678901 "))
        assertNull(UidExtractor.sanitizeCandidate("zuck"))
        assertNull(UidExtractor.sanitizeCandidate("123")) // Too short for Facebook UID
        assertNull(UidExtractor.sanitizeCandidate(""))
    }

    @Test
    fun testTotpGenerator_rfc6238Vectors() {
        val rfcSecret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"
        // RFC 6238 Test Vector: 59s -> 287082
        val code59 = com.example.util.TotpGenerator.generateCurrentCode(rfcSecret, 59_000L)
        assertEquals("287082", code59)

        // RFC 6238 Test Vector: 1111111109s -> 081804
        val code1111111109 = com.example.util.TotpGenerator.generateCurrentCode(rfcSecret, 1111111109_000L)
        assertEquals("081804", code1111111109)

        // RFC 6238 Test Vector: 1234567890s -> 005924
        val code1234567890 = com.example.util.TotpGenerator.generateCurrentCode(rfcSecret, 1234567890_000L)
        assertEquals("005924", code1234567890)
    }

    @Test
    fun testUsaNamesData_generatesAuthenticNames() {
        val firstNamesCount = com.example.util.UsaNamesData.FIRST_NAMES.size
        val lastNamesCount = com.example.util.UsaNamesData.LAST_NAMES.size
        assertTrue(firstNamesCount >= 200)
        assertTrue(lastNamesCount >= 200)

        val randomPerson = com.example.util.UsaNamesData.getRandomName()
        assertTrue(randomPerson.firstName.isNotBlank())
        assertTrue(randomPerson.lastName.isNotBlank())
        assertEquals("${randomPerson.firstName} ${randomPerson.lastName}", randomPerson.fullName)
    }

    @Test
    fun testRemoteConfigManager_configModel() {
        val config = com.example.util.RemoteConfigManager.AppConfig(
            minRequiredVersion = 2,
            latestVersion = "2.0.0",
            isForceUpdate = true,
            isAppDisabled = false,
            updateTitle = "Please Update",
            downloadUrl = "https://example.com/download"
        )
        assertEquals(2, config.minRequiredVersion)
        assertEquals("2.0.0", config.latestVersion)
        assertTrue(config.isForceUpdate)
        assertFalse(config.isAppDisabled)
        assertEquals("Please Update", config.updateTitle)
        assertEquals("https://example.com/download", config.downloadUrl)
    }
}
