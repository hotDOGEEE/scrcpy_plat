package com.example.devices.scrcpy;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/kilo/{username}")
@Component
public class WebSocket {
    private static int onlineCount = 0;
    private static Map<String, WebSocket> clients = new ConcurrentHashMap<String, WebSocket>();
    private static Map<Session, String> names = new ConcurrentHashMap<Session, String>();
    public static String messages;
    public int x;
    public int y;
    public int realWidth;
    public int realHeight;
    private static Socket socket;
    private Session session;

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setRealWH(int realWidth, int realHeight) {
        System.out.println("w:" + realWidth + "\nh:" + realHeight);
        this.realWidth = realWidth;
        this.realHeight = realHeight;
    }


    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) {
        addOnlineCount();
        this.session = session;
        clients.put(username, this);
        names.put(session, username);
        System.out.println("加入连接:" + onlineCount);
    }

    @OnClose
    public void onClose() {
        System.out.println("关闭连接");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param messages 客户端发送过来的消息
     * @param session  可选的参数
     */
    @OnMessage
    public void onMessage(String messages, Session session) {
        try {
            //System.out.println("接收到消息"+messages);
            String name = names.get(session);
            System.out.println(name);
            this.messages = messages;
            if (messages.length() > 12) {
                ScrcpyUtil.map.get(name).setSend(true);
                JSONObject jsonObject = JSONObject.parseObject(messages);
                if (jsonObject.get("action").toString().equals("2")) {
                    JSONObject jsonObject_position = JSONObject.parseObject(jsonObject.get("position").toString());
                    sendMove(Integer.parseInt(jsonObject_position.get("x_start").toString()) * 3,
                            Integer.parseInt(jsonObject_position.get("y_start").toString()) * 3,
                            Integer.parseInt(jsonObject_position.get("x_end").toString()) * 3,
                            Integer.parseInt(jsonObject_position.get("y_end").toString()) * 3,
                            Integer.parseInt(jsonObject_position.get("time").toString()),name);
                } else if (jsonObject.get("action").toString().equals("1")) {
                    JSONObject jsonObject_position = JSONObject.parseObject(jsonObject.get("position").toString());
                    sendClick(Integer.parseInt(jsonObject_position.get("x_start").toString()) * 3,
                            Integer.parseInt(jsonObject_position.get("y_start").toString()) * 3,name);
                } else {
                    System.out.println("前端数据结构或者数据有问题");
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getKey(Map<String, WebSocket> map, String value) {
        String key = null;
        //Map,HashMap并没有实现Iteratable接口.不能用于增强for循环.
        for (String getKey : map.keySet()) {
            if (map.get(getKey).equals(value)) {
                key = getKey;
            }
        }
        return key;
        //这个key肯定是最后一个满足该条件的key.
    }

    public void sendMessage(Object bf, String name) {
        try {
            clients.get(name).session.getBasicRemote().sendObject(bf);
        } catch (IllegalStateException e) {
//            System.out.println("服务没有启动");
            e.printStackTrace();
        } catch (EncodeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMove(int x_start, int y_start, int x_end, int y_end, int time,String name) {
        try {
            OutputStream outputStream = ScrcpyUtil.map.get(name).getSocketTouch().getOutputStream();
            //OutputStream outputStream = socket.getOutputStream();
            ByteBuffer byteBuf_down = ByteBuffer.allocate(28);//按压
            byteBuf_down.put((byte) 2);
            byteBuf_down.put((byte) 0);
            byteBuf_down.putLong(0);
            byteBuf_down.putInt(x_start);
            byteBuf_down.putInt(y_start);
            byteBuf_down.putShort((short) 1080);
            byteBuf_down.putShort((short) 1920);
            byteBuf_down.putShort((short) 0);
            byteBuf_down.putInt(0);
            byteBuf_down.flip();//切换写为读模式
            outputStream.write(conver(byteBuf_down));
            if (time >= 300) {
                Thread.sleep(50);
            }
            ByteBuffer byteBuf_move = ByteBuffer.allocate(28);//移动
            byteBuf_move.put((byte) 2);
            byteBuf_move.put((byte) 2);
            byteBuf_move.putLong(0);
            byteBuf_move.putInt(x_end);
            byteBuf_move.putInt(y_end);
            byteBuf_move.putShort((short) 1080);
            byteBuf_move.putShort((short) 1920);
            byteBuf_move.putShort((short) 0);
            byteBuf_move.putInt(0);
            byteBuf_move.flip();//切换写为读模式
            outputStream.write(conver(byteBuf_move));
            //Thread.sleep(time);
            ByteBuffer byteBuf_up = ByteBuffer.allocate(28);//抬起
            byteBuf_up.put((byte) 2);
            byteBuf_up.put((byte) 1);
            byteBuf_up.putLong(0);
            byteBuf_up.putInt(x_end);
            byteBuf_up.putInt(y_end);
            byteBuf_up.putShort((short) 1080);
            byteBuf_up.putShort((short) 1920);
            byteBuf_up.putShort((short) 0);
            byteBuf_up.putInt(0);
            byteBuf_up.flip();//切换写为读模式
            outputStream.write(conver(byteBuf_up));
            outputStream.flush();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendClick(int x_start, int y_start,String name) {
        //OutputStream outputStream = null;
        try {
            System.out.println("点击");
            OutputStream outputStream = ScrcpyUtil.map.get(name).getSocketTouch().getOutputStream();
            //outputStream = socket.getOutputStream();
            ByteBuffer byteBuf_down = ByteBuffer.allocate(28);//按压
            byteBuf_down.put((byte) 2);
            byteBuf_down.put((byte) 0);
            byteBuf_down.putLong(0);
            byteBuf_down.putInt(x_start);
            byteBuf_down.putInt(y_start);
            byteBuf_down.putShort((short) 1080);
            byteBuf_down.putShort((short) 1920);
            byteBuf_down.putShort((short) 0);
            byteBuf_down.putInt(0);
            byteBuf_down.flip();//切换写为读模式
            outputStream.write(conver(byteBuf_down));
            ByteBuffer byteBuf_up = ByteBuffer.allocate(28);//抬起
            byteBuf_up.put((byte) 2);
            byteBuf_up.put((byte) 1);
            byteBuf_up.putLong(0);
            byteBuf_up.putInt(x_start);
            byteBuf_up.putInt(y_start);
            byteBuf_up.putShort((short) 1080);
            byteBuf_up.putShort((short) 1920);
            byteBuf_up.putShort((short) 0);
            byteBuf_up.putInt(0);
            byteBuf_up.flip();//切换写为读模式
            outputStream.write(conver(byteBuf_up));
            outputStream.flush();
        } catch (SocketException e) {
            System.out.println("socket没有连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendback(String name) {
        //OutputStream outputStream = null;
        try {
            OutputStream outputStream = ScrcpyUtil.map.get(name).getSocketTouch().getOutputStream();
            //outputStream = socket.getOutputStream();
            ByteBuffer byteBuf_down = ByteBuffer.allocate(10);//按压
            byteBuf_down.put((byte) 0);
            byteBuf_down.put((byte) 0);//1抬起
            byteBuf_down.putInt(4);
            byteBuf_down.putInt(0);
            byteBuf_down.flip();
            outputStream.write(conver(byteBuf_down));
            ByteBuffer byteBuf_up = ByteBuffer.allocate(10);//按压
            byteBuf_up.put((byte) 0);
            byteBuf_up.put((byte) 1);//1抬起
            byteBuf_up.putInt(4);
            byteBuf_up.putInt(0);
            byteBuf_up.flip();
            outputStream.write(conver(byteBuf_up));
            outputStream.flush();
        } catch (SocketException e) {
            System.out.println("socket没有连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendhome(String name) {
        //OutputStream outputStream = null;
        try {
            OutputStream outputStream = ScrcpyUtil.map.get(name).getSocketTouch().getOutputStream();
            //outputStream = socket.getOutputStream();
            ByteBuffer byteBuf_down = ByteBuffer.allocate(10);//按压
            byteBuf_down.put((byte) 0);
            byteBuf_down.put((byte) 0);//1抬起
            byteBuf_down.putInt(3);
            byteBuf_down.putInt(0);
            byteBuf_down.flip();
            outputStream.write(conver(byteBuf_down));
            ByteBuffer byteBuf_up = ByteBuffer.allocate(10);//按压
            byteBuf_up.put((byte) 0);
            byteBuf_up.put((byte) 1);//1抬起
            byteBuf_up.putInt(3);
            byteBuf_up.putInt(0);
            byteBuf_up.flip();
            outputStream.write(conver(byteBuf_up));
            outputStream.flush();
        } catch (SocketException e) {
            System.out.println("socket没有连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendmenu(String name) {
        //OutputStream outputStream = null;
        try {
            OutputStream outputStream = ScrcpyUtil.map.get(name).getSocketTouch().getOutputStream();
            //outputStream = socket.getOutputStream();
            ByteBuffer byteBuf_down = ByteBuffer.allocate(10);//按压
            byteBuf_down.put((byte) 0);
            byteBuf_down.put((byte) 0);//1抬起
            byteBuf_down.putInt(187);
            byteBuf_down.putInt(0);
            byteBuf_down.flip();
            outputStream.write(conver(byteBuf_down));
            ByteBuffer byteBuf_up = ByteBuffer.allocate(10);//抬起
            byteBuf_up.put((byte) 0);
            byteBuf_up.put((byte) 1);//1抬起
            byteBuf_up.putInt(187);
            byteBuf_up.putInt(0);
            byteBuf_up.flip();
            outputStream.write(conver(byteBuf_up));
            outputStream.flush();
        } catch (SocketException e) {
            System.out.println("socket没有连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] conver(ByteBuffer byteBuffer) {
        int len = byteBuffer.limit() - byteBuffer.position();
        byte[] bytes = new byte[len];
        if (byteBuffer.isReadOnly()) {
            return null;
        } else {
            byteBuffer.get(bytes);
        }
        return bytes;
    }

    public static synchronized void addOnlineCount() {
        WebSocket.onlineCount++;
    }

}