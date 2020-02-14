package tcp.bio.chatroom_threadpool.executor;

public class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println(this);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
