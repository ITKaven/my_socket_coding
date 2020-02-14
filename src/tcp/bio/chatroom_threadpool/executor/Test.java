package tcp.bio.chatroom_threadpool.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {
    private static ExecutorService executorService = Executors.newFixedThreadPool(3);

    public static void main(String[] args) {

        for (int i = 0; i < 10; i++) {
            executorService.execute(new MyRunnable());
        }
    }
}
