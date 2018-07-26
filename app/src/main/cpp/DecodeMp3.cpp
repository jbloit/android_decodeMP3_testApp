//
// Created by jbloit on 7/25/18.
//

#include "DecodeMp3.h"


DecodeMp3::DecodeMp3() {

}

DecodeMp3::~DecodeMp3() {

}


void DecodeMp3::decode(char* file) {

    TinyAudioDecoderAndroidExtras platformExtras;
    platformExtras.assetManager = AAssetManager();

    TinyAudioDecoderBuffer dstBuffer;
    int channelDataSize = sizeof(TinyAudioDecoderSampleType) * 2048;

    dstBuffer.channelDataSize = channelDataSize;
    dstBuffer.leftChannelData = malloc(channelDataSize);
    dstBuffer.rightChannelData = malloc(channelDataSize);

    if(decoder.Initialize(file, 44100, true, &dstBuffer, &platformExtras) == TINYAD_RESULT_SUCCESS) {
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

    delete decoder;
}