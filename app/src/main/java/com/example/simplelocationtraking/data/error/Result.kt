package com.example.simplelocationtraking.data.error

typealias RootError = Error
sealed interface Result<out D, out E:RootError> {

    data class Success<out D>(val success:D) : Result<D,Nothing>
    data class Error<out E:RootError>(val error:E) : Result<Nothing,E>

}