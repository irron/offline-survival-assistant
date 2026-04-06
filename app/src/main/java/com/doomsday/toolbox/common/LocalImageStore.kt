package com.doomsday.toolbox.common

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object LocalImageStore {
    fun importImage(context: Context, uri: Uri): String {
        val imagesDir = File(context.filesDir, "chat_images").apply { mkdirs() }
        val targetFile = File(imagesDir, "chat_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "无法读取所选图片" }
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
        return targetFile.absolutePath
    }
}
