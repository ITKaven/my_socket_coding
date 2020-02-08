package bio.chatroom_threadpool.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private final int DEFAULT_PORT = 8888;
    private final String QUIT = "quit";

    private ExecutorService executorService;
    private ServerSocket serverSocket;

    // 把客户端的port当作客户端的id
    private Map<Integer , Writer> connectedClients;

    public ChatServer(){
        executorService = Executors.newFixedThreadPool(2);
        connectedClients = new HashMap<>();
    }

    public synchronized void addClient(Socket socket) throws IOException {
        if(socket != null){
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );
            connectedClients.put(port , writer);
            System.out.println("客户端["+port+"]已连接到服务器");
        }
    }

    public synchronized void removeClient(Socket socket) throws IOException {
        if(socket != null){
            int port = socket.getPort();
            if(connectedClients.containsKey(port)){
                connectedClients.get(port).close();
                connectedClients.remove(port);
                System.out.println("客户端["+port+"]已断开连接");
            }
        }
    }

    public synchronized void forwardMessage(Socket socket , String fwdMsg) throws IOException {
        // 发送消息的端口
        int sendMessagePort = socket.getPort();
        for(Integer port : connectedClients.keySet()){
            if(!port.equals(sendMessagePort)){
                Writer writer = connectedClients.get(port);
                writer.write(fwdMsg);
                writer.flush();
            }
        }
    }

    public boolean readyToQuit(String msg){
        return QUIT.equalsIgnoreCase(msg);
    }

    public synchronized void close(){
        if(serverSocket != null){
            try {
                serverSocket.close();
                System.out.println("关闭了ServerSocket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start(){
        try {
            // 创建ServerSocket，绑定和监听端口
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口"+DEFAULT_PORT+"...");

            while(true){
                // 等待客户端连接
                Socket socket = serverSocket.accept();
                // 创建ChatHandler线程
//                new Thread(new ChatHandler(this , socket)).start();
                executorService.execute(new ChatHandler(this , socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
