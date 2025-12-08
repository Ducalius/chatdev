package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.Principal;

public class Session implements Runnable {

    private PrintWriter out;
    private BufferedReader in;

    public final static ScopedValue<Principal> PRINCIPAL = ScopedValue.newInstance();
    // Разделить ввод и вывод на два отдельных потока,
    // Так как вебсокеты позволяют это делать
    @Override
    public void run() {
        try {
            this.out = new PrintWriter(
                    Server.SOCKET.get().getOutputStream(), true
            );
            this.in = new BufferedReader(
                    new InputStreamReader(Server.SOCKET.get().getInputStream())
            );
            String clientInput;
            while((clientInput = this.in.readLine()) != null) {

            }
        } catch (IOException e) {}
    }
}
