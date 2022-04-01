/*
 * Copyright (c) 2019 EFDIS AG Bankensoftware, Freising <info@efdis.de>.
 *
 * This file is part of the activeTAN app for Android.
 *
 * The activeTAN app is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The activeTAN app is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the activeTAN app.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.efdis.tangenerator;

import android.Manifest;
import android.content.Intent;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.efdis.tangenerator.activetan.HHDkm;
import de.efdis.tangenerator.activetan.KeyMaterialType;
import de.efdis.tangenerator.gui.initialization.AbstractBackgroundTask;
import de.efdis.tangenerator.gui.initialization.InitializeTokenActivity;
import de.efdis.tangenerator.persistence.database.InMemoryDatabaseRule;
import de.efdis.tangenerator.persistence.keystore.BankingKeyComponents;
import de.efdis.tangenerator.screenshot.DayNightRule;
import de.efdis.tangenerator.screenshot.ScreenshotRule;

@RunWith(AndroidJUnit4.class)
public class InitializeTokenActivityTest {

    static final String SERIAL_NUMBER = "XX1234567890";
    static final String WRONG_SERIAL_NUMBER = "XX1234567891";
    static final int LETTER_NUMBER = 1;
    static final int WRONG_LETTER_NUMBER = LETTER_NUMBER + 1;

    static Intent getIntentWithTestData() {
        HHDkm hhdkm = new HHDkm();
        hhdkm.setType(KeyMaterialType.DEMO);
        hhdkm.setAesKeyComponent(new byte[BankingKeyComponents.BANKING_KEY_LENGTH]);
        hhdkm.setLetterNumber(LETTER_NUMBER);

        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                InitializeTokenActivity.class);
        intent.putExtra(InitializeTokenActivity.EXTRA_LETTER_KEY_MATERIAL, hhdkm.getBytes());
        intent.putExtra(InitializeTokenActivity.EXTRA_MOCK_SERIAL_NUMBER, SERIAL_NUMBER);
        return intent;
    }

    @Rule
    public UnlockedDeviceRule unlockedDeviceRule = new UnlockedDeviceRule();

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    @Rule
    public GrantPermissionRule cameraPermissionRule
            = GrantPermissionRule.grant(
                Manifest.permission.CAMERA);

    @Rule
    public InMemoryDatabaseRule mockDatabaseRule
            = InMemoryDatabaseRule.withoutTanGenerators();

    @Rule
    public ScreenshotRule screenshotRule = new ScreenshotRule();

    @Rule
    public DayNightRule dayNightRule = new DayNightRule();

    @Rule
    public ActivityScenarioRule<InitializeTokenActivity> activityScenarioRule
            = new ActivityScenarioRule<>(getIntentWithTestData());

    @Rule
    public RegisterIdlingResourceRule registerIdlingResourceRule = new RegisterIdlingResourceRule(AbstractBackgroundTask.getIdlingResource());

    static void simulatePortalQrCodeInput(ActivityScenario<InitializeTokenActivity> activityScenario) {
        simulatePortalQrCodeInput(activityScenario, SERIAL_NUMBER, LETTER_NUMBER);
    }

    static void simulatePortalQrCodeInput(
            ActivityScenario<InitializeTokenActivity> activityScenario,
            final String serialNumber,
            final int letterNumber) {
        Espresso.onView(ViewMatchers.withId(R.id.cameraPreview))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        final HHDkm hhdkm = new HHDkm();
        hhdkm.setType(KeyMaterialType.PORTAL);
        hhdkm.setAesKeyComponent(new byte[16]);
        hhdkm.setLetterNumber(letterNumber);
        hhdkm.setDeviceSerialNumber(serialNumber);

        activityScenario.onActivity(activity -> activity.onKeyMaterial(hhdkm.getBytes()));
    }

    static void simulateLetterQrCodeInput(ActivityScenario<InitializeTokenActivity> activityScenario) {
        Espresso.onView(ViewMatchers.withId(R.id.cameraPreview))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        final HHDkm hhdkm = new HHDkm();
        hhdkm.setType(KeyMaterialType.LETTER);
        hhdkm.setAesKeyComponent(new byte[16]);
        hhdkm.setLetterNumber(LETTER_NUMBER);

        activityScenario.onActivity(activity -> activity.onKeyMaterial(hhdkm.getBytes()));
    }

    @Test
    @DayNightRule.UiModes({AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.MODE_NIGHT_NO})
    public void takeScreenshots() {
        screenshotRule.captureScreen("initializeTokenStep1");

        Espresso.onView(ViewMatchers.withId(R.id.buttonContinue))
                .perform(ViewActions.click());

        screenshotRule.captureScreen("initializeTokenStep2");

        simulatePortalQrCodeInput(activityScenarioRule.getScenario());

        screenshotRule.captureScreen("initializeTokenStep3");
    }

    @Test
    public void checkHappyPath() {
        Espresso.onView(ViewMatchers.withId(R.id.buttonContinue))
                .perform(ViewActions.click());

        Espresso.pressBack();

        Espresso.onView(ViewMatchers.withId(R.id.buttonContinue))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.buttonContinue))
                .perform(ViewActions.click());

        simulatePortalQrCodeInput(activityScenarioRule.getScenario());

        Espresso.onView(ViewMatchers.withId(R.id.initialTAN))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.pressBack();

        Espresso.onView(ViewMatchers.withText(R.string.initialization_confirm_quit_message))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void checkWrongSerialNumber() {
        Espresso.onView(ViewMatchers.withId(R.id.buttonContinue))
                .perform(ViewActions.click());

        simulatePortalQrCodeInput(activityScenarioRule.getScenario(),
                WRONG_SERIAL_NUMBER, LETTER_NUMBER);

        Espresso.onView(ViewMatchers.withText(R.string.initialization_failed_wrong_serial))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.repeat))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.serialNumber))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.buttonContinue))
                .perform(ViewActions.click());

        simulatePortalQrCodeInput(activityScenarioRule.getScenario());

        Espresso.onView(ViewMatchers.withId(R.id.initialTAN))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void checkWrongLetterNumber() {
        Espresso.onView(ViewMatchers.withId(R.id.buttonContinue))
                .perform(ViewActions.click());

        simulatePortalQrCodeInput(activityScenarioRule.getScenario(),
                SERIAL_NUMBER, WRONG_LETTER_NUMBER);

        if (InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getBoolean(R.bool.email_initialization_enabled)) {
            Espresso.onView(ViewMatchers.withText(R.string.initialization_failed_wrong_email))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        } else {
            Espresso.onView(ViewMatchers.withText(R.string.initialization_failed_wrong_letter))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }

        Espresso.onView(ViewMatchers.withText(R.string.repeat))
                .check(ViewAssertions.doesNotExist());
    }

    @Test
    public void checkScanLetterTwice() {
        Espresso.onView(ViewMatchers.withId(R.id.buttonContinue))
                .perform(ViewActions.click());

        simulateLetterQrCodeInput(activityScenarioRule.getScenario());

        Espresso.onView(ViewMatchers.withText(R.string.initialization_failed_wrong_qr_code))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.repeat))
                .perform(ViewActions.click());

        simulatePortalQrCodeInput(activityScenarioRule.getScenario());

        Espresso.onView(ViewMatchers.withId(R.id.initialTAN))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void checkMultipleTokenHint() {
        Espresso.onView(ViewMatchers.withId(R.id.buttonContinue))
                .perform(ViewActions.click());

        simulatePortalQrCodeInput(activityScenarioRule.getScenario());

        Espresso.onView(ViewMatchers.withText(R.string.initialization_multiple_generators_title))
                .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())));
    }
}
