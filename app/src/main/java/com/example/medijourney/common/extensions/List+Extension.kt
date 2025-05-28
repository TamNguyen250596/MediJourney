package com.example.medijourney.common.extensions

import androidx.compose.runtime.snapshots.SnapshotStateList

fun <T: Any> List<T>.replaceAllValueOf(stateList: SnapshotStateList<T>) {
    stateList.clear()
    stateList.addAll(this)
}