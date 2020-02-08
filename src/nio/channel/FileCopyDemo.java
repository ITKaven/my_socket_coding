package nio.channel;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileCopyDemo {

    private static final int ROUNDS = 5;

    private static void benchmark(FileCopyRunner test ,
                                  File source , File target){
        long elapsed = 0L;
        for (int i = 0; i < ROUNDS; i++) {
            long startTime = System.currentTimeMillis();
            test.copyFile(source , target);
            elapsed += System.currentTimeMillis() - startTime;
            target.delete();
        }
        System.out.println(test+"："+(1.0F)*elapsed / ROUNDS);
    }

    public static void close(Closeable closeable){
        if(closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        FileCopyRunner noBufferStreamCopy  = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream fin = null;
                OutputStream fout = null;
                try {
                    fin = new FileInputStream(source);
                    fout = new FileOutputStream(target);

                    int result;
                    while((result = fin.read()) != -1){
                        fout.write(result);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    close(fin);
                    close(fout);
                }
            }

            @Override
            public String toString() {
                return "noBufferStreamCopy";
            }
        };

        FileCopyRunner bufferedStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream fin = null;
                OutputStream fout = null;
                try {
                    fin = new BufferedInputStream(new FileInputStream(source));
                    fout = new BufferedOutputStream(new FileOutputStream(target));

                    byte[] buffer = new byte[8192];

                    int result;
                    while((result = fin.read(buffer)) != -1){
                        fout.write(buffer , 0 ,result);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    close(fin);
                    close(fout);
                }
            }

            @Override
            public String toString() {
                return "bufferedStreamCopy";
            }
        };

        FileCopyRunner nioBufferCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                FileChannel fin = null;
                FileChannel fout = null;

                try {
                    fin = new FileInputStream(source).getChannel();
                    fout = new FileOutputStream(target).getChannel();

                    ByteBuffer buffer = ByteBuffer.allocate(8192);
                    while(fin.read(buffer) != -1){
                        buffer.flip(); //开始读模式
                        while(buffer.hasRemaining()){
                            fout.write(buffer);
                        }
                        buffer.clear(); // 开始写模式
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    close(fin);
                    close(fout);
                }
            }

            @Override
            public String toString() {
                return "nioBufferCopy";
            }
        };

        FileCopyRunner nioTransferCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                FileChannel fin = null;
                FileChannel fout = null;
                try {
                    fin = new FileInputStream(source).getChannel();
                    fout = new FileOutputStream(target).getChannel();

                    long transferred = 0;
                    long size = fin.size();
                    while(transferred != size){
                        transferred += fin.transferTo(0,size,fout);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    close(fin);
                    close(fout);
                }
            }

            @Override
            public String toString() {
                return "nioTransferCopy";
            }
        };

        File one = new File("E:\\test\\1.png");
        File oneCopy = new File("E:\\test\\1-copy.png");

        System.out.println("---Copying one---");
        benchmark(noBufferStreamCopy , one , oneCopy);
        benchmark(bufferedStreamCopy , one , oneCopy);
        benchmark(nioBufferCopy , one , oneCopy);
        benchmark(nioTransferCopy , one , oneCopy);

        File two = new File("E:\\test\\2.mp4");
        File twoCopy = new File("E:\\test\\2-copy.mp4");

        System.out.println("---Copying two---");
//        benchmark(noBufferStreamCopy , two , twoCopy);
        benchmark(bufferedStreamCopy , two , twoCopy);
        benchmark(nioBufferCopy , two , twoCopy);
        benchmark(nioTransferCopy , two , twoCopy);

        File three = new File("E:\\test\\3.mp4");
        File threeCopy = new File("E:\\test\\3-copy.mp4");

        System.out.println("---Copying three---");
//        benchmark(noBufferStreamCopy , three , threeCopy);
        benchmark(bufferedStreamCopy , three , threeCopy);
        benchmark(nioBufferCopy , three , threeCopy);
        benchmark(nioTransferCopy , three , threeCopy);

        File four = new File("E:\\test\\4.avi");
        File fourCopy = new File("E:\\test\\4-copy.avi");

        System.out.println("---Copying four---");
//        benchmark(noBufferStreamCopy , four , fourCopy);
        benchmark(bufferedStreamCopy , four , fourCopy);
        benchmark(nioBufferCopy , four , fourCopy);
        benchmark(nioTransferCopy , four , fourCopy);
    }
}
