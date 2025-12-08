package chat.client;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;


/**
 * Основной класс интерфейса клиентской программы
 */
public class ClientGUI extends JFrame {


    private ChatBox chatBox;
    private SendBox sendBox;
    private LoginPopup loginPopup;


    private String username;
    private int port;
    private InetAddress address;


    private ChatClientSocket socket;

    public ClientGUI() {
        super();
    }

    /**
     * Запускает клиент
     */
    public void launch() {

        loginPopup = new LoginPopup(8080);
        socket = null;
        // Настройка основного окна
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800,600);
        setTitle("Chat");
        setLocationByPlatform(true);
        this.setVisible(true);

        // Получаем от пользователя данные для подключения
        int loginResult = loginPopup.run();

        // Если данные не были получены,
        // например пользователь закрыл окно,
        // то завершаем программу
        if (loginResult != JOptionPane.OK_OPTION) {
            this.quit();
            return;
        }

        // Сохраняем полученные данные
        this.username = loginPopup.getUsername();
        this.port = loginPopup.getPort();
        this.address = loginPopup.getAddress();
        setTitle(String.format("Chat | Logged in as [%s]",this.username));

        // Инициализация компонентов окна
        chatBox = new ChatBox();
        sendBox = new SendBox(this.username);



        add(chatBox, BorderLayout.CENTER);
        add(sendBox, BorderLayout.PAGE_END);
        pack();

        try {
            // Установка подключения
            this.socket = new ChatClientSocket(this.address, this.port, this.username);

            Runtime.getRuntime().addShutdownHook(new Thread(){
                public void run(){
                    try {
                        socket.close();
                    } catch (IOException e) {}
                }
            });

            this.socket.addListener(chatBox.getMessageListener());
            sendBox.addListener(this.socket.getMessageListener());
            sendBox.addListener(chatBox.getMessageListener());
            JOptionPane.showMessageDialog(this, "Connected successfully");
            this.setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e,"Error", JOptionPane.ERROR_MESSAGE);
            System.out.println(e);
            this.quit();
        }

    }

    /**
     * Завершает работу
     */
    public void quit() {
        this.dispose();
    }
}
