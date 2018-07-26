#include <jni.h>
#include <string>
#include "DecodeMp3.h"

extern "C"
{
JNIEXPORT jstring JNICALL
Java_com_jbloit_decodemp3_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


JNIEXPORT jstring JNICALL
Java_com_jbloit_decodemp3_MainActivity_decodeMp3(
        JNIEnv *env,
        jobject /* this */,
        jstring filename) {

    DecodeMp3 *decoder = new(std::nothrow) DecodeMp3();


    // convert Java string to UTF-8
    jboolean isCopy;
    const char *utf8 = env->GetStringUTFChars(filename, &isCopy);
    assert(NULL != utf8);
    std::string str = std::string(utf8);
    decoder->loadAudioFileSimple(str);
    // see https://stackoverflow.com/questions/5859673/shoul
    // d-you-call-releasestringutfchars-if-getstringutfchars-returned-a-copy
    env->ReleaseStringUTFChars(filename, utf8);


    std::string hello = "Hello from C++";
    DecodeMp3 *decoder = new(std::nothrow) DecodeMp3();

    return env->NewStringUTF(hello.c_str());
}
}