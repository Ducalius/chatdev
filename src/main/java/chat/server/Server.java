package chat.server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private int port;
    public final static ScopedValue<Socket> SOCKET = ScopedValue.newInstance();

    public Server(){
        port = 8080;
    }
    @Override
    public void run(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket sessionSocket = serverSocket.accept();
                ScopedValue.where(SOCKET, sessionSocket)
                        .run(()-> new Session().run());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
