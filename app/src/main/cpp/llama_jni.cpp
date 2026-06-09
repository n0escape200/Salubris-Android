#include <jni.h>
#include <string>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "LLAMA", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "LLAMA", __VA_ARGS__)

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_salubris_LlamaNative_loadModel(
        JNIEnv *env,
        jobject,
        jstring path_) {

    const char *path = env->GetStringUTFChars(path_, 0);
    LOGI("Model path received: %s", path);
    env->ReleaseStringUTFChars(path_, path);

    return JNI_TRUE;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_salubris_LlamaNative_generate(
        JNIEnv *env,
        jobject,
        jstring prompt_) {

    const char *prompt = env->GetStringUTFChars(prompt_, 0);

    std::string response = "LLM not linked yet. Prompt received.";

    env->ReleaseStringUTFChars(prompt_, prompt);

    return env->NewStringUTF(response.c_str());
}