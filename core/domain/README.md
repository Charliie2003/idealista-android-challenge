# :core:domain

Pure Kotlin module (no Android SDK dependency). Contains all domain models, repository interfaces, use cases, and shared types such as `Result` and `UiText`. Every other module may depend on this one; this module depends on nothing. Apply changes here only when the domain contract itself changes, not when the data source or UI changes.
