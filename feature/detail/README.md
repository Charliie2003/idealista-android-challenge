# :feature:detail

Android library module for the property detail screen. Uses a Fragment as host with a `ComposeView` that renders Composables. State is exposed via a StateFlow-backed ViewModel. Depends only on `:core:domain`; it is completely unaware of Retrofit or Room. Receives `propertyId: String` from the nav graph argument via `DetailFragmentArgs`.
