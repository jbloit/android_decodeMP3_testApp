//
// Created by jbloit on 7/25/18.
//

#ifndef ANDROID_DECODEMP3_TESTAPP_DECODEMP3_H
#define ANDROID_DECODEMP3_TESTAPP_DECODEMP3_H

#include "TinyAudioDecoder_Android.h"
#include "TinyAudioDecoder.h"

class DecodeMp3 {
public:
    DecodeMp3();
    ~DecodeMp3();

    TinyAudioDecoder decoder = TinyAudioDecoder();

    void decode();

};


#endif //ANDROID_DECODEMP3_TESTAPP_DECODEMP3_H
