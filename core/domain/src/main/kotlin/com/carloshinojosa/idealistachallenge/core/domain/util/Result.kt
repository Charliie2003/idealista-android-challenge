package com.carloshinojosa.idealistachallenge.core.domain.util

import com.carloshinojosa.idealistachallenge.core.domain.error.DomainError

/**
 * Custom result type. Preferred over [kotlin.Result] because [DomainError] is not a [Throwable]
 * subtype, and exhaustive `when` over Success/Error is cleaner than isSuccess/isFailure.
 */
sealed interface Result<out T> {
    data class Success<out T>(val data: T) : Result<T>
    data class Error(val error: DomainError) : Result<Nothing>
}

fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error   -> this
}

fun <T> Result<T>.getOrNull(): T? = (this as? Result.Success)?.data
