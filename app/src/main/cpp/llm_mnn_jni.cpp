#include <jni.h>

#include <string>

#include "llm_session.h"
#include "nlohmann/json.hpp"

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_doomsday_toolbox_ai_NativeLlmSession_nativeCreate(
        JNIEnv* env,
        jobject /* thiz */,
        jstring configPath,
        jstring mergedConfigJson,
        jstring runtimeConfigJson) {
    const char* configPathChars = env->GetStringUTFChars(configPath, nullptr);
    const char* mergedConfigChars = env->GetStringUTFChars(mergedConfigJson, nullptr);
    const char* runtimeConfigChars = env->GetStringUTFChars(runtimeConfigJson, nullptr);

    auto* session = new doom::LlmSession(
            configPathChars == nullptr ? "" : configPathChars,
            nlohmann::json::parse(mergedConfigChars == nullptr ? "{}" : mergedConfigChars),
            nlohmann::json::parse(runtimeConfigChars == nullptr ? "{}" : runtimeConfigChars));

    if (configPathChars != nullptr) {
        env->ReleaseStringUTFChars(configPath, configPathChars);
    }
    if (mergedConfigChars != nullptr) {
        env->ReleaseStringUTFChars(mergedConfigJson, mergedConfigChars);
    }
    if (runtimeConfigChars != nullptr) {
        env->ReleaseStringUTFChars(runtimeConfigJson, runtimeConfigChars);
    }

    if (!session->load()) {
        const std::string error = session->getLastError();
        delete session;
        jclass illegalState = env->FindClass("java/lang/IllegalStateException");
        if (illegalState != nullptr) {
            env->ThrowNew(illegalState, error.c_str());
        }
        return 0;
    }

    return reinterpret_cast<jlong>(session);
}

JNIEXPORT jstring JNICALL
Java_com_doomsday_toolbox_ai_NativeLlmSession_nativeGenerate(
        JNIEnv* env,
        jobject /* thiz */,
        jlong nativePtr,
        jstring prompt) {
    auto* session = reinterpret_cast<doom::LlmSession*>(nativePtr);
    if (session == nullptr) {
        return env->NewStringUTF("模型尚未加载");
    }

    const char* promptChars = env->GetStringUTFChars(prompt, nullptr);
    const std::string response = session->generate(promptChars == nullptr ? "" : promptChars);
    if (promptChars != nullptr) {
        env->ReleaseStringUTFChars(prompt, promptChars);
    }
    return env->NewStringUTF(response.c_str());
}

JNIEXPORT void JNICALL
Java_com_doomsday_toolbox_ai_NativeLlmSession_nativeReset(
        JNIEnv* /* env */,
        jobject /* thiz */,
        jlong nativePtr) {
    auto* session = reinterpret_cast<doom::LlmSession*>(nativePtr);
    if (session != nullptr) {
        session->reset();
    }
}

JNIEXPORT void JNICALL
Java_com_doomsday_toolbox_ai_NativeLlmSession_nativeRelease(
        JNIEnv* /* env */,
        jobject /* thiz */,
        jlong nativePtr) {
    delete reinterpret_cast<doom::LlmSession*>(nativePtr);
}

JNIEXPORT jboolean JNICALL
Java_com_doomsday_toolbox_ai_NativeLlmSession_nativeIsReady(
        JNIEnv* /* env */,
        jobject /* thiz */,
        jlong nativePtr) {
    auto* session = reinterpret_cast<doom::LlmSession*>(nativePtr);
    return session != nullptr && session->isReady() ? JNI_TRUE : JNI_FALSE;
}

}  // extern "C"
