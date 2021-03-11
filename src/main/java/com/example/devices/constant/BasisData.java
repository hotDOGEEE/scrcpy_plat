package com.example.devices.constant;

public class BasisData {

    private static String title = "移动端设备管理";

    public static String getTitle() {
        return title;
    }

   /* public static String getName(){
        return SecurityUtils.getSubject().getPrincipal().toString();
    }*/

    public static void setTitle(String title) {
        BasisData.title = title;
    }
}
