package udp.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Provider {
    public static void main(String[] args) throws IOException {
        System.out.println("Provider Started...");

        // 作为接收者，指定一个端口用于数据接收
        DatagramSocket ds = new DatagramSocket(2000);

        // 构建接收实体
        final byte[] buf = new byte[512];
        DatagramPacket receivePack = new DatagramPacket(buf , buf.length);

        // 接收
        ds.receive(receivePack);

        // 打印接收到的信息与发送者的信息
        // 发送者的IP地址
        String ip = receivePack.getAddress().getHostAddress();
        int port = receivePack.getPort();
        int dataLen = receivePack.getLength();
        String data = new String(receivePack.getData() , 0 ,dataLen);
        System.out.println("Provider receive form ip："+ip+" port："+port+" data："+data);

        // 构建一份回送数据
        String responseData = "Receive data with len:" + dataLen;
        byte[] responseDataBytes = responseData.getBytes();
        // 直接根据发送者构建一份
        DatagramPacket responsePacket = new DatagramPacket(responseDataBytes ,
                responseDataBytes.length,
                receivePack.getAddress(),
                receivePack.getPort());

        // 发送信息
        ds.send(responsePacket);

        // 关闭
        System.out.println("Provider Finished...");
        ds.close();
    }
}
