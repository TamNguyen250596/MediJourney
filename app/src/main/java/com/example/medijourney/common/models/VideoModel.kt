package com.example.medijourney.common.models

import android.os.Parcel
import android.os.Parcelable

class VideoModel() : Parcelable {

    // Properties
    var title: String? = null
    var videoTag: String? = null

    // Constructors
    constructor(parcel: Parcel) : this() {
        title = parcel.readString()
        videoTag = parcel.readString()
    }

    // Functions
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(videoTag)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoModel> {
        override fun createFromParcel(parcel: Parcel): VideoModel {
            return VideoModel(parcel)
        }

        override fun newArray(size: Int): Array<VideoModel?> {
            return arrayOfNulls(size)
        }
    }
}
