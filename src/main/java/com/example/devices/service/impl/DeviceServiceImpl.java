package com.example.devices.service.impl;

import com.example.devices.dao.DeviceRepository;
import com.example.devices.entity.Device;
import com.example.devices.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    DeviceRepository deviceRepository;

    @Override
    public List<Device> findAll() {
        return deviceRepository.findAll();
    }

    //插入数据
    public void saveAndFlush(Device device) {
        deviceRepository.saveAndFlush(device);
    }

    public Device findById(Integer id){
        return deviceRepository.findById(id).get();
    }
}
