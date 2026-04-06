package com.doomsday.toolbox.ai

class NativeLlmSession(
    private val configPath: String,
    private val mergedConfigJson: String,
    private val runtimeConfigJson: String
) {
    private var nativePtr: Long = 0L

    companion object {
        init {
            System.loadLibrary("doomllm")
        }
    }

    fun load() {
        if (nativePtr != 0L) return
        nativePtr = nativeCreate(configPath, mergedConfigJson, runtimeConfigJson)
    }

    fun isReady(): Boolean = nativePtr != 0L && nativeIsReady(nativePtr)

    fun generate(prompt: String): String = nativeGenerate(nativePtr, prompt)

    fun reset() {
        if (nativePtr != 0L) nativeReset(nativePtr)
    }

    fun release() {
        if (nativePtr != 0L) {
            nativeRelease(nativePtr)
            nativePtr = 0L
        }
    }

    private external fun nativeCreate(
        configPath: String,
        mergedConfigJson: String,
        runtimeConfigJson: String
    ): Long

    private external fun nativeGenerate(nativePtr: Long, prompt: String): String
    private external fun nativeReset(nativePtr: Long)
    private external fun nativeRelease(nativePtr: Long)
    private external fun nativeIsReady(nativePtr: Long): Boolean
}
