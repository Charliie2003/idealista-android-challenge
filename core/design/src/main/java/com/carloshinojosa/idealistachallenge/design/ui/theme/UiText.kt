package com.carloshinojosa.idealistachallenge.design.ui.theme

/**
 * Represents text that can originate from a dynamic string or a string resource ID.
 * Used to pass displayable messages from ViewModel to UI without leaking Android Context into
 * the domain layer.
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    /** [resId] is an Android string resource ID; [args] are optional format arguments. */
    class StringResource(val resId: Int, vararg val args: Any) : UiText {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StringResource) return false
            return resId == other.resId && args.contentDeepEquals(other.args)
        }
        override fun hashCode(): Int = 31 * resId + args.contentDeepHashCode()
    }
}
