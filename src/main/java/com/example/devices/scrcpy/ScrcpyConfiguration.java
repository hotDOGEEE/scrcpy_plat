package com.example.devices.scrcpy;

import com.example.devices.constant.CMDComman;
import com.example.devices.utils.CMDUtil;

public class ScrcpyConfiguration {
    public static String adb_push_command;
    /*
    * 上传
    * server-debug.apk
    * libcompress.so
    * libjpeg.so
    * libturbojpeg.so
    * 到手机
    * */
    public static String adbPush(){
        String abi = "arm64-v8a";
        String libcompress = null;
        String libjpeg = null;
        String libturbojpeg = null;
        if (abi.equals("arm64-v8a")){
            libcompress ="adb push D:\\1A_GuoJun\\IDEA\\IdeaProjects\\kilo-device\\lib\\arm64-v8a\\libcompress.so /data/local/tmp/";
            libjpeg="adb push D:\\1A_GuoJun\\IDEA\\IdeaProjects\\kilo-device\\lib\\arm64-v8a\\libjpeg.so /data/local/tmp/";
            libturbojpeg="adb push D:\\1A_GuoJun\\IDEA\\IdeaProjects\\kilo-device\\lib\\arm64-v8a\\libturbojpeg.so /data/local/tmp/";

        }else if (abi.equals("armeabi-v7a")){
            libcompress="adb push D:\\1A_GuoJun\\IDEA\\IdeaProjects\\kilo-device\\lib\\armeabi-v7a\\libcompress.so /data/local/tmp/";
            libjpeg="adb push D:\\1A_GuoJun\\IDEA\\IdeaProjects\\kilo-device\\lib\\armeabi-v7a\\libjpeg.so /data/local/tmp/";
            libturbojpeg="adb push D:\\1A_GuoJun\\IDEA\\IdeaProjects\\kilo-device\\lib\\armeabi-v7a\\libturbojpeg.so /data/local/tmp/";

        }
       return adb_push_command="cmd /c "+CMDComman.adb_push_apk+" & "+CMDComman.adb_chmod+" & "+libcompress+" & "+libjpeg+" & "+libturbojpeg;
    }

    /*
    * 启动scrcpy服务
    * */
    public static void adbStartScrcpy() throws InterruptedException {
        //CMDUtil.excuteCMDCommand("cmd /c "+CMDComman.adb_forward);
        //String LD_LIBRARY_PATH = CMDUtil.excuteCMDCommand(CMDComman.abd_LD_LIBRARY_PATH).replaceAll(" ","");
        String startScrcpy="adb shell LD_LIBRARY_PATH=/system/lib64:/product/lib64:/prets/lib64:/data/local/tmp CLASSPATH=/data/local/tmp/server-debug.apk app_process / com.genymobile.scrcpy.Server";//默认启动 -Q 60 -r 24 -P 480
        CMDUtil.excuteCMDCommand(startScrcpy);
    }
}
