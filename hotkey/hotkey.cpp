// hotkey.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"
#include <jni.h>
#include <stdio.h>
#include "hotkey.h"


JNIEXPORT void JNICALL Java_hotkey_startloop(JNIEnv *env, jobject obj) 
{
	jclass cls = env->GetObjectClass(obj);
    jmethodID mid = (jmethodID)env->GetMethodID( cls, "trigger", "(I)V");
    if (mid == 0) {
        return;
    }

	//RegisterHotKey(NULL,120,MOD_CONTROL | MOD_ALT,'L');
	RegisterHotKey(NULL,120,MOD_WIN,'A');
	MSG msg;

	while(GetMessage(&msg,NULL,WM_HOTKEY,WM_HOTKEY))
	{
		env->CallVoidMethod(obj, mid, msg.wParam);
	} 
return;
}