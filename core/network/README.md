# :core:network

Android library module responsible for all remote data access. Hosts Retrofit service interfaces, OkHttp client configuration, response DTOs, and `Dto → Domain` mappers. Exposes remote data source implementations that `:app` wires into repository implementations via Hilt. Feature modules must never depend on this module directly.
