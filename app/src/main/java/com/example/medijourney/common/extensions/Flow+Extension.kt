package com.example.medijourney.common.extensions

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
fun <T> Flow<T>.firstThenDebounce(debounceMillis: Long): Flow<T> {
    var firstEmission = true
    return this.debounce {
        if (firstEmission) {
            firstEmission = false
            0
        } else {
            debounceMillis
        }
    }
}