package tcp.aio.test;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

public class Server {

    final String LOCALHOST = "localhost";
    final int DEFAULT_PORT = 8888;
    AsynchronousServerSocketChannel serverChannel;

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
            // 打开通道
            serverChannel = AsynchronousServerSocketChannel.open();
            // 绑定、监听端口
            serverChannel.bind(new InetSocketAddress(DEFAULT_PORT));
            System.out.println("启动服务器，监听端口："+DEFAULT_PORT+"...");

            while(true){
                // 异步调用
                serverChannel.accept(null,new AcceptHandler());
                // 不会频繁调用accept的小技巧
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            close(serverChannel);
        }
    }

    private class AcceptHandler implements
            CompletionHandler<AsynchronousSocketChannel , Object> {
        @Override
        public void completed(AsynchronousSocketChannel result, Object attachment) {
            //此次调用accept完成，再一次调用accept，等待下一个客户端连接
            if(serverChannel.isOpen()) {
                serverChannel.accept(null , this);
            }

            AsynchronousSocketChannel clientChannel = result;
            if(clientChannel != null && clientChannel.isOpen()){
                ClientHandler handler = new ClientHandler(clientChannel);

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                Map<String , Object> info = new HashMap<>();
                info.put("type", "read");
                info.put("buffer" , buffer);
                clientChannel.read(buffer , info, handler);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            // 处理错误
        }
    }

    private class ClientHandler implements
            CompletionHandler<Integer , Object>{

        private AsynchronousSocketChannel clientChannel;

        public ClientHandler(AsynchronousSocketChannel channel){
            this.clientChannel = channel;
        }
        @Override
        public void completed(Integer result, Object attachment) {
            Map<String , Object> info = (Map<String, Object>) attachment;
            String type = (String) info.get("type");

            if(type.equals("read")){
                ByteBuffer buffer = (ByteBuffer) info.get("buffer");
                // 读模式
                buffer.flip();
                info.put("type" , "write");
                clientChannel.write(buffer ,info , this);
                // 写模式(也相当于清空)
                buffer.clear();
            }
            else if(type.equals("write")){
                ByteBuffer buffer = ByteBuffer.allocate(1024);

                info.put("type" , "read");
                info.put("buffer" , buffer);

                clientChannel.read(buffer , info , this);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            // 处理错误
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
