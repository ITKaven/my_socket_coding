package udp.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Searcher {
    public static void main(String[] args) throws IOException {
        System.out.println("Searcher Started...");

        // 作为搜索方，无需指定端口，让系统分配
        DatagramSocket ds = new DatagramSocket();

        // 构建一份请求数据
        String requestData = "Hello World";
        byte[] requestDataBytes = requestData.getBytes();
        // 直接构建packet
        DatagramPacket requestPacket = new DatagramPacket(requestDataBytes ,
                requestDataBytes.length);
        // 本地地址
        requestPacket.setAddress(InetAddress.getLocalHost());
        // 端口2000
        requestPacket.setPort(2000);

        // 发送信息
        ds.send(requestPacket);

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

        // 关闭
        System.out.println("Searcher Finished...");
        ds.close();
    }
}
