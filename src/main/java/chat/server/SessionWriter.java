package chat.server;

import java.io.BufferedWriter;
import java.util.concurrent.BlockingQueue;

public class SessionWriter implements Runnable {

    private final BufferedWriter out;
    public final BlockingQueue<Session.OutMessage> queue;


    public SessionWriter(BufferedWriter out, BlockingQueue<Session.OutMessage> queue){
        this.out = out;
        this.queue = queue;
    }

    @Override
    public void run() {
        Session.OutMessage message;
        try {
            while (true) {
                message = queue.take();
                if (message.type == Session.OutMessage.MessageType.CLOSE) {
                    break;
                }
                out.write(message.content, 0, message.content.length());
                out.flush();
            }
        } catch (Exception e) {}
        finally {
            try{
                out.close();
            } catch (Exception e) {}

        }
    }

}
