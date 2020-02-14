package udp.search1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

public class Provider {
    public static void main(String[] args) throws IOException {
       // 生成一份唯一标识
        String sn = UUID.randomUUID().toString();
        MyRunnable runnable = new MyRunnable(sn);
        Thread thread = new Thread(runnable);
        thread.start();

        // 读取任意键盘信息后可以退出
        System.in.read();
        runnable.exit();
    }
}
