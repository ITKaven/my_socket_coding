package udp.search1;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MyRunnable implements Runnable {

    private final String sn;
    private boolean done = false;
    private DatagramSocket ds = null;

    public MyRunnable(String sn) {
        this.sn = sn;
    }

    private void close(){
        if(ds != null){
            ds.close();
            ds = null;
        }
    }

    void exit(){
        done = true;
        close();
    }

    @Override
    public void run() {
        System.out.println("Provider Started...");
        try {
            // 作为接收者，指定一个端口用于数据接收
            ds = new DatagramSocket(2000);
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
                System.out.println("Provider receive form ip："+ip+" port："+port+" data："+data);

                // 解析端口号
                int responsePort = MessageCreator.parsePort(data);
                if(responsePort != -1){
                    // 构建一份回送数据
                    String responseData = MessageCreator.buildWithSn(sn);
                    byte[] responseDataBytes = responseData.getBytes();
                    // 直接根据发送者构建一份
                    DatagramPacket responsePacket = new DatagramPacket(responseDataBytes ,
                            responseDataBytes.length,
                            receivePack.getAddress(),
                            responsePort);

                    // 发送信息
                    ds.send(responsePacket);
                }
            }
        } catch (Exception ignored) {
        } finally {
            close();
        }
        // 关闭
        System.out.println("Provider Finished...");
    }
}
