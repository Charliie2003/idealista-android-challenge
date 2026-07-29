# :feature:list

Android library module for the property listing screen. Uses XML layouts, ViewBinding, `ListAdapter` with `DiffUtil`, and a LiveData-backed ViewModel. Depends only on `:core:domain`; it is completely unaware of Retrofit or Room. Navigation to the detail screen is delegated to `MainActivity` via the `ListingNavigator` interface defined in this module.
