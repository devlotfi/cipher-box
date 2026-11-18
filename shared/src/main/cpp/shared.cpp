#include <jni.h>
#include <android/log.h>
#include <vector>
#include "TinyProtocolFd.h"

#define LOG_TAG "TinyProtoJNI"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {

    JavaVM *g_vm = nullptr;

// One protocol instance + a handle back to the Kotlin object that owns it
    struct Session {
        tinyproto::FdD proto;
        jobject owner = nullptr;          // global ref to the Kotlin TinyProto object
        jmethodID onFrameId = nullptr;    // TinyProto.onNativeFrame(int, byte[])
        explicit Session(int bufSize) : proto(bufSize) {
        }
    };

// Gets a JNIEnv for the current thread (attaches the thread if necessary)
    struct EnvScope {
        JNIEnv *env = nullptr;
        bool attached = false;

        EnvScope() {
            if (g_vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) == JNI_EDETACHED) {
                if (g_vm->AttachCurrentThread(&env, nullptr) == JNI_OK) attached = true;
            }
        }

        ~EnvScope() {
            if (attached) g_vm->DetachCurrentThread();
        }
    };

// Called by tinyproto when a complete frame has been received.
// Runs on whichever thread called run_rx() (i.e. inside nativeFeed).
    void onReceive(void *userData, uint8_t addr, tinyproto::IPacket &pkt) {
        auto *s = static_cast<Session *>(userData);
        EnvScope scope;
        JNIEnv *env = scope.env;
        if (!env) return;

        jbyteArray arr = env->NewByteArray(pkt.size());
        env->SetByteArrayRegion(arr, 0, pkt.size(),
                reinterpret_cast<const jbyte *>(pkt.data()));
        env->CallVoidMethod(s->owner, s->onFrameId, static_cast<jint>(addr), arr);
        env->DeleteLocalRef(arr);

        if (env->ExceptionCheck()) {          // never leave a Java exception pending
            env->ExceptionDescribe();
            env->ExceptionClear();
        }
    }

    inline Session *S(jlong h) {
        return reinterpret_cast<Session *>(h);
    }

}  // namespace

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *) {
    g_vm = vm;
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_github_devlotfi_cipherbox_shared_transport_SerialTinyProtoLayer_nativeCreate(JNIEnv *env, jobject thiz, jint mtu, jint window) {
    auto *s = new Session(tiny_fd_buffer_size_by_mtu(mtu, window));
    s->owner = env->NewGlobalRef(thiz);
    jclass cls = env->GetObjectClass(thiz);
    s->onFrameId = env->GetMethodID(cls, "onNativeFrame", "(I[B)V");
    if (!s->onFrameId) {LOGE("onNativeFrame not found");}

    s->proto.setUserData(s);
    s->proto.setReceiveCallback(onReceive);
    s->proto.setWindowSize(static_cast<uint8_t>(window));
    s->proto.enableCrc16();
    s->proto.begin();
    return reinterpret_cast<jlong>(s);
}

extern "C" JNIEXPORT void JNICALL
Java_com_github_devlotfi_cipherbox_shared_transport_SerialTinyProtoLayer_nativeDestroy(JNIEnv *env, jobject, jlong h) {
    Session *s = S(h);
    if (!s) return;
    jobject owner = s->owner;
    delete s;                       // ~FdD() calls end() exactly once
    env->DeleteGlobalRef(owner);    // release the global ref only after the protocol is fully torn down
}

// Kotlin → device: queue a payload for sending
extern "C" JNIEXPORT jint JNICALL
Java_com_github_devlotfi_cipherbox_shared_transport_SerialTinyProtoLayer_nativeSend(JNIEnv *env, jobject, jlong h, jbyteArray data) {
    jsize len = env->GetArrayLength(data);
    std::vector<char> buf(len);
    env->GetByteArrayRegion(data, 0, len, reinterpret_cast<jbyte *>(buf.data()));
    return S(h)->proto.write(buf.data(), len);   // may block up to the send timeout
}

// Transport → protocol: raw bytes that arrived from the link
extern "C" JNIEXPORT jint JNICALL
Java_com_github_devlotfi_cipherbox_shared_transport_SerialTinyProtoLayer_nativeFeed(JNIEnv *env, jobject, jlong h, jbyteArray data, jint len) {
    std::vector<uint8_t> buf(len);
    env->GetByteArrayRegion(data, 0, len, reinterpret_cast<jbyte *>(buf.data()));
    return S(h)->proto.run_rx(buf.data(), len);  // may fire onReceive() synchronously
}

// Protocol → transport: next chunk of bytes to put on the wire (null if none)
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_github_devlotfi_cipherbox_shared_transport_SerialTinyProtoLayer_nativePollTx(JNIEnv *env, jobject, jlong h) {
    uint8_t tmp[256];
    int n = S(h)->proto.run_tx(tmp, sizeof(tmp));
    if (n <= 0) return nullptr;
    jbyteArray out = env->NewByteArray(n);
    env->SetByteArrayRegion(out, 0, n, reinterpret_cast<jbyte *>(tmp));
    return out;
}