package com.example.devices.service;

import com.example.devices.entity.Device;

import java.util.List;

public interface DeviceService {

    List<Device> findAll();

    void saveAndFlush(Device device);
}
