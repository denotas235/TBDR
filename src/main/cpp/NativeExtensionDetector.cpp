#include <jni.h>
#include <string>
#include <dlfcn.h>
#include <android/log.h>

#define TAG "TDBR-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Primitivos e macros do OpenGL ES / EGL para evitar dependências pesadas de headers externos
typedef unsigned int GLenum;
typedef unsigned char GLubyte;
typedef int EGLint;

#define GL_RENDERER   0x1F01
#define GL_EXTENSIONS 0x1F03
#define EGL_EXTENSIONS 0x3055

template<typename T>
T loadSymbol(void* lib, const char* symbol) {
    void* addr = dlsym(lib, symbol);
    if (!addr) {
        LOGE("Falha ao carregar o simbolo nativo: %s", symbol);
        return nullptr;
    }
    return reinterpret_cast<T>(addr);
}

typedef const GLubyte* (*PFNGLGETSTRINGPROC)(GLenum);
typedef const char* (*PFNEGLQUERYSTRINGPROC)(void*, EGLint);
typedef void* (*PFNEGLGETCURRENTDISPLAYPROC)();

extern "C" JNIEXPORT jstring JNICALL
Java_com_tdbr_optimizer_detector_TDBRDetector_getGLExtensions(JNIEnv* env, jobject) {
    void* libGLES = dlopen("libGLESv2.so", RTLD_LAZY);
    if (!libGLES) {
        LOGE("Erro crítico: Nao foi possivel abrir a libGLESv2.so");
        return env->NewStringUTF("");
    }

    auto glGetString = loadSymbol<PFNGLGETSTRINGPROC>(libGLES, "glGetString");
    if (!glGetString) {
        dlclose(libGLES);
        return env->NewStringUTF("");
    }

    const GLubyte* extensions = glGetString(GL_EXTENSIONS);
    if (!extensions) {
        LOGE("Alerta: glGetString retornou nulo. Esta funcao deve ser chamada estritamente na Render Thread ativa!");
        dlclose(libGLES);
        return env->NewStringUTF("");
    }

    std::string extString(reinterpret_cast<const char*>(extensions));
    dlclose(libGLES);
    return env->NewStringUTF(extString.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_tdbr_optimizer_detector_TDBRDetector_getEGLExtensions(JNIEnv* env, jobject) {
    void* libEGL = dlopen("libEGL.so", RTLD_LAZY);
    if (!libEGL) {
        LOGE("Erro crítico: Nao foi possivel abrir a libEGL.so");
        return env->NewStringUTF("");
    }

    auto eglGetCurrentDisplay = loadSymbol<PFNEGLGETCURRENTDISPLAYPROC>(libEGL, "eglGetCurrentDisplay");
    auto eglQueryString = loadSymbol<PFNEGLQUERYSTRINGPROC>(libEGL, "eglQueryString");

    if (!eglGetCurrentDisplay || !eglQueryString) {
        dlclose(libEGL);
        return env->NewStringUTF("");
    }

    // Captura o display EGL ativo do Pojav/CSLauncher em vez de tentar criar um novo do zero (nullptr)
    void* display = eglGetCurrentDisplay();
    if (!display) {
        dlclose(libEGL);
        return env->NewStringUTF("");
    }

    const char* extensions = eglQueryString(display, EGL_EXTENSIONS);
    std::string extString = extensions ? extensions : "";
    dlclose(libEGL);

    return env->NewStringUTF(extString.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_tdbr_optimizer_detector_TDBRDetector_getGPUNameNative(JNIEnv* env, jobject) {
    void* libGLES = dlopen("libGLESv2.so", RTLD_LAZY);
    if (!libGLES) {
        return env->NewStringUTF("Mali-G52 (Fallback)");
    }

    auto glGetString = loadSymbol<PFNGLGETSTRINGPROC>(libGLES, "glGetString");
    if (!glGetString) {
        dlclose(libGLES);
        return env->NewStringUTF("Mali-G52 (Fallback)");
    }

    const GLubyte* renderer = glGetString(GL_RENDERER);
    std::string gpuName = renderer ? reinterpret_cast<const char*>(renderer) : "ARM Mali-G52 (MaliOpt Dev)";
    dlclose(libGLES);

    return env->NewStringUTF(gpuName.c_str());
}
