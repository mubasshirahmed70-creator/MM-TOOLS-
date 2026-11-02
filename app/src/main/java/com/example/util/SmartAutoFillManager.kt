package com.example.util

import android.content.Context
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom

/**
 * Clean, single-action Smart Auto-Fill & Auto-Next Manager for Facebook Registration.
 *
 * Key Guarantees:
 * - Instant start as soon as form elements appear in DOM.
 * - Full Bloks-compatible pointer/mouse/click sequence on a SINGLE target element (no double loop).
 * - Strict once-per-step lock (`completedSteps`) so "Next", "Not now", and "I agree" are clicked
 *   strictly ONCE, keeping the Facebook Lite OTP confirmation bridge 100% intact.
 * - Zero @JavascriptInterface globals exposed on `window`.
 */
object SmartAutoFillManager {

    private val secureRandom = SecureRandom()

    data class AutoFillProfile(
        val firstName: String,
        val lastName: String,
        val gender: String, // "male" or "female"
        val day: Int,
        val month: Int,
        val year: Int,
        val password: String
    ) {
        fun toJsonObject(): JSONObject {
            return JSONObject().apply {
                put("firstName", firstName)
                put("lastName", lastName)
                put("gender", gender)
                put("day", day)
                put("month", month)
                put("year", year)
                put("password", password)
            }
        }
    }

    /**
     * Generates a guaranteed unique USA Name (filtered against recent history in PreferencesManager),
     * a realistic adult birth date (1994..2004), and loads the user's permanently saved password.
     */
    fun generateRandomProfile(context: Context? = null): AutoFillProfile {
        val prefs = context?.let { PreferencesManager(it) }
        val person = UsaNamesData.getRandomName(prefs)
        val day = secureRandom.nextInt(28) + 1 // 1..28 valid for all months
        val month = secureRandom.nextInt(12) + 1 // 1..12
        val year = secureRandom.nextInt(11) + 1994 // 1994..2004
        val savedPassword = prefs?.savedAutoFillPassword ?: PreferencesManager.DEFAULT_AUTOFILL_PASSWORD
        return AutoFillProfile(
            firstName = person.firstName,
            lastName = person.lastName,
            gender = person.gender,
            day = day,
            month = month,
            year = year,
            password = savedPassword
        )
    }

    /**
     * Injects the auto-fill observer into the current page using a fresh batch of unique profiles
     * and the user's current permanently saved password.
     */
    fun injectAutoFillObserver(webView: WebView?, context: Context? = null) {
        if (webView == null) return
        val prefs = context?.let { PreferencesManager(it) }
        val savedPassword = prefs?.savedAutoFillPassword ?: PreferencesManager.DEFAULT_AUTOFILL_PASSWORD

        // Pre-generate 5 guaranteed-unique profiles for this page session
        val profilesArray = JSONArray()
        repeat(5) {
            profilesArray.put(generateRandomProfile(context).toJsonObject())
        }

        val script = buildObserverScript(
            profilesJson = profilesArray.toString(),
            savedPasswordJson = JSONObject.quote(savedPassword)
        )
        webView.evaluateJavascript(script, null)
    }

    val AUTO_FILL_OBSERVER_JS: String
        get() = buildObserverScript(
            profilesJson = JSONArray().put(generateRandomProfile(null).toJsonObject()).toString(),
            savedPasswordJson = JSONObject.quote(PreferencesManager.DEFAULT_AUTOFILL_PASSWORD)
        )

    private fun buildObserverScript(profilesJson: String, savedPasswordJson: String): String = """
        (function() {
            var incomingProfiles = $profilesJson;
            var latestSavedPassword = $savedPasswordJson;

            // If observer is already active on this document, update password & run check immediately
            if (window.__mmState) {
                window.__mmState.savedPassword = latestSavedPassword;
                if (!window.__mmState.profileQueue || window.__mmState.profileQueue.length === 0) {
                    window.__mmState.profileQueue = incomingProfiles || [];
                }
                if (window.__mmState.runCheck) {
                    window.__mmState.runCheck();
                }
                return;
            }

            var state = {
                profileQueue: incomingProfiles || [],
                currentProfile: (incomingProfiles && incomingProfiles.length > 0) ? incomingProfiles[0] : null,
                savedPassword: latestSavedPassword,
                completedSteps: {},
                pendingTimer: null,
                stoppedAtOtp: false,
                runCheck: null
            };
            window.__mmState = state;

            function pullNextUniqueProfile() {
                if (state.profileQueue.length > 0) {
                    state.currentProfile = state.profileQueue.shift();
                }
                if (!state.currentProfile) {
                    state.currentProfile = {
                        firstName: 'James',
                        lastName: 'Anderson',
                        gender: 'male',
                        day: 14,
                        month: 6,
                        year: 1998,
                        password: state.savedPassword
                    };
                }
                state.currentProfile.password = state.savedPassword;
                return state.currentProfile;
            }

            function setNativeValue(el, value) {
                if (!el) return false;
                try {
                    el.focus();
                    var proto = Object.getPrototypeOf(el);
                    var descriptor = Object.getOwnPropertyDescriptor(proto, 'value');
                    if (!descriptor && el.tagName === 'INPUT') {
                        descriptor = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value');
                    } else if (!descriptor && el.tagName === 'SELECT') {
                        descriptor = Object.getOwnPropertyDescriptor(window.HTMLSelectElement.prototype, 'value');
                    }
                    if (descriptor && descriptor.set) {
                        descriptor.set.call(el, String(value));
                    } else {
                        el.value = String(value);
                    }
                    el.dispatchEvent(new Event('keydown', { bubbles: true }));
                    el.dispatchEvent(new Event('keypress', { bubbles: true }));
                    el.dispatchEvent(new Event('input', { bubbles: true }));
                    el.dispatchEvent(new Event('keyup', { bubbles: true }));
                    el.dispatchEvent(new Event('change', { bubbles: true }));
                    el.dispatchEvent(new Event('blur', { bubbles: true }));
                    return true;
                } catch (e) {
                    return false;
                }
            }

            // Fires Bloks-compatible pointer/mouse/click sequence on ONE target element only (no double loop)
            function clickSingleTarget(el) {
                if (!el) return false;
                try {
                    var target = el.closest('[role="button"], button, a, input[type="submit"], input[type="button"], [tabindex="0"], label') || el;
                    target.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, cancelable: true }));
                    target.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, cancelable: true }));
                    target.dispatchEvent(new PointerEvent('pointerup', { bubbles: true, cancelable: true }));
                    target.dispatchEvent(new MouseEvent('mouseup', { bubbles: true, cancelable: true }));
                    target.click();
                    return true;
                } catch (e) {
                    try {
                        el.click();
                        return true;
                    } catch (e2) {
                        return false;
                    }
                }
            }

            function isVisible(el) {
                if (!el) return false;
                if (el.disabled || el.type === 'hidden') return false;
                var style = window.getComputedStyle(el);
                if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') return false;
                var rect = el.getBoundingClientRect();
                return rect.width > 8 && rect.height > 8;
            }

            function getFieldHint(el) {
                if (!el) return '';
                var parts = [
                    el.name || '',
                    el.id || '',
                    el.placeholder || '',
                    el.getAttribute('aria-label') || '',
                    el.getAttribute('title') || '',
                    el.getAttribute('autocomplete') || ''
                ];
                if (el.id) {
                    var lbl = document.querySelector('label[for="' + el.id.replace(/"/g, '\\"') + '"]');
                    if (lbl) parts.push(lbl.innerText || lbl.textContent || '');
                }
                var parentLabel = el.closest('label');
                if (parentLabel) {
                    parts.push(parentLabel.innerText || parentLabel.textContent || '');
                }
                var prev = el.previousElementSibling;
                if (prev && (prev.tagName === 'LABEL' || prev.tagName === 'SPAN' || prev.tagName === 'DIV')) {
                    parts.push(prev.innerText || prev.textContent || '');
                }
                return parts.join(' ').toLowerCase();
            }

            // Checks ONLY visible inputs to see if the Mobile Number / Email input box is currently shown
            function isContactPointInputVisible(allInputs) {
                for (var i = 0; i < allInputs.length; i++) {
                    var inp = allInputs[i];
                    var type = (inp.type || 'text').toLowerCase();
                    var hint = getFieldHint(inp);
                    if (inp.name === 'email' && document.querySelector('input[name="pass"]')) continue;
                    if (
                        inp.name === 'reg_email__' ||
                        inp.name === 'contactpoint' ||
                        type === 'email' ||
                        hint.indexOf('mobile number') !== -1 ||
                        hint.indexOf('phone number') !== -1 ||
                        hint.indexOf('email address') !== -1 ||
                        hint.indexOf('মোবাইল নম্বর') !== -1 ||
                        hint.indexOf('ফোন নম্বর') !== -1 ||
                        hint.indexOf('ইমেইল') !== -1 ||
                        hint.indexOf('ইমেল') !== -1
                    ) {
                        return true;
                    }
                }
                return false;
            }

            // Checks if the 5-Digit Confirmation Code (OTP) input box is currently visible
            function isOtpCodeInputVisible(allInputs) {
                for (var i = 0; i < allInputs.length; i++) {
                    var inp = allInputs[i];
                    var hint = getFieldHint(inp);
                    if (
                        inp.name === 'c' ||
                        inp.name === 'code' ||
                        inp.getAttribute('autocomplete') === 'one-time-code' ||
                        hint.indexOf('confirmation code') !== -1 ||
                        hint.indexOf('enter code') !== -1 ||
                        hint.indexOf('5-digit') !== -1 ||
                        hint.indexOf('নিশ্চিতকরণ কোড') !== -1 ||
                        hint.indexOf('কোডটি লিখুন') !== -1
                    ) {
                        return true;
                    }
                }
                return false;
            }

            function findButtonByLabels(exactLabels) {
                var allElements = Array.from(
                    document.querySelectorAll('button, input[type="submit"], input[type="button"], [role="button"], [tabindex="0"], a, span, div')
                ).filter(isVisible);

                for (var L = 0; L < exactLabels.length; L++) {
                    var targetLabel = exactLabels[L].toLowerCase();
                    for (var i = allElements.length - 1; i >= 0; i--) {
                        var el = allElements[i];
                        var txt = (el.innerText || el.textContent || '').trim().toLowerCase();
                        var val = (el.value || '').trim().toLowerCase();
                        var aria = (el.getAttribute('aria-label') || '').trim().toLowerCase();
                        if (txt === targetLabel || val === targetLabel || aria === targetLabel) {
                            return el;
                        }
                    }
                }
                return null;
            }

            function scheduleSingleStepClick(stepKey, labels, delayMs) {
                if (state.completedSteps[stepKey]) return;
                if (state.pendingTimer) return;

                state.pendingTimer = setTimeout(function() {
                    state.pendingTimer = null;
                    if (state.completedSteps[stepKey] || state.stoppedAtOtp) return;

                    var visibleInputs = Array.from(document.querySelectorAll('input')).filter(isVisible);
                    if (isOtpCodeInputVisible(visibleInputs)) {
                        state.stoppedAtOtp = true;
                        return;
                    }
                    if (isContactPointInputVisible(visibleInputs)) {
                        return;
                    }

                    var btn = findButtonByLabels(labels);
                    if (btn) {
                        // Lock this step BEFORE clicking so it is strictly clicked ONCE
                        state.completedSteps[stepKey] = true;
                        clickSingleTarget(btn);
                    }
                }, delayMs || 480);
            }

            function __mmRunSmartAutoFill() {
                try {
                    var allInputs = Array.from(document.querySelectorAll('input')).filter(isVisible);
                    var fullPageText = (document.body ? (document.body.innerText || '') : '').toLowerCase();

                    // Stop immediately if the 5-Digit Confirmation Code (OTP) box is visible
                    if (isOtpCodeInputVisible(allInputs)) {
                        state.stoppedAtOtp = true;
                        return;
                    }
                    state.stoppedAtOtp = false;

                    // Pause if the Mobile Number / Email input box is currently visible on screen
                    var contactPointVisible = isContactPointInputVisible(allInputs);
                    if (contactPointVisible) {
                        return;
                    }

                    // =====================================================
                    // 1. NAME STEP (FIRST NAME & LAST NAME)
                    // =====================================================
                    var firstNameInput = null;
                    var lastNameInput = null;
                    var fullNameInput = null;

                    for (var i = 0; i < allInputs.length; i++) {
                        var inp = allInputs[i];
                        var type = (inp.type || 'text').toLowerCase();
                        if (type !== 'text' && type !== 'search' && type !== '') continue;
                        var hint = getFieldHint(inp);

                        if (!firstNameInput && (
                            inp.name === 'firstname' ||
                            hint.indexOf('firstname') !== -1 ||
                            hint.indexOf('first name') !== -1 ||
                            hint.indexOf('first_name') !== -1 ||
                            hint.indexOf('given-name') !== -1 ||
                            hint.indexOf('forename') !== -1 ||
                            hint.indexOf('নামের প্রথম অংশ') !== -1 ||
                            hint.indexOf('প্রথম নাম') !== -1
                        )) {
                            firstNameInput = inp;
                            continue;
                        }

                        if (!lastNameInput && (
                            inp.name === 'lastname' ||
                            hint.indexOf('lastname') !== -1 ||
                            hint.indexOf('last name') !== -1 ||
                            hint.indexOf('last_name') !== -1 ||
                            hint.indexOf('surname') !== -1 ||
                            hint.indexOf('family-name') !== -1 ||
                            hint.indexOf('পদবি') !== -1 ||
                            hint.indexOf('নামের শেষ অংশ') !== -1 ||
                            hint.indexOf('শেষ নাম') !== -1
                        )) {
                            lastNameInput = inp;
                            continue;
                        }

                        if (!fullNameInput && (
                            inp.name === 'fullname' ||
                            hint.indexOf('full name') !== -1 ||
                            hint.indexOf('পুর্ণ নাম') !== -1 ||
                            hint.indexOf('পুরো নাম') !== -1
                        )) {
                            fullNameInput = inp;
                        }
                    }

                    // Fallback for Bloks Name step where 2 text inputs are visible under "What's your name?"
                    if (!firstNameInput && !lastNameInput) {
                        var textInputs = allInputs.filter(function(el) {
                            var t = (el.type || 'text').toLowerCase();
                            return t === 'text' || t === '';
                        });
                        var isNameHeading = fullPageText.indexOf("what's your name") !== -1 ||
                                            fullPageText.indexOf("what is your name") !== -1 ||
                                            fullPageText.indexOf("আপনার নাম কী") !== -1 ||
                                            fullPageText.indexOf("আপনার নাম কি") !== -1 ||
                                            (fullPageText.indexOf("first name") !== -1 && (fullPageText.indexOf("surname") !== -1 || fullPageText.indexOf("last name") !== -1)) ||
                                            (fullPageText.indexOf("নামের প্রথম অংশ") !== -1 && fullPageText.indexOf("পদবি") !== -1);
                        if (isNameHeading && textInputs.length >= 2) {
                            firstNameInput = textInputs[0];
                            lastNameInput = textInputs[1];
                        }
                    }

                    if (firstNameInput && lastNameInput) {
                        if (!firstNameInput.dataset.mmFilled && !firstNameInput.value.trim()) {
                            // Starting a fresh registration: reset step locks and pull a unique profile
                            state.completedSteps = {};
                            var newProf = pullNextUniqueProfile();
                            firstNameInput.dataset.mmFilled = 'true';
                            lastNameInput.dataset.mmFilled = 'true';
                            setNativeValue(firstNameInput, newProf.firstName);
                            setNativeValue(lastNameInput, newProf.lastName);
                        }
                        if (firstNameInput.value.trim() && lastNameInput.value.trim()) {
                            scheduleSingleStepClick(
                                'step_name_next',
                                ['next', 'পরবর্তী', 'continue', 'চালিয়ে যান', 'এগিয়ে যান', 'এগিয়ে যান'],
                                480
                            );
                        }
                        return;
                    }

                    if (fullNameInput) {
                        if (!fullNameInput.dataset.mmFilled && !fullNameInput.value.trim()) {
                            state.completedSteps = {};
                            var newProfFull = pullNextUniqueProfile();
                            fullNameInput.dataset.mmFilled = 'true';
                            setNativeValue(fullNameInput, newProfFull.firstName + ' ' + newProfFull.lastName);
                        }
                        if (fullNameInput.value.trim()) {
                            scheduleSingleStepClick(
                                'step_name_next',
                                ['next', 'পরবর্তী', 'continue', 'চালিয়ে যান', 'এগিয়ে যান', 'এগিয়ে যান'],
                                480
                            );
                        }
                        return;
                    }

                    var profile = state.currentProfile || pullNextUniqueProfile();

                    // =====================================================
                    // 2. DATE OF BIRTH (BIRTHDAY) STEP
                    // =====================================================
                    var allSelects = Array.from(document.querySelectorAll('select')).filter(isVisible);
                    var daySelect = null;
                    var monthSelect = null;
                    var yearSelect = null;

                    for (var s = 0; s < allSelects.length; s++) {
                        var sel = allSelects[s];
                        var sHint = getFieldHint(sel);
                        var optCount = sel.options ? sel.options.length : 0;

                        if (!daySelect && (sel.name === 'birthday_day' || sel.id === 'day' || sHint.indexOf('day') !== -1 || sHint.indexOf('দিন') !== -1)) {
                            daySelect = sel;
                            continue;
                        }
                        if (!monthSelect && (sel.name === 'birthday_month' || sel.id === 'month' || sHint.indexOf('month') !== -1 || sHint.indexOf('মাস') !== -1)) {
                            monthSelect = sel;
                            continue;
                        }
                        if (!yearSelect && (sel.name === 'birthday_year' || sel.id === 'year' || sHint.indexOf('year') !== -1 || sHint.indexOf('বছর') !== -1 || sHint.indexOf('সাল') !== -1)) {
                            yearSelect = sel;
                            continue;
                        }

                        if (optCount >= 12) {
                            var hasYear1998 = false;
                            for (var o = 0; o < sel.options.length; o++) {
                                if (sel.options[o].value === '1998' || (sel.options[o].text && sel.options[o].text.trim() === '1998')) {
                                    hasYear1998 = true;
                                    break;
                                }
                            }
                            if (hasYear1998 && !yearSelect) yearSelect = sel;
                            else if (optCount >= 28 && optCount <= 33 && !daySelect) daySelect = sel;
                            else if (optCount >= 12 && optCount <= 14 && !monthSelect) monthSelect = sel;
                        }
                    }

                    function selectOptionSmart(sel, targetVal) {
                        if (!sel) return false;
                        var strVal = String(targetVal);
                        var paddedVal = targetVal < 10 ? ('0' + strVal) : strVal;
                        for (var idx = 0; idx < sel.options.length; idx++) {
                            var opt = sel.options[idx];
                            if (opt.value === strVal || opt.value === paddedVal || opt.text.trim() === strVal || opt.text.trim() === paddedVal) {
                                sel.selectedIndex = idx;
                                setNativeValue(sel, opt.value);
                                return true;
                            }
                        }
                        if (targetVal >= 1 && targetVal < sel.options.length) {
                            sel.selectedIndex = targetVal;
                            setNativeValue(sel, sel.options[targetVal].value);
                            return true;
                        }
                        return false;
                    }

                    if (daySelect && monthSelect && yearSelect) {
                        if (!daySelect.dataset.mmFilled) {
                            daySelect.dataset.mmFilled = 'true';
                            monthSelect.dataset.mmFilled = 'true';
                            yearSelect.dataset.mmFilled = 'true';
                            selectOptionSmart(daySelect, profile.day);
                            selectOptionSmart(monthSelect, profile.month);
                            selectOptionSmart(yearSelect, profile.year);
                        }
                        scheduleSingleStepClick(
                            'step_dob_next',
                            ['next', 'পরবর্তী', 'continue', 'চালিয়ে যান', 'এগিয়ে যান', 'এগিয়ে যান'],
                            480
                        );
                        return;
                    }

                    // Check for <input type="date"> or Birthday inputs
                    var filledDobInput = false;
                    var hasDobInputOnScreen = false;
                    for (var k = 0; k < allInputs.length; k++) {
                        var dInp = allInputs[k];
                        var dType = (dInp.type || '').toLowerCase();
                        var dHint = getFieldHint(dInp);

                        if (dType === 'date') {
                            hasDobInputOnScreen = true;
                            if (!dInp.dataset.mmFilled) {
                                dInp.dataset.mmFilled = 'true';
                                var mm = profile.month < 10 ? ('0' + profile.month) : String(profile.month);
                                var dd = profile.day < 10 ? ('0' + profile.day) : String(profile.day);
                                setNativeValue(dInp, profile.year + '-' + mm + '-' + dd);
                                filledDobInput = true;
                            }
                        } else if (dInp.name === 'birthday_day' || dHint.indexOf('bday-day') !== -1) {
                            hasDobInputOnScreen = true;
                            if (!dInp.dataset.mmFilled) {
                                dInp.dataset.mmFilled = 'true';
                                setNativeValue(dInp, profile.day);
                                filledDobInput = true;
                            }
                        } else if (dInp.name === 'birthday_month' || dHint.indexOf('bday-month') !== -1) {
                            hasDobInputOnScreen = true;
                            if (!dInp.dataset.mmFilled) {
                                dInp.dataset.mmFilled = 'true';
                                setNativeValue(dInp, profile.month);
                                filledDobInput = true;
                            }
                        } else if (dInp.name === 'birthday_year' || dHint.indexOf('bday-year') !== -1) {
                            hasDobInputOnScreen = true;
                            if (!dInp.dataset.mmFilled) {
                                dInp.dataset.mmFilled = 'true';
                                setNativeValue(dInp, profile.year);
                                filledDobInput = true;
                            }
                        } else if (dInp.name === 'age' || dHint.indexOf('your age') !== -1 || dHint.indexOf('বয়স') !== -1 || dHint.indexOf('বয়স') !== -1) {
                            hasDobInputOnScreen = true;
                            if (!dInp.dataset.mmFilled) {
                                dInp.dataset.mmFilled = 'true';
                                setNativeValue(dInp, 2026 - profile.year);
                                filledDobInput = true;
                            }
                        } else if (
                            dHint.indexOf('birthday') !== -1 ||
                            dHint.indexOf('date of birth') !== -1 ||
                            dHint.indexOf('birth date') !== -1 ||
                            dHint.indexOf('জন্ম তারিখ') !== -1 ||
                            dHint.indexOf('জন্মদিন') !== -1
                        ) {
                            hasDobInputOnScreen = true;
                            if (!dInp.dataset.mmFilled) {
                                dInp.dataset.mmFilled = 'true';
                                var mmStr = profile.month < 10 ? ('0' + profile.month) : String(profile.month);
                                var ddStr = profile.day < 10 ? ('0' + profile.day) : String(profile.day);
                                setNativeValue(dInp, mmStr + '/' + ddStr + '/' + profile.year);
                                filledDobInput = true;
                            }
                        }
                    }

                    if (hasDobInputOnScreen || filledDobInput) {
                        scheduleSingleStepClick(
                            'step_dob_next',
                            ['next', 'পরবর্তী', 'continue', 'চালিয়ে যান', 'এগিয়ে যান', 'এগিয়ে যান'],
                            480
                        );
                        return;
                    }

                    // =====================================================
                    // 3. GENDER STEP (MALE / FEMALE)
                    // =====================================================
                    var wantFemale = profile.gender === 'female';
                    var targetSexVal = wantFemale ? '1' : '2';
                    var allRadios = Array.from(document.querySelectorAll('input[type="radio"]')).filter(isVisible);
                    var matchedRadio = null;

                    for (var r = 0; r < allRadios.length; r++) {
                        var rad = allRadios[r];
                        var rHint = getFieldHint(rad);
                        var rVal = (rad.value || '').trim();
                        if ((rad.name === 'sex' || rad.name === 'gender') && rVal === targetSexVal) {
                            matchedRadio = rad;
                            break;
                        }
                        if (wantFemale && (rHint.indexOf('female') !== -1 || rHint.indexOf('মহিলা') !== -1 || rHint.indexOf('নারী') !== -1)) {
                            matchedRadio = rad;
                            break;
                        }
                        if (!wantFemale && ((rHint.indexOf('male') !== -1 && rHint.indexOf('female') === -1) || rHint.indexOf('পুরুষ') !== -1)) {
                            matchedRadio = rad;
                            break;
                        }
                    }

                    if (matchedRadio) {
                        if (!matchedRadio.dataset.mmFilled) {
                            allRadios.forEach(function(item) { item.dataset.mmFilled = 'true'; });
                            matchedRadio.checked = true;
                            clickSingleTarget(matchedRadio);
                            matchedRadio.dispatchEvent(new Event('input', { bubbles: true }));
                            matchedRadio.dispatchEvent(new Event('change', { bubbles: true }));
                        }
                        scheduleSingleStepClick(
                            'step_gender_next',
                            ['next', 'পরবর্তী', 'continue', 'চালিয়ে যান', 'এগিয়ে যান', 'এগিয়ে যান'],
                            480
                        );
                        return;
                    }

                    var isGenderStepPage = allInputs.length === 0 && (
                        fullPageText.indexOf("what's your gender") !== -1 ||
                        fullPageText.indexOf("what is your gender") !== -1 ||
                        fullPageText.indexOf("আপনার লিঙ্গ") !== -1 ||
                        (fullPageText.indexOf("female") !== -1 && fullPageText.indexOf("male") !== -1 && fullPageText.indexOf("custom") !== -1) ||
                        (fullPageText.indexOf("মহিলা") !== -1 && fullPageText.indexOf("পুরুষ") !== -1)
                    );
                    if (isGenderStepPage) {
                        if (!state.completedSteps['filled_gender_bloks']) {
                            var roleRadios = Array.from(document.querySelectorAll('[role="radio"], label, [role="button"]')).filter(isVisible);
                            for (var rr = 0; rr < roleRadios.length; rr++) {
                                var elRadio = roleRadios[rr];
                                var txt = (elRadio.innerText || elRadio.textContent || elRadio.getAttribute('aria-label') || '').trim().toLowerCase();
                                var isMatch = wantFemale
                                    ? (txt === 'female' || txt.indexOf('female\n') === 0 || txt === 'মহিলা' || txt === 'নারী')
                                    : (txt === 'male' || txt.indexOf('male\n') === 0 || txt === 'পুরুষ');
                                if (isMatch) {
                                    state.completedSteps['filled_gender_bloks'] = true;
                                    clickSingleTarget(elRadio);
                                    break;
                                }
                            }
                        }
                        if (state.completedSteps['filled_gender_bloks']) {
                            scheduleSingleStepClick(
                                'step_gender_next',
                                ['next', 'পরবর্তী', 'continue', 'চালিয়ে যান', 'এগিয়ে যান', 'এগিয়ে যান'],
                                480
                            );
                        }
                        return;
                    }

                    // =====================================================
                    // 4. PASSWORD STEP (POST-MOBILE NUMBER)
                    // =====================================================
                    var isRegularLoginPage = Boolean(
                        document.querySelector('input[name="email"]') &&
                        document.querySelector('input[name="pass"]') &&
                        location.href.indexOf('/reg') === -1 &&
                        location.href.indexOf('/r.php') === -1
                    );

                    if (!isRegularLoginPage) {
                        var passInput = null;
                        for (var p = 0; p < allInputs.length; p++) {
                            var pInp = allInputs[p];
                            var pType = (pInp.type || '').toLowerCase();
                            var pHint = getFieldHint(pInp);
                            if (
                                pType === 'password' ||
                                pInp.name === 'reg_passwd__' ||
                                pInp.id === 'password_step_input' ||
                                pHint.indexOf('new-password') !== -1 ||
                                pHint.indexOf('password') !== -1 ||
                                pHint.indexOf('পাসওয়ার্ড') !== -1 ||
                                pHint.indexOf('পাসওয়ার্ড') !== -1
                            ) {
                                passInput = pInp;
                                break;
                            }
                        }

                        if (passInput) {
                            if (!passInput.dataset.mmFilled && !passInput.value.trim()) {
                                passInput.dataset.mmFilled = 'true';
                                setNativeValue(passInput, state.savedPassword);
                            }
                            if (passInput.value.trim()) {
                                scheduleSingleStepClick(
                                    'step_password_next',
                                    ['next', 'পরবর্তী', 'continue', 'চালিয়ে যান', 'এগিয়ে যান', 'এগিয়ে যান', 'sign up', 'সাইন আপ'],
                                    480
                                );
                            }
                            return;
                        }
                    }

                    // =====================================================
                    // 5. "SAVE YOUR LOGIN INFO?" -> CLICK "NOT NOW" ONCE
                    // =====================================================
                    var isSaveLoginInfoPage = allInputs.length === 0 && (
                        fullPageText.indexOf('save your login info') !== -1 ||
                        fullPageText.indexOf("won't need to enter it next time") !== -1 ||
                        fullPageText.indexOf('লগইন তথ্য সেভ') !== -1 ||
                        fullPageText.indexOf('লগইনের তথ্য সেভ') !== -1
                    );
                    if (isSaveLoginInfoPage) {
                        scheduleSingleStepClick(
                            'step_save_login_not_now',
                            ['not now', 'এখন নয়', 'এখন নয়', 'skip', 'এড়িয়ে যান'],
                            480
                        );
                        return;
                    }

                    // =====================================================
                    // 6. "TERMS AND POLICIES" -> CLICK "I AGREE" ONCE
                    // =====================================================
                    var isTermsPage = allInputs.length === 0 && (
                        fullPageText.indexOf('terms and policies') !== -1 ||
                        fullPageText.indexOf('finish signing up') !== -1 ||
                        fullPageText.indexOf('শর্তাবলী') !== -1 ||
                        fullPageText.indexOf('সাইন আপ শেষ করুন') !== -1 ||
                        fullPageText.indexOf('i agree') !== -1 ||
                        fullPageText.indexOf('আমি সম্মত') !== -1
                    );
                    if (isTermsPage) {
                        scheduleSingleStepClick(
                            'step_terms_agree',
                            ['i agree', 'agree', 'আমি সম্মত', 'সম্মত', 'sign up', 'সাইন আপ'],
                            550
                        );
                        return;
                    }
                } catch (err) {}
            }

            state.runCheck = __mmRunSmartAutoFill;
            __mmRunSmartAutoFill();

            var debounceTimer = null;
            var observer = new MutationObserver(function() {
                if (state.stoppedAtOtp) return;
                if (debounceTimer) clearTimeout(debounceTimer);
                debounceTimer = setTimeout(__mmRunSmartAutoFill, 200);
            });
            if (document.documentElement) {
                observer.observe(document.documentElement, {
                    childList: true,
                    subtree: true
                });
            }
            setInterval(function() {
                if (!state.stoppedAtOtp) {
                    __mmRunSmartAutoFill();
                }
            }, 600);
        })();
    """.trimIndent()
}
