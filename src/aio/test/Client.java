package aio.test;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Client {
    final String LOCALHOST = "localhost";
    final int DEFAULT_PORT = 8888;

    AsynchronousSocketChannel clientChannel;

    private void close(Closeable closeable){
        if(closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start(){
        try {
            // 创建Channel
            clientChannel = AsynchronousSocketChannel.open();
            Future<Void> future = clientChannel.connect(
                    new InetSocketAddress(LOCALHOST , DEFAULT_PORT));
            // 当未连接成功前，这里是阻塞的
            future.get();

            // 等待用户的输入
            BufferedReader consoleReader =
                    new BufferedReader(new InputStreamReader(System.in));

            while(true){
                String input = consoleReader.readLine();

                byte[] inputBytes = input.getBytes();
                // 得到buffer的模式是读模式，可以Debug看一看
                ByteBuffer buffer = ByteBuffer.wrap(inputBytes);
                Future<Integer> writeResult = clientChannel.write(buffer);

                // 等待成功写入用户管道
                writeResult.get();
                // 写模式
                buffer.clear();
                Future<Integer> readResult = clientChannel.read(buffer);
                // 等待成功读取用户管道
                readResult.get();
                String echo = new String(buffer.array());
                System.out.println(echo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            close(clientChannel);
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}
