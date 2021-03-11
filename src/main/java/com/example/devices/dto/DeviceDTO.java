package com.example.devices.dto;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
public class DeviceDTO {

    private String company;         //厂商
    private String name;          //设备名
    private String type;            //型号
    private String android;         //Android版本
    private String device;          //设备名
    private String resolution;      //设备分辨率
    private String abi;             //芯片架构
    private String description;     //其他描述
}
