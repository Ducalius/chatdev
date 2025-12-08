package chat;

import chat.client.ClientGUI;
import chat.server.ChatServerSocket;

import java.io.IOException;

/**
 * Основной класс
 * Для запуска клиента: Lab11.jar
 * Для запуска сервера: Lab11.jar [port]
 */
public class Main {
    public static void main(String[] args) {
        // Если нет аргументов, то запускаем клиент
        if(args.length == 0) {
            ClientGUI gui = new ClientGUI();
            gui.launch();
        }
        // Иначе, запускаем сервер
        else {
            int port;
            // Валидация номера порта, переданного пользователем
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Incorrect port number");
                System.out.println("Usage:");
                System.out.println("    To start client: Lab11.jar");
                System.out.println("    To start server: Lab11.jar [port]");
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