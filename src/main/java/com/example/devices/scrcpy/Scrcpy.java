package com.example.devices.scrcpy;

import lombok.Data;

import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

@Data
public class Scrcpy {

    private String username;
    private Socket socket;
    private Socket socketTouch;
    private WebSocket webSocket;
    private boolean isSend=false;
    private  boolean isRunning = false;
    private Thread frame;
    private Thread touch;
    private Thread convert;
    private LinkedBlockingQueue<byte[]> dataQueue;
    private Banner banner = new Banner();
    private int port;
}
