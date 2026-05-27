#include <jni.h>
#include <string>
#include <vector>
#include <dlfcn.h>
#include <android/log.h>

#define TAG "TDBR-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Função para carregar um símbolo de uma biblioteca
template<typename T>
T loadSymbol(void* lib, const char* symbol) {
    void* addr = dlsym(lib, symbol);
    if (!addr) {
        LOGE("Failed to load symbol: %s", symbol);
        return nullptr;
    }
    return reinterpret_cast<T>(addr);
}

// Ponteiros para funções OpenGL ES
typedef const GLubyte* (*PFNGLGETSTRINGPROC)(GLenum);
typedef const char* (*PFNEGLQUERYSTRINGPROC)(void*, EGLint);

extern "C" JNIEXPORT jstring JNICALL
Java_com_tdbr_optimizer_detector_TDBRDetector_getGLExtensions(JNIEnv* env, jobject) {
    void* libGLES = dlopen("libGLESv2.so", RTLD_LAZY);
    if (!libGLES) {
        LOGE("Failed to load libGLESv2.so");
        return env->NewStringUTF("");
    }

    auto glGetString = loadSymbol<PFNGLGETSTRINGPROC>(libGLES, "glGetString");
    if (!glGetString) {
        dlclose(libGLES);
        return env->NewStringUTF("");
    }

    const GLubyte* extensions = glGetString(0x1F03); // GL_EXTENSIONS
    std::string extString = extensions ? reinterpret_cast<const char*>(extensions) : "";
    dlclose(libGLES);

    return env->NewStringUTF(extString.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_tdbr_optimizer_detector_TDBRDetector_getEGLExtensions(JNIEnv* env, jobject) {
    void* libEGL = dlopen("libEGL.so", RTLD_LAZY);
    if (!libEGL) {
        LOGE("Failed to load libEGL.so");
        return env->NewStringUTF("");
    }

    auto eglGetDisplay = loadSymbol<decltype(eglGetDisplay)*>(libEGL, "eglGetDisplay");
    auto eglQueryString = loadSymbol<PFNEGLQUERYSTRINGPROC>(libEGL, "eglQueryString");

    if (!eglGetDisplay || !eglQueryString) {
        dlclose(libEGL);
        return env->NewStringUTF("");
    }

    void* display = eglGetDisplay(nullptr);
    const char* extensions = eglQueryString(display, 0x3055); // EGL_EXTENSIONS
    std::string extString = extensions ? extensions : "";
    dlclose(libEGL);

    return env->NewStringUTF(extString.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_tdbr_optimizer_detector_TDBRDetector_getGPUName(JNIEnv* env, jobject) {
    void* libGLES = dlopen("libGLESv2.so", RTLD_LAZY);
    if (!libGLES) {
        return env->NewStringUTF("Unknown");
    }

    auto glGetString = loadSymbol<PFNGLGETSTRINGPROC>(libGLES, "glGetString");
    if (!glGetString) {
        dlclose(libGLES);
        return env->NewStringUTF("Unknown");
    }

    const GLubyte* renderer = glGetString(0x1F01); // GL_RENDERER
    std::string gpuName = renderer ? reinterpret_cast<const char*>(renderer) : "Unknown";
    dlclose(libGLES);

    return env->NewStringUTF(gpuName.c_str());
}