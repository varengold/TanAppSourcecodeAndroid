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

import androidx.appcompat.app.AppCompatDelegate;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.efdis.tangenerator.gui.settings.SettingsActivity;
import de.efdis.tangenerator.persistence.database.BankingTokenUsage;
import de.efdis.tangenerator.persistence.database.InMemoryDatabaseRule;
import de.efdis.tangenerator.screenshot.DayNightRule;
import de.efdis.tangenerator.screenshot.ScreenshotRule;

@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {

    @Rule
    public UnlockedDeviceRule unlockedDeviceRule = new UnlockedDeviceRule();

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    @Rule
    public InMemoryDatabaseRule mockDatabaseRule
            = InMemoryDatabaseRule.withSingleTanGenerator(BankingTokenUsage.DISABLED_AUTH_PROMPT);

    @Rule
    public ScreenshotRule screenshotRule = new ScreenshotRule();

    @Rule
    public DayNightRule dayNightRule = new DayNightRule();

    @Rule
    public ActivityScenarioRule<SettingsActivity> activityScenarioRule
            = new ActivityScenarioRule<>(SettingsActivity.class);

    @Test
    @DayNightRule.UiModes({AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.MODE_NIGHT_NO})
    public void takeScreenshots() {
        Espresso.onView(ViewMatchers.withId(R.id.bankingTokenRecyclerView))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.exhausted_generator_description))
                .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())));

        screenshotRule.captureScreen("settings");
    }

}
