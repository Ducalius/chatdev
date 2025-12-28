package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Класс, инициализирующий ресурсы, необходимые для {@link Server}, и запускающий его
 */
public class ServerMain {

    public static void main(String[] args) {

        Connection db;
        Logger log = LoggerFactory.getLogger(ServerMain.class);
        try {
            String DB_URL = System.getProperty("DATABASE_CONNECTION_URL");
            String DB_USER = System.getProperty("DATABASE_USER_NAME");
            String DB_PASS = System.getProperty("DATABASE_USER_PASSWORD");
            int SERVER_PORT = Integer.parseInt(System.getProperty("CHAT_SERVER_PORT"));

            if (DB_URL.isEmpty()) {
                log.error("Environment variable DATABASE_CONNECTION_URL is unset");
            }
            if (DB_USER.isEmpty()) {
                log.error("Environment variable DATABASE_USER_NAME is unset");
            }
            if (DB_PASS.isEmpty()) {
                log.error("Environment variable DATABASE_USER_PASSWORD is unset");
            }
             db = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Thread.startVirtualThread(new Server(SERVER_PORT, db, log));
        } catch (Exception e) {
            log.error(e.toString());
            return;
        }
        BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
        String serverInput;
        try {
            do {
                serverInput = systemIn.readLine();
                if(serverInput == "quit\n") System.out.println(serverInput);
            } while (!serverInput.contains("quit"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
