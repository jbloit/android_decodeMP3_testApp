#include <jni.h>
#include <string>

// for native asset manager
#include <sys/types.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>

#include "DecodeMp3.h"

extern "C" {
JNIEXPORT jstring JNICALL
Java_com_jbloit_decodemp3_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


JNIEXPORT void JNICALL
Java_com_jbloit_decodemp3_MainActivity_decodeMp3(
        JNIEnv *env,
        jobject /* this */,
        jobject assetManager,
        jstring filename) {


    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
    assert(NULL != mgr);

    // DEBUG: Just testing we're actually able to read the asset:

//    AAsset* testAsset = AAssetManager_open(mgr, "sinusoid.mp3", AASSET_MODE_UNKNOWN);
//    if (testAsset)
//    {
//        assert(testAsset);
//
//        size_t assetLength = AAsset_getLength(testAsset);
//
//        __android_log_print(ANDROID_LOG_DEBUG, "Native Audio", "Asset file size: %d\n", assetLength);
//
//        char* buffer = (char*) malloc(assetLength + 1);
//        AAsset_read(testAsset, buffer, assetLength);
//        buffer[assetLength] = 0;
//
//        __android_log_print(ANDROID_LOG_INFO, "Test Asset Manager", "The value is %s", buffer);
//
//        AAsset_close(testAsset);
//        free(buffer);
//    }
//    else {
//        __android_log_print(ANDROID_LOG_ERROR, "Test Asset Manager", "Cannot open file");
//    }


    DecodeMp3 *decoder = new(std::nothrow) DecodeMp3();

    // convert Java string to UTF-8
    jboolean isCopy;
    const char *utf8 = env->GetStringUTFChars(filename, &isCopy);
    assert(NULL != utf8);
    std::string str = std::string(utf8);

    decoder->decode(mgr, utf8);

    env->ReleaseStringUTFChars(filename, utf8);

}
}