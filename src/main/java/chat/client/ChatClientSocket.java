package chat.client;

import chat.util.ProtocolException;
import chat.util.ProtocolMessage;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Компонент, осуществляющий взаимодействие с сервером
 *
 */
public class ChatClientSocket extends Socket {

    private final PrintWriter out;
    private final BufferedReader in;
    private List<ProtocolMessageListener> listeners;

    private JSONObject userData = null;

    private ProtocolMessageListener sendMsgListener;

    /**
     * Устанавливает подключение с сервером
     * @param address адрес, к которому нужно подключится
     * @param port порт, на котором открыт сервер
     * @throws IOException ошибка при подключении
     */
    public ChatClientSocket(InetAddress address, int port)
    throws IOException {
        // Установка подключения
        super(address, port);

        this.out = new PrintWriter(getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(getInputStream()));

        this.listeners = new ArrayList<>();

        this.sendMsgListener = new ProtocolMessageListener() {
            public void newMessage(ProtocolMessage msg) {
                try {
                    send(msg);
                } catch (IOException e) {}
            }
        };
    }

    /**
     * Запускает поток, считывающий сообщения от сервера
     */
    public void startSession(){
        InputHandler h = new InputHandler(this.in);
        Thread t = new Thread(h);
        t.start();
    }

    /**
     * Отправляет запрос на сервер
     * @param msg
     * @throws IOException
     */
    public void send(ProtocolMessage msg) throws IOException {
        this.out.println(msg.toString());
        this.out.flush();

    }

    /**
     * Отправляет запрос на сервер
     * @param header
     * @param content
     * @throws IOException
     */
    public void send(String header, JSONObject content) throws IOException {
        ProtocolMessage requestMessage = new ProtocolMessage(header, content);
        send(requestMessage);
    }

    /**
     * Отправляет запрос на сервер, дожидается ответа и возвращает его
     * @param msg
     * @return
     * @throws IOException
     */
    public ProtocolMessage request(ProtocolMessage msg) throws IOException {
        send(msg);
        String response = this.in.readLine();
        return ProtocolMessage.parse(response);
    }

    /**
     * Отправляет запрос на сервер, дожидается ответа и возвращает его
     * @param header
     * @param content
     * @return
     * @throws IOException
     */
    public ProtocolMessage request(String header, JSONObject content) throws IOException {
        send(header, content);
        String response = this.in.readLine();
        return ProtocolMessage.parse(response);
    }

    /**
     * Отправляет на сервер запрос на авторизацию, дожидается ответа и возвращает результат авторизации
     * @param username
     * @param password
     * @return JSONObject, содержащий данные пользователя если авторизация прошла успешно
     * @return null, если авторизация не удалась
     * @throws IOException
     */
    public JSONObject login(String username, String password) throws IOException {
        JSONObject content = new JSONObject();
        content.put("username",username);
        content.put("password",password);

        ProtocolMessage response = request("login", content);
        if (Objects.equals(response.header(), "loginSuccess")) {
            this.userData = response.content();
            return response.content();
        }
        return null;
    }

    /**
     * Отправляет на сервер запрос на регистрацию, дожидается ответа и возвращает результат регистрации
     * @param username
     * @param password
     * @param email
     * @return true - успешная регистрация, false - ошибка при регистрации
     * @throws IOException
     */
    public boolean register(String username, String password, String email) throws IOException {
        JSONObject content = new JSONObject();
        content.put("username",username);
        content.put("password",password);
        content.put("email",email);

        ProtocolMessage response = request("register", content);
        System.out.println(response.header());
        return Objects.equals(response.header(), "registerSuccess");
    }

    /**
     * Отправляет на сервер запрос на получение списка чатов, дожидается ответа и возвращает результат
     * @return
     * @throws IOException
     */
    public ProtocolMessage listRooms() throws IOException {
        return request("listRooms", new JSONObject());
    }

    /**
     * Отправляет на сервер запрос на получение списка сообщений в указанном чате
     * @param roomId
     * @throws IOException
     */
    public void listMessages(int roomId) throws IOException {
        JSONObject content = new JSONObject();
        content.put("roomId",roomId);

        send("listMessages", content);
    }

    /**
     * Отправляет на сервер запрос на подписку на указанный чат
     * @param roomId
     * @throws IOException
     */
    public void subscribe(int roomId) throws IOException {
        JSONObject content = new JSONObject();
        content.put("roomId",roomId);

        send("subscribe", content);
    }

    /**
     * Отправляет на сервер запрос на отписку от указанного чата
     * @param roomId
     * @throws IOException
     */
    public void unsubscribe(int roomId) throws IOException {
        JSONObject content = new JSONObject();
        content.put("roomId",roomId);

        send("unsubscribe", content);
    }

    /**
     * Регистрирует {@link ProtocolMessageListener}
     * получающий оповещение при приеме нового сообщения от сервера
     * @param l
     */
    public void addListener(ProtocolMessageListener l) {
        this.listeners.add(l);
    }

    /**
     * Возвращает {@link ProtocolMessageListener}, отправляющий сообщение на сервер
     * @return
     */
    public ProtocolMessageListener getProtocolMessageListener() {
        return sendMsgListener;
    }

    /**
     * Обработчик входящих сообщений, запускаемый в отдельном потоке
     */
    private class InputHandler implements Runnable {

        private final BufferedReader in;

        String serverInput;

        public InputHandler(BufferedReader in){
            this.in = in;
        }

        public void run() {
            try {
                // Цикл, обрабатывающий входящие сообщения
                // .readLine() блокирует поток до тех пор, пока от сервера не придет сообщение
                while((this.serverInput = this.in.readLine()) != null) {
                    // Формирование MessageEvent
                    try {
                        ProtocolMessage msg = ProtocolMessage.parse(serverInput);

                        // Отправка MessageEvent получателям
                        for (ProtocolMessageListener l : listeners) {
                            l.newMessage(msg);
                        }
                    } catch (ProtocolException e) {}
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
