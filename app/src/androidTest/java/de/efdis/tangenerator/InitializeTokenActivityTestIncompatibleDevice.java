/*
 * Copyright (c) 2020 EFDIS AG Bankensoftware, Freising <info@efdis.de>.
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

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.efdis.tangenerator.gui.initialization.AbstractBackgroundTask;
import de.efdis.tangenerator.gui.initialization.InitializeTokenActivity;
import de.efdis.tangenerator.persistence.database.InMemoryDatabaseRule;

@RunWith(AndroidJUnit4.class)
public class InitializeTokenActivityTestIncompatibleDevice {

    static Intent getIntentWithTestData() {
        Intent intent = InitializeTokenActivityTest.getIntentWithTestData();

        intent.putExtra(InitializeTokenActivity.EXTRA_ENFORCE_COMPATIBILITY_MODE, true);
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
    public ActivityScenarioRule<InitializeTokenActivity> activityScenarioRule
            = new ActivityScenarioRule<>(getIntentWithTestData());

    @Rule
    public RegisterIdlingResourceRule registerIdlingResourceRule = new RegisterIdlingResourceRule(AbstractBackgroundTask.getIdlingResource());

    @Test
    public void checkCompatibilityMode() {
        Espresso.onView(ViewMatchers.withText(R.string.initialization_mandatory_user_auth_description))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(android.R.string.ok))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.buttonContinue))
                .perform(ViewActions.click());

        InitializeTokenActivityTest.simulatePortalQrCodeInput(activityScenarioRule.getScenario());

        // To compute the initial TAN, an authorization request is performed automatically.

        Espresso.onView(ViewMatchers.withId(R.id.initialTAN))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.pressBack();

        Espresso.onView(ViewMatchers.withText(R.string.initialization_confirm_quit_message))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

}
