package com.example.doan.utils

import java.text.SimpleDateFormat
import java.util.Locale


const val IMAGE_MEDIA = "Images"
const val AUDIO_MEDIA = "Audios"
const val VIDEO_MEDIA = "Videos"
const val DOCUMENT = "Documents"

const val VIEW_ALL = "ALL FILE"
enum class STATUS {PENDING, DOING, DONE, FAIL}
val storageUnits = arrayOf("B", "KB", "MB", "GB")

val formatter = SimpleDateFormat("MMM dd", Locale.ENGLISH)

