package tcp.bio.chatroom_threadpool.client;

import java.io.*;
import java.net.Socket;

public class ChatClient {

    private final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private final int DEFAULT_PORT = 8888;
    private final String QUIT = "quit";

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    // 发送消息给服务器
    public void send(String msg) throws IOException {
        if(!socket.isOutputShutdown()){
            writer.write(msg+"\n");
            writer.flush();
        }
    }

    // 接收服务器的消息
    public String receive() throws IOException {
        String msg = null;
        if(!socket.isInputShutdown()){
            msg = reader.readLine();
        }
        return msg;
    }

    // 检查用户是否准备退出
    public boolean readyToQuit(String msg){
        return QUIT.equalsIgnoreCase(msg);
    }

    public void close(){
        if(writer != null){
            try {
                writer.close();
                System.out.println("关闭socket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start(){
        try {
            // 创建socket
            socket = new Socket(DEFAULT_SERVER_HOST , DEFAULT_PORT);

            // 创建IO流
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );

            // 处理用户的输入
            new Thread(new UserInputHandler(this)).start();

            // 读取服务器转发的消息
            String msg = null;
            while((msg = receive()) != null){
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            close();
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }
}
