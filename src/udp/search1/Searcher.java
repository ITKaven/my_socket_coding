package udp.search1;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Searcher {
    private static final int LISTEN_PORT = 3000;
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Searcher Started...");

        Listener listener = listen();
        sendBroadcast();

        // 读取任意键盘信息后可以退出
        System.in.read();

        List<Device> devices = listener.getDevicesAndClose();

        for (Device device : devices){
            System.out.println("Device：" + device.toString());
        }
        System.out.println("Searcher Finished...");
    }

    private static Listener listen() throws InterruptedException {
        System.out.println("Searcher start listen...");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT , countDownLatch);
        Thread thread = new Thread(listener);
        thread.start();

        countDownLatch.await();
        return listener;
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("Searcher sendBroadcast Started...");

        // 作为搜索方，无需指定端口，让系统分配
        DatagramSocket ds = new DatagramSocket();

        // 构建一份请求数据
        String requestData = MessageCreator.buildWithPort(LISTEN_PORT);
        byte[] requestDataBytes = requestData.getBytes();
        // 直接构建packet
        DatagramPacket requestPacket = new DatagramPacket(requestDataBytes ,
                requestDataBytes.length);
        // 广播地址
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        // 端口2000
        requestPacket.setPort(2000);

        // 发送信息
        ds.send(requestPacket);

        // 关闭
        System.out.println("Searcher sendBroadcast Finished...");
        ds.close();
    }

    private static class Device{
        int port;
        String ip;
        String sn;

        public Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", ip='" + ip + '\'' +
                    ", sn='" + sn + '\'' +
                    '}';
        }
    }

    private static class Listener implements Runnable{

        private final int listenPort;
        private final CountDownLatch countDownLatch;
        private final List<Device> devices = new ArrayList<>();
        private boolean done = false;
        private DatagramSocket ds = null;

        public Listener(int listenPort, CountDownLatch countDownLatch) {
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            // 通知启动
            countDownLatch.countDown();
            try{
                ds = new DatagramSocket(listenPort);

                while(!done){
                    // 构建接收实体
                    final byte[] buf = new byte[512];
                    DatagramPacket receivePack = new DatagramPacket(buf , buf.length);

                    // 接收
                    ds.receive(receivePack);

                    // 打印接收到的信息与发送者的信息
                    // 发送者的IP地址
                    String ip = receivePack.getAddress().getHostAddress();
                    // 发送者的端口
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    String data = new String(receivePack.getData() , 0 ,dataLen);
                    System.out.println("Searcher receive form ip："+ip+" port："+port+" data："+data);

                    String sn = MessageCreator.parseSn(data);
                    if(sn != null){
                        Device device = new Device(port , ip , sn);
                        devices.add(device);
                    }
                }
            } catch (Exception ignored){

            } finally {
                close();
            }
            // 关闭
            System.out.println("Searcher Finished...");
        }

        private void close(){
            if(ds != null){
                ds.close();
                ds = null;
            }
        }

        List<Device> getDevicesAndClose(){
            done = true;
            close();
            return devices;
        }
    }
}
