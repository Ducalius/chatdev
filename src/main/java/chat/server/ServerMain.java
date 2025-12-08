package chat.server;

import java.io.IOException;

public class ServerMain {
    public static void printHelp(){
        System.out.println("Usage:");
        System.out.println("    To start client: Lab11.jar");
        System.out.println("    To start server: Lab11.jar [port]");

    }
    public static void main(String[] args) {
        if(args.length == 0) {
            printHelp();
        }
        // Иначе, запускаем сервер
        else {
            int port;
            // Валидация номера порта, переданного пользователем
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Incorrect port number");
                printHelp();
                return;
            }
            // Запуск сервера
            try (ChatServerSocket server = new ChatServerSocket(port)) {
                server.start();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
