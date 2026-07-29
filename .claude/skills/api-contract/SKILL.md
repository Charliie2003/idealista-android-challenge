---
name: api-contract
description: Read this before writing any DTO, mapper, or repository. Documents the two Idealista challenge endpoints, their fields, and the three known contract quirks that must be handled defensively.
---

# API Contract

## Endpoints

| Purpose | URL | Method | Static? |
|---|---|---|---|
| Listing | `https://idealista.github.io/android-challenge/list.json` | GET | Yes — always returns the same 4 properties |
| Detail  | `https://idealista.github.io/android-challenge/detail.json` | GET | Yes — always returns detail for `adid: 1` |

Both endpoints return `application/json` with no auth and no rate limiting behavior we need to worry about.

## Listing response

Array of property objects. Fields (all optional unless marked):

| Field | Type | Notes |
|---|---|---|
| `propertyCode` | `String` | **Required.** ID as string (`"1"`, `"2"`, ...). Normalize to `Property.id`. |
| `thumbnail` | `String` (URL) | May be null. Provide placeholder. |
| `floor` | `String` | E.g. `"2"`, `"6"`. |
| `price` | `Double` | **Ignore — see Quirk 1.** |
| `priceInfo.price.amount` | `Double` | **Source of truth for price value.** |
| `priceInfo.price.currencySuffix` | `String` | Either `"€"` (sale) or `"€/mes"` (rent). **Source of truth for display.** |
| `propertyType` | `String` | E.g. `"flat"`. |
| `operation` | `String` | `"sale"` or `"rent"`. |
| `size` | `Double` | Square meters. |
| `exterior` | `Boolean` | |
| `rooms` | `Int` | |
| `bathrooms` | `Int` | |
| `address` | `String` | E.g. `"calle de Lagasca"`. |
| `province`, `municipality`, `district`, `country`, `neighborhood` | `String` | Location fields. |
| `latitude`, `longitude` | `Double` | |
| `description` | `String` | Long text. |
| `multimedia.images[]` | Array | Each has `url: String`, `tag: String` (`"livingRoom"`, `"kitchen"`, ...). |
| `parkingSpace` | Object | **Optional field** — may be absent. |
| `parkingSpace.hasParkingSpace` | `Boolean` | |
| `parkingSpace.isParkingSpaceIncludedInPrice` | `Boolean` | |
| `features.hasAirConditioning` | `Boolean` | |
| `features.hasBoxRoom` | `Boolean` | |
| `features.hasSwimmingPool` | `Boolean` | **Optional** — only in some items. |
| `features.hasTerrace` | `Boolean` | **Optional** — only in some items. |
| `features.hasGarden` | `Boolean` | **Optional** — only in some items. |

## Detail response

Single object. Fields:

| Field | Type | Notes |
|---|---|---|
| `adid` | `Int` | ID as integer. **Always `1` in the static response — see Quirk 2.** |
| `price` | `Double` | Redundant with `priceInfo.amount`. |
| `priceInfo.amount` | `Double` | **Source of truth.** |
| `priceInfo.currencySuffix` | `String` | |
| `operation` | `String` | `"sale"` or `"rent"`. |
| `propertyType`, `extendedPropertyType`, `homeType` | `String` | |
| `state` | `String` | E.g. `"active"`. |
| `multimedia.images[]` | Array | Each has `url`, `tag`, `localizedName`, `multimediaId`. |
| `propertyComment` | `String` | Long HTML-free text with `\n`. |
| `ubication.latitude`, `ubication.longitude` | `Double` | Note: `ubication` (not `location`). |
| `country` | `String` | |
| `moreCharacteristics` | Object | See below. |
| `energyCertification.title` | `String` | |
| `energyCertification.energyConsumption.type` | `String` | Letter grade (`"a"`..`"g"`). |
| `energyCertification.emissions.type` | `String` | Letter grade. |

### `moreCharacteristics` fields

`communityCosts` (Double), `roomNumber` (Int), `bathNumber` (Int), `exterior` (Boolean), `housingFurnitures` (String), `agencyIsABank` (Boolean), `energyCertificationType` (String), `flatLocation` (String), `modificationDate` (Long, epoch millis), `constructedArea` (Int), `lift` (Boolean), `boxroom` (Boolean), `isDuplex` (Boolean), `floor` (String), `status` (String).

---

## The three quirks (handle these defensively)

### Quirk 1 — Price ambiguity

The top-level `price` field and `priceInfo.price.amount` (listing) or `priceInfo.amount` (detail) can disagree.

**Example:** Property `2` has `price: 2750000.0` but `priceInfo.price.amount: 1200.0` with suffix `€/mes`. The property is a rental at €1,200/month; the top-level `price` is either wrong or refers to something else entirely.

**Handling:**
- **Source of truth is always `priceInfo`.**
- The `PropertyDtoMapper` reads `priceInfo.price.amount` and `priceInfo.price.currencySuffix`, ignores top-level `price`.
- Test coverage: `PropertyDtoMapperTest` includes a test named `` `ignores top-level price when priceInfo disagrees` ``.

### Quirk 2 — Detail endpoint is static

The detail endpoint always returns `adid: 1`. If a user opens the detail of property `2`, `3`, or `4`, the endpoint returns data for property `1`.

**Handling:**
- The detail use case (`ObservePropertyDetailUseCase`) requests the endpoint AND loads the base property from the listing repository.
- If `detail.adid.toString() == requestedId`, the detail is used to enrich the base property.
- Otherwise, the base property (from listing) is returned as `PropertyDetail` with the fields we have.
- Documented in `ADR-002 — Static detail endpoint handling`.

This is not a workaround, it is a real product behavior — apps must degrade gracefully when a backend returns partial or stale data.

### Quirk 3 — ID type mismatch

Listing: `propertyCode: String` (`"1"`).
Detail: `adid: Int` (`1`).

**Handling:**
- Domain uses `id: String` universally.
- Detail DTO maps `adid.toString()` when mapping to domain.
- ID equality checks (Quirk 2) always compare as `String`.

---

## Optional fields — the safe way

`parkingSpace`, `features.hasSwimmingPool`, `features.hasTerrace`, `features.hasGarden` are absent from some list entries. Represent them as nullable in the DTO and default to `false` (or `null` for `parkingSpace` whole object) in the domain mapper.

```kotlin
@Serializable
internal data class PropertyDto(
    val propertyCode: String,
    val parkingSpace: ParkingSpaceDto? = null, // absent in some items
    val features: FeaturesDto = FeaturesDto(),
    // ...
)

@Serializable
internal data class FeaturesDto(
    val hasAirConditioning: Boolean = false,
    val hasBoxRoom: Boolean = false,
    val hasSwimmingPool: Boolean = false,
    val hasTerrace: Boolean = false,
    val hasGarden: Boolean = false,
)
```

Providing defaults at the DTO layer avoids null-safety noise in the mapper.

---

## kotlinx.serialization configuration

```kotlin
Json {
    ignoreUnknownKeys = true      // future-proof against new fields
    coerceInputValues = true      // null → default when property has default
    explicitNulls = false         // don't emit nulls back
}
```

## Update policy

If a new quirk is discovered during development, update this document **before** writing the workaround. The document is the contract; the code follows.
