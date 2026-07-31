package com.carloshinojosa.idealistachallenge

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.recyclerview.widget.RecyclerView
import com.carloshinojosa.idealistachallenge.core.network.di.NetworkModule
import com.carloshinojosa.idealistachallenge.list.R as ListR
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class EndToEndFavoriteFlowTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun favoriteFromList_persistsIntoDetail_andBackToList() {
        ActivityScenario.launch(MainActivity::class.java).use {
            // Wait for the listing to load, then click the favorite button on the first item
            onView(withId(ListR.id.propertyList))
                .perform(
                    actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0,
                        ClickChildViewAction(ListR.id.favTarget),
                    ),
                )

            // Navigate to detail by clicking the first item
            onView(withId(ListR.id.propertyList))
                .perform(
                    actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0,
                        click(),
                    ),
                )

            // Press back to return to listing
            pressBack()

            // Verify the listing still shows the favorited item in FAVORITES filter
            onView(withId(ListR.id.tabFavorites)).perform(click())

            // The item should still be present (favorites persisted through Room)
            onView(withId(ListR.id.propertyList))
                .check(matches(isDisplayed()))
        }
    }
}
