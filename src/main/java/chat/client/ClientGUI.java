package chat.client;

import org.json.JSONArray;
import org.json.JSONObject;

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
    private RoomTreeBox roomTreeBox;

    private ConnectPopup connectPopup;
    private LoginPopup loginPopup;
    private RegisterPopup registerPopup;


    private String username;
    private int port;
    private InetAddress address;


    private static ChatClientSocket socket = null;

    public ClientGUI() {
        super();
    }

    /**
     * Запускает клиент
     */
    public void launch() {

        connectPopup = new ConnectPopup(8080);
        loginPopup = new LoginPopup();
        registerPopup = new RegisterPopup();
        socket = null;
        // Настройка основного окна
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800,600);
        setTitle("Chat");
        setLocationByPlatform(true);
        this.setVisible(true);

        while(true) {
            // Получаем от пользователя данные для подключения
            int action = connectPopup.run();

            // Если данные не были получены,
            // например пользователь закрыл окно,
            // то завершаем программу
            if (action != JOptionPane.OK_OPTION) {
                this.quit();
                return;
            }

            this.port = connectPopup.getPort();
            this.address = connectPopup.getAddress();

            try {
                // Установка подключения
                socket = new ChatClientSocket(this.address, this.port);

                Runtime.getRuntime().addShutdownHook(new Thread(){
                    public void run(){
                        try {
                            socket.close();
                        } catch (IOException e) {}
                    }
                });

                JOptionPane.showMessageDialog(this, "Connected successfully");
                break;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e,"Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        String state = "login";
        String error = "";
        while (true) {
            System.out.println(state);
            switch (state){
                case "login": {
                    LoginPopup.UserAction action = loginPopup.run(error);
                    if(action == LoginPopup.UserAction.REGISTER){
                        state = "register";
                        error = "";
                        loginPopup.clearPassword();
                        continue;
                    }
                    if (action == LoginPopup.UserAction.CLOSE) {
                        this.quit();
                        return;
                    }
                    JSONObject loginResult;
                    try {
                        loginResult = socket.login(loginPopup.getUsername(), loginPopup.getPassword());
                    } catch (Exception e) {
                        loginResult = null;
                    }
                    if(loginResult == null) {
                        error = "Invalid credentials";
                        loginPopup.clearPassword();
                        continue;
                    }
                    this.username = loginResult.getString("username");
                    state = "logged in";
                    break;
                }
                case "register": {
                    RegisterPopup.UserAction action = registerPopup.run(error);
                    if(action == RegisterPopup.UserAction.LOGIN){
                        state = "login";
                        error = "";
                        registerPopup.clearPassword();
                        continue;
                    }
                    if (action == RegisterPopup.UserAction.CLOSE) {
                        this.quit();
                        return;
                    }
                    boolean registerResult;
                    try {
                         registerResult = socket.register(
                                registerPopup.getUsername(),
                                registerPopup.getPassword(),
                                registerPopup.getEmail()
                        );
                    } catch (Exception e) {
                        registerResult = false;
                    }
                    if(!registerResult) {
                        error = "Registration error";
                        loginPopup.clearPassword();
                        continue;
                    }
                    JOptionPane.showMessageDialog(this, "Registered successfully!");
                    state = "login";
                    registerPopup.clearAll();
                }
            }
            if(state == "logged in") break;
        }
        JSONArray roomsListJson;
        try {
            roomsListJson = socket.listRooms().content().getJSONArray("rooms");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Server Error","Error", JOptionPane.ERROR_MESSAGE);
            this.quit();
            return;
        }

        chatBox = new ChatBox();
        sendBox = new SendBox();
        roomTreeBox = new RoomTreeBox();

        sendBox.addListener(socket.getProtocolMessageListener());
        socket.addListener(chatBox.getProtocolMessageListener());
        socket.addListener(roomTreeBox.getProtocolMessageListener());

        roomTreeBox.addSelectionListener(sendBox.getTreeSelectionListener());
        roomTreeBox.addSelectionListener(chatBox.getTreeSelectionListener());

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(chatBox, BorderLayout.CENTER);
        chatPanel.add(sendBox, BorderLayout.PAGE_END);

        add(roomTreeBox, BorderLayout.WEST);
        add(chatPanel, BorderLayout.CENTER);

        socket.startSession();


        for(Object roomObject : roomsListJson){
            JSONObject room = (JSONObject)roomObject;
            String roomName = room.getString("name");
            int roomId = room.getInt("id");

            roomTreeBox.addRoom(roomId, roomName);
            chatBox.addRoom(roomId, roomName);
            try {
                socket.listMessages(roomId);
                socket.subscribe(roomId);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Server Error","Error", JOptionPane.ERROR_MESSAGE);
                this.quit();
                return;
            }
        }
        this.roomTreeBox.selectFirstNode();

        setTitle(String.format("Chat | Logged in as [%s]",this.username));

        // Инициализация компонентов окна



        pack();

        //socket.addListener(chatBox.getMessageListener());
        //sendBox.addListener(socket.getMessageListener());
        //sendBox.addListener(chatBox.getMessageListener());
        this.setVisible(true);
        this.repaint();

    }

    /**
     * Завершает работу
     */
    public void quit() {
        this.dispose();
    }
}
