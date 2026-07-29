# :app

Composition root. Hosts `IdealistaApp` (`@HiltAndroidApp`), `MainActivity` (single Activity with `NavHostFragment`), the navigation graph, and all Hilt modules that wire repository implementations to their interfaces. Depends on every other module. No business logic lives here — only wiring and entry points.
