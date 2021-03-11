package com.example.devices.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "device_devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;             //设备id

    @Column
    private String company;         //厂商
    private String type;            //型号
    private String android;         //Android版本
    private String device;          //设备名
    private String name;            //设备名称
    private String resolution;      //设备分辨率
    private String abi;             //芯片架构
    private String description;     //其他描述
    private String libraryPath;     //库路径
}
