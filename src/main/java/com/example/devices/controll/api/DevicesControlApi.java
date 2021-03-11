package com.example.devices.controll.api;

import com.example.devices.dto.DeviceDTO;
import com.example.devices.entity.Device;
import com.example.devices.scrcpy.ScrcpyUtil;

import com.example.devices.scrcpy.WebSocket;
import com.example.devices.service.impl.DeviceServiceImpl;
import com.example.devices.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DevicesControlApi {
    @Autowired
    DeviceServiceImpl deviceService;


    @PostMapping("/devices")
    public R getDevices(){
        ArrayList list=new ArrayList<>();
        int a=0;
        while (a<=50){
            list.add(a);
            a++;
        }
        return R.ok().put("data",list);
    }

    @PostMapping("/addDevice")
    public R addDevice(@RequestBody Map map){
        System.out.println(map.toString());
        Device device =new Device();
        device.setAbi(map.get("abi").toString());
        device.setName(map.get("name").toString());
        device.setAndroid(map.get("android").toString());
        device.setCompany(map.get("company").toString());
        device.setDescription(map.get("description").toString());
        device.setDevice(map.get("device").toString());
        device.setResolution(map.get("resolution").toString());
        device.setType(map.get("type").toString());
        deviceService.saveAndFlush(device);
        return R.ok();
    }

    @PostMapping("/close")
    public R closeDevice(HttpServletRequest request,Integer id,String username){
//        String username = request.getSession().getAttribute("username").toString();
        Device device=deviceService.findById(id);
        ScrcpyUtil.stopSocket(username);
        return R.ok();
    }

    @PostMapping("/login")
    public R getLogin(@RequestBody Map user, HttpServletRequest request){
        HttpSession session = request.getSession();
        session.setAttribute("username",user.get("username"));
        return R.ok();
    }

    /*
    * menu 82
    * back 4
    * home 3
    * */
    @PostMapping("/back")
    public R back(HttpServletRequest request,String username){
//        String username = request.getSession().getAttribute("username").toString();
        WebSocket.sendback(username);
        return R.ok();
    }
    @PostMapping("/home")
    public R home(HttpServletRequest request,String username){
//        String username = request.getSession().getAttribute("username").toString();
        WebSocket.sendhome(username);
        return R.ok();
    }
    @PostMapping("/menu")
    public R menu(HttpServletRequest request,String username){
//        String username = request.getSession().getAttribute("username").toString();
        WebSocket.sendmenu(username);
        return R.ok();
    }


}
