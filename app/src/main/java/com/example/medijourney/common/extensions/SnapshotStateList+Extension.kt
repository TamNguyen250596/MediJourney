package com.example.medijourney.common.extensions

import androidx.compose.runtime.snapshots.SnapshotStateList

fun <T: Any> SnapshotStateList<T>.replaceAllValueOf(list: List<T>) {
    this.clear()
    this.addAll(list)
}