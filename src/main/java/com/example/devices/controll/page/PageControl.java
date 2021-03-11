package com.example.devices.controll.page;


import com.android.ddmlib.IDevice;
import com.example.devices.entity.Device;
import com.example.devices.scrcpy.ScrcpyUtil;
import com.example.devices.service.impl.DeviceServiceImpl;
import com.example.devices.utils.ADB;
import com.example.devices.utils.AdbDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class PageControl {

    @Autowired
    DeviceServiceImpl deviceService;

    @GetMapping("/device")
    public String getDevice(Integer id,HttpServletRequest request, Model model) {
        Device device = deviceService.findById(id);
        HttpSession session =request.getSession();
//        String name = session.getAttribute("Username").toString();
        String name=device.getDevice();
        //ADB adb = new ADB();
        try {
            ScrcpyUtil scrcpyUtil = new ScrcpyUtil();
            scrcpyUtil.startScreenListener(device,name);
            model.addAttribute("deviceName",name);
            return "page/device";
        } catch (NullPointerException | InterruptedException | IOException e) {
            e.printStackTrace();
            return "page/device";
        }
    }

    @GetMapping("/index")
    public String getIndex() {
        return "index";
    }

    @GetMapping("/devices")
    public String getDevices(Model model) {
        ADB adb =new ADB();
        IDevice[] devices = adb.getDevices();
        HashMap<String,String> map=new HashMap<>();
        for (IDevice device : devices){
            map.put(device.toString(),device.getState().toString());
        }
        List<AdbDevice> data=new ArrayList<>();
        List<Device> all = deviceService.findAll();
        for (Device device:all){
            AdbDevice adbDevice =new AdbDevice();
            adbDevice.setDevice(device.getDevice());
            adbDevice.setId(device.getId());
            adbDevice.setCompany(device.getCompany());
            adbDevice.setType(device.getType());
            adbDevice.setAndroid(device.getAndroid());
            adbDevice.setName(device.getName());
            adbDevice.setResolution(device.getResolution());
            adbDevice.setAbi(device.getAbi());
            adbDevice.setDescription(device.getDescription());
            adbDevice.setState(map.get(device.getDevice()));
            data.add(adbDevice);
        }
        model.addAttribute("data", data);
        return "page/devices";
    }

    @GetMapping("/addDevice")
    public String addDevice() {
        return "page/addDevice";
    }

    @GetMapping("/deviceSetting")
    public String deviceSetting() {
        return "page/deviceSetting";
    }
}
