#include <jni.h>
#include "string"

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In RnMainActivity.java:
//    static {
//       System.loadLibrary("dqd2022");
//    }
//
// Or, in RnMainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("dqd2022")
//      }
//    }
extern "C"
JNIEXPORT jstring JNICALL
Java_com_dqd2022_MainActivity_getHelloWorldFromCpp(JNIEnv *env, jobject thiz) {
    // TODO: implement getHelloWorldFromCpp()
    std::string str = "哈喽沃尔玛德";
    return env->NewStringUTF(str.c_str());
}