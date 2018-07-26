//
// Created by jbloit on 7/25/18.
//

#include "DecodeMp3.h"
#include "string.h"
#include "logging_macros.h"
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

DecodeMp3::DecodeMp3() {
    decoder = new TinyAudioDecoder();
}

DecodeMp3::~DecodeMp3() {
    delete decoder;
}

void DecodeMp3::decode(AAssetManager* assetManager, const char* audioFile) {

    TinyAudioDecoderAndroidExtras platformExtras;
    platformExtras.assetManager = assetManager;

//     Set channel data size to 0 to read all frames at once
    TinyAudioDecoderBuffer dstBuffer;
    dstBuffer.channelDataSize = 0;
    dstBuffer.leftChannelData = NULL;
    dstBuffer.rightChannelData = NULL;

    int frameCount = 0;

    if(decoder->Initialize(audioFile, 44100, true, &dstBuffer, &platformExtras) == TINYAD_RESULT_SUCCESS) {
        int frameCount = 0;
        do {
            TINYAD_RESULT result = decoder->ReadFrames(frameCount);

            if(result == TINYAD_RESULT_SUCCESS) {
                // Copy frames to a circular buffer or do something with the data
                // dstBuffer.leftChannelData and dstBuffer.rightChannelData
                LOGD("Got %d frames of audio data", frameCount);
            }
            else {
                if(result == TINYAD_RESULT_EOF) {
                    LOGD("End of file reached");
                }
                else {
                    LOGD("Error %d occured", result);
                }
                break;
            }
        } while(frameCount > 0);
    }

    free(dstBuffer.leftChannelData);
    free(dstBuffer.rightChannelData);
}