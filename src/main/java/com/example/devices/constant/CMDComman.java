package com.example.devices.constant;

public class CMDComman {

    public static String adb_phone_resolution="adb shell wm size";//获取手机分辨率

    public static String adb_forward="adb forward tcp:1717 tcp:6612";//转发本地1717端口到手机6621端口

    public static String adb_push_apk="adb push D:\\1A_GuoJun\\IDEA\\IdeaProjects\\kilo-device\\lib\\apk\\server-debug.apk /data/local/tmp/";//推送apk文件到手机

    public static String adb_abi="adb shell getprop ro.product.cpu.abi";//查询手机的abi类型

    public static String adb_chmod="adb shell chmod 777 /data/local/tmp/server-debug.apk";//修改权限

    public static String abd_LD_LIBRARY_PATH="adb shell CLASSPATH=/data/local/tmp/server-debug.apk app_process / com.genymobile.scrcpy.Server -L";//获取LD_LIBRARY_PATH参数

}
