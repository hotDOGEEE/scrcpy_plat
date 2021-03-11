package com.example.devices.scrcpy;


import com.android.ddmlib.*;
import com.example.devices.entity.Device;
import com.example.devices.utils.CMDUtil;
import lombok.SneakyThrows;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class ScrcpyUtil implements ScreenSubject{

    private  Socket socket;
    private IDevice device;
    private  Socket socketTouch;
    private List<AndroidScreenObserver> observers = new ArrayList<AndroidScreenObserver>();
    public  boolean isRunning = false;
    private static WebSocket webSocket;
    public  boolean isSend=false;
    public  Thread frame;
    public  Thread touch;
    public  Thread convert;
    public String username;

    public static Map<String,Scrcpy> map=new ConcurrentHashMap<String,Scrcpy>();


    private LinkedBlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<byte[]>();

    private Banner banner = new Banner();

    public ScrcpyUtil(){}

    private byte[] subByteArray(byte[] byte1, int start, int end) {
        byte[] byte2 = new byte[0];
        try {
            byte2 = new byte[end - start];
        } catch (NegativeArraySizeException e) {
            e.printStackTrace();
        }
        System.arraycopy(byte1, start, byte2, 0, end - start);
        return byte2;
    }

    // java合并两个byte数组
    private static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    private BufferedImage createImageFromByte(byte[] binaryData) {
        BufferedImage bufferedImage = null;
        InputStream in = new ByteArrayInputStream(binaryData);
        try {
            bufferedImage = ImageIO.read(in);
            if (bufferedImage == null) {
                //LOG.debug("bufferimage为空");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return bufferedImage;
    }

    public void startScreenListener(Device device, String username) throws InterruptedException, IOException {
        this.username=username;
        String startScrcpy = "adb -s "+device.getDevice()+" shell LD_LIBRARY_PATH=/system/lib64:/product/lib64:/prets/lib64:/data/local/tmp CLASSPATH=/data/local/tmp/server-debug.apk app_process / com.genymobile.scrcpy.Server -Q 60 -r 24 -P "+device.getResolution();//默认启动 -Q 60 -r 24 -P 480
        String adb_forward="adb -s "+device.getDevice()+" forward tcp:1717 tcp:6612";
        new Thread(new Runnable() {
            @Override
            public void run() {
                CMDUtil.excuteCMDCommand(adb_forward);
            }
        }).start();
        Thread.sleep(500);
        new Thread(new Runnable() {
            @Override
            public void run() {
                CMDUtil.excuteCMDCommand(startScrcpy);
            }
        }).start();
        Thread.sleep(2000);

        webSocket=new WebSocket();
        socket = new Socket("127.0.0.1", 1717);
        socketTouch = new Socket("127.0.0.1", 1717);
        //webSocket.setSocket(socketTouch);
        isRunning = true;
        frame = new Thread(new ImageBinaryFrameCollector());
        touch= new Thread(new TouchThread());
        convert = new Thread(new ImageConverter());
        Scrcpy scrcpy=new Scrcpy();
        scrcpy.setSocket(socket);
        scrcpy.setSocketTouch(socketTouch);
        scrcpy.setFrame(frame);
        scrcpy.setTouch(touch);
        scrcpy.setConvert(convert);
        scrcpy.setRunning(isRunning);
        scrcpy.setDataQueue(dataQueue);
        scrcpy.setSend(isSend);
        map.put(username,scrcpy);

        frame.start();

        touch.start();

        convert.start();
    }



    class ImageConverter implements Runnable {
        private int readBannerBytes = 0;
        private int bannerLength = 2;
        private int readFrameBytes = 0;
        private int frameBodyLength = 0;
        private byte[] frameBody = new byte[0];

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Runnable#run()
         */
        @SneakyThrows
        public void run() {
            // TODO Auto-generated method stub
            long start = System.currentTimeMillis();
            while (map.get(username).isRunning()) {
                byte[] buffer = new byte[0];
                try {
                    //if(dataQueue.isEmpty()) //System.out.println("队列为空~");
                    buffer = map.get(username).getDataQueue().take();
                    //buffer = dataQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int len = buffer.length;
                for (int cursor = 0; cursor < len; ) {
                    int byte10 = buffer[cursor] & 0xff;
                    if (readBannerBytes < bannerLength) {
                        cursor = parserBanner(cursor, byte10);
                    } else if (readFrameBytes < 4) {
                        // 第二次的缓冲区中前4位数字和为frame的缓冲区大小
                        frameBodyLength += (byte10 << (readFrameBytes * 8)) >>> 0;
                        cursor += 1;
                        readFrameBytes += 1;
                        // LOG.debug("解析图片大小 = " + readFrameBytes);
                    } else {
                        if (len - cursor >= frameBodyLength) {
                            //LOG.debug("frameBodyLength = " + frameBodyLength);
                            byte[] subByte = subByteArray(buffer, cursor,
                                    cursor + frameBodyLength);
                            frameBody = byteMerger(frameBody, subByte);
                            if ((frameBody[0] != -1) || frameBody[1] != -40) {
                                //LOG.error(String.format("Frame body does not start with JPG header"));
                            }
                            //TODO 前端传输数据 ws
                            final byte[] finalBytes = subByteArray(frameBody,
                                    0, frameBody.length);
                            // 转化成bufferImage
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    //Image image = createImageFromByte(finalBytes);
                                    webSocket.sendMessage(finalBytes,username);
                                    //System.out.println("发送给数据");
                                }
                            }).start();

                            long current = System.currentTimeMillis();
                           // LOG.info("图片已生成,耗时: " + TimeUtil.formatElapsedTime(current - start));
                            start = current;
                            cursor += frameBodyLength;
                            restore();
                        } else {
                            //LOG.debug("所需数据大小 : " + frameBodyLength);
                            byte[] subByte = subByteArray(buffer, cursor, len);
                            frameBody = byteMerger(frameBody, subByte);
                            frameBodyLength -= (len - cursor);
                            readFrameBytes += (len - cursor);
                            cursor = len;
                        }
                    }
                }
            }

        }

        private void restore() {
            frameBodyLength = 0;
            readFrameBytes = 0;
            frameBody = new byte[0];
        }

        private int parserBanner(int cursor, int byte10) {
            switch (readBannerBytes) {
                case 0:
                    // version
                    banner.setVersion(byte10);
                    //System.out.println("VERSION = "+ banner.getVersion());
                    break;
                case 1:
                    // length
                    bannerLength = byte10;
                    banner.setLength(byte10);
                    //System.out.println("LENGHT = "+ banner.getLength());
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                    // pid
                    int pid = banner.getPid();
                    pid += (byte10 << ((readBannerBytes - 2) * 8)) >>> 0;
                    banner.setPid(pid);
                    //System.out.println("PID = "+pid);
                    break;
                case 6:
                case 7:
                case 8:
                case 9:
                    // real width
                    int realWidth = banner.getReadWidth();
                    realWidth += (byte10 << ((readBannerBytes - 6) * 8)) >>> 0;
                    banner.setReadWidth(realWidth);
                    //width_r=realWidth;
                    //System.out.println("realWidth = "+ banner.getReadWidth());
                    break;
                case 10:
                case 11:
                case 12:
                case 13:
                    // real height
                    int realHeight = banner.getReadHeight();
                    realHeight += (byte10 << ((readBannerBytes - 10) * 8)) >>> 0;
                    banner.setReadHeight(realHeight);
                    //height_r=realHeight;
                    //System.out.println("realHeight = "+ banner.getReadHeight());
                    break;
                case 14:
                case 15:
                case 16:
                case 17:
                    // virtual width
                    int virtualWidth = banner.getVirtualWidth();
                    virtualWidth += (byte10 << ((readBannerBytes - 14) * 8)) >>> 0;
                    banner.setVirtualWidth(virtualWidth);
                    break;
                case 18:
                case 19:
                case 20:
                case 21:
                    // virtual height
                    int virtualHeight = banner.getVirtualHeight();
                    virtualHeight += (byte10 << ((readBannerBytes - 18) * 8)) >>> 0;
                    banner.setVirtualHeight(virtualHeight);
                    break;
                case 22:
                    // orientation
                    banner.setOrientation(byte10 * 90);
                    break;
                case 23:
                    // quirks
                    banner.setQuirks(byte10);
                    break;

            }

            cursor += 1;
            readBannerBytes += 1;

            if (readBannerBytes == bannerLength) {
                // LOG.debug(banner.toString());
            }
            return cursor;
        }

    }


    //控制模块
    class TouchThread implements Runnable {
        @Override
        public void run() {
            /*try {
                socketTouch = new Socket("127.0.0.1", 1717);
            }catch (ConnectException e){
                System.out.println("scrcpy服务没有启动");
            }catch (IOException e) {
                e.printStackTrace();
            }*/
            while (map.get(username).isRunning()){
                if (map.get(username).isSend()){
                    //String startCmd = String.format(MINITOUCH_START_COMMAND,MINITOUCH_FILE);
                    //webSocket.setSocket(socketTouch);
                    map.get(username).setSend(false);
                    //isSend=false;
                }
            }


        }
    }


    private String executeShellCommand(String command) {
        CollectingOutputReceiver output = new CollectingOutputReceiver();
        try {
            device.executeShellCommand(command, output, 0);
            System.out.println(device.getAvdName());
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AdbCommandRejectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ShellCommandUnresponsiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return output.getOutput();
    }

    class ImageBinaryFrameCollector implements Runnable {
        private InputStream stream = null;

        // private DataInputStream input = null;

        public void run() {
            System.out.println("图片二进制数据收集器已经开启");

            try {
                //socket = new Socket("127.0.0.1", 1717);
                stream = socket.getInputStream();
                int len = 4096;
                while (map.get(username).isRunning()) {
                    byte[] buffer;
                    buffer = new byte[len];
                    int realLen = stream.read(buffer);
                    if (buffer.length != realLen) {
                        buffer = subByteArray(buffer, 0, realLen);
                    }
                    map.get(username).getDataQueue().add(buffer);
                    //dataQueue.add(buffer);
                }
            }catch (ConnectException e){
                System.out.println("scrcpy服务没有启动");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null && socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            //LOG.debug("图片二进制数据收集器已关闭");
        }

    }


    public void registerObserver(AndroidScreenObserver o) {
        observers.add(o);
    }

    public void removeObserver(AndroidScreenObserver o) {
        int index = observers.indexOf(o);
        if (index != -1) {
            observers.remove(o);
        }
    }

    @Override
    public void notifyObservers(Image image) {
        for (AndroidScreenObserver observer : observers) {
            observer.frameImageChange(image);
        }
    }

    public static void stopSocket(String username){
        try {


            //isRunning=false;
            map.get(username).setRunning(false);
            //Thread.sleep(2000);
            //socket.close();
            map.get(username).getSocket().close();
            //socketTouch.close();
            map.get(username).getSocketTouch().close();
            //webSocket.onClose();
            //frame.interrupt();
            map.get(username).getFrame().interrupt();
            //touch.interrupt();
            map.get(username).getTouch().interrupt();
            //convert.interrupt();
            map.get(username).getConvert().interrupt();
            //dataQueue.clear();
            map.get(username).getDataQueue().clear();
        } catch (IOException  e) {
            e.printStackTrace();
        }
    }
}
