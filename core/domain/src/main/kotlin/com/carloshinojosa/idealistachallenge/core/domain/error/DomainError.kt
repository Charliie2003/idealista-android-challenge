package com.carloshinojosa.idealistachallenge.core.domain.error

/** Typed domain errors that propagate from data sources to the UI layer. */
sealed class DomainError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    object Network : DomainError("Network error")
    data class Http(val code: Int) : DomainError("HTTP $code")
    object Parse : DomainError("Parse error")
    data class Unknown(override val cause: Throwable) : DomainError(cause = cause)
    object NotFound : DomainError("Property not found")
}
