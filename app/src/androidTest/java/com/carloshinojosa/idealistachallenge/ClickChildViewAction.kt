package com.carloshinojosa.idealistachallenge

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.Matcher

/** ViewAction that clicks a child view with the given [viewId] inside the target item. */
class ClickChildViewAction(private val viewId: Int) : ViewAction {
    override fun getConstraints(): Matcher<View> = isDisplayed()
    override fun getDescription(): String = "Click child view with id $viewId"
    override fun perform(uiController: UiController, view: View) {
        val child = view.findViewById<View>(viewId)
        child.performClick()
        uiController.loopMainThreadUntilIdle()
    }
}
