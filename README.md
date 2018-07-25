# android_decodeMP3_testApp

Decodes "sinsusoid.mp3" stored in app resources, and save to external storage in "test/sinusoid.wav".

This is basically a quick hack, putting together these 2 resources:

1. mp3 to audio buffer with [DecoderTest.java](https://android.googlesource.com/platform/cts/+/jb-mr2-release/tests/tests/media/src/android/media/cts/DecoderTest.java)

2. audio buffer to wav file with [WaveFileWriter.java](https://github.com/philburk/jsyn/blob/master/src/com/jsyn/util/WaveFileWriter.java)

See original files if you need to decode other formats.
