package tcp.nio.chatroom.server;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;

public class ChatServer {

    // 默认端口
    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    // 缓冲区大小
    private static final int BUFFER = 1024;

    private ServerSocketChannel server;
    private Selector selector;
    // 读取 buffer
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
    // 写入 buffer
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
    private Charset charset = Charset.forName("UTF-8");
    // 自定义端口
    private int port;

    public ChatServer(){
        this(DEFAULT_PORT);
    }

    public ChatServer(int port){
        this.port = port;
    }

    public void start(){
        try {
            // 打开一个ServerSocket 的 Channel
            server = ServerSocketChannel.open();
            // 设置非阻塞
            server.configureBlocking(false);
            // 得到一个关于该ServerSocketChannel的ServerSocket、并且绑定端口
            server.socket().bind(new InetSocketAddress(port));

            // 打开Selector
            selector = Selector.open();
            // 把ServerSocketChannel注册到Selector ， 设置监听事件为ACCEPT
            server.register(selector , SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，监听端口："+ port +"...");

            while(true){
                // 本身是阻塞式调用
                selector.select();
                // 触发事件集
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for(SelectionKey key : selectionKeys){
                    // 处理被触发的事件
                    handles(key);
                }
                // 处理完成后，手动清空
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            close(selector);
        }
    }

    private boolean readyToQuit(String msg){
        return QUIT.equalsIgnoreCase(msg);
    }

    private synchronized void close(Closeable closeable){
        if(closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String receive(SocketChannel client) throws IOException {
        // 写模式
        rBuffer.clear();
        while((client.read(rBuffer)) > 0);
        // 读模式
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    private void forwardMessage(SocketChannel client , String fwdMsg) throws IOException {
        for(SelectionKey key : selector.keys()){
            Channel connectedClient = key.channel();
            if(connectedClient instanceof  ServerSocketChannel){
                continue;
            }
            if(key.isValid() && !client.equals(connectedClient)){
                // 写模式
                wBuffer.clear();
                wBuffer.put(charset.encode(getClientName(client) +":"+fwdMsg));
                // 读模式
                wBuffer.flip();
                while(wBuffer.hasRemaining()){
                    ((SocketChannel) connectedClient).write(wBuffer);
                }
            }
        }
    }

    private String getClientName(SocketChannel client){
        return "客户端["+client.socket().getPort()+"]";
    }

    private void handles(SelectionKey key) throws IOException {
        // ACCEPT事件 - 和客户端建立了连接
        if(key.isAcceptable()){
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel client = server.accept();
            // 设置成非阻塞
            client.configureBlocking(false);
            client.register(selector , SelectionKey.OP_READ);
            System.out.println(getClientName(client)+"已连接");
        }
        // READ事件 - 客户端发送了消息
        else if(key.isReadable()){
            SocketChannel client = (SocketChannel) key.channel();
            String fwdMsg = receive(client);
            if(fwdMsg.isEmpty()){
                // 客户端异常 , 不再监听这个事件
                key.cancel();
                // 更新监听事件状态
                selector.wakeup();
            } else{
                forwardMessage(client , fwdMsg);

                // 检查用户是否准备退出
                if(readyToQuit(fwdMsg)){
                    key.cancel();
                    selector.wakeup();
                    System.out.println(getClientName(client)+"已断开");
                }
            }
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
