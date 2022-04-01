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

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.efdis.tangenerator.gui.transaction.VerifyTransactionDetailsActivity;
import de.efdis.tangenerator.persistence.database.BankingTokenUsage;
import de.efdis.tangenerator.persistence.database.InMemoryDatabaseRule;

/**
 * Test TAN generation with multiple tokens, i. e., the user has to select a token.
 */
@RunWith(AndroidJUnit4.class)
public class VerifyTransactionDetailsActivityTestMultipleTokens {

    @Rule
    public UnlockedDeviceRule unlockedDeviceRule = new UnlockedDeviceRule();

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    @Rule
    public InMemoryDatabaseRule mockDatabaseRule
            = InMemoryDatabaseRule.withMultipleTanGenerators(BankingTokenUsage.ENABLED_AUTH_PROMPT);

    @Rule
    public ActivityScenarioRule<VerifyTransactionDetailsActivity> activityScenarioRule
            = new ActivityScenarioRule<>(VerifyTransactionDetailsActivityTest.getIntentWithTestData());

    @Test
    public void selectTokenAndComputeTan() {
        Espresso.onView(ViewMatchers.withId(R.id.validateButton))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.choose_token_title))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(android.R.string.ok))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.generatedTanContainer))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void cancelTokenSelection() {
        Espresso.onView(ViewMatchers.withId(R.id.validateButton))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.choose_token_title))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(android.R.string.cancel))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.generatedTanContainer))
                .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())));

        Espresso.onView(ViewMatchers.withId(R.id.validateButton))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

}
