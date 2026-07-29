# :core:database

Android library module responsible for all local persistence. Hosts the Room database, entity definitions, DAOs, and `Entity → Domain` mappers. The favorites table schema is defined here. Exposes local data source implementations that `:app` wires into repository implementations via Hilt. Feature modules must never depend on this module directly.
