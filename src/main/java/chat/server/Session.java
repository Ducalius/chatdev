package chat.server;

import chat.server.db.models.Message;
import chat.server.db.models.Room;
import chat.server.db.models.User;
import chat.util.ProtocolMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * Класс, отвечающий за взаимодействие с конкретным клиентом
 */
public class Session implements Runnable {

    private BufferedWriter out;
    private BufferedReader in;
    private final Socket socket;
    private User user = null;
    public final BlockingQueue<OutMessage> queue;
    // Разделить ввод и вывод на два отдельных потока,
    // Так как вебсокеты позволяют это делать

    public Session(Socket socket){
        this.socket = socket;
        this.queue = new LinkedBlockingQueue<>();
        try {
            this.out = new BufferedWriter(
                    new OutputStreamWriter(this.socket.getOutputStream())
            );
            this.in = new BufferedReader(
                    new InputStreamReader(this.socket.getInputStream())
            );

            Thread.startVirtualThread(() -> {
                try {
                    Session.OutMessage message;
                    while (true) {
                        message = queue.take();
                        if (message.type == Session.OutMessage.MessageType.CLOSE) {
                            break;
                        }
                        out.write(message.content);
                        out.newLine();
                        out.flush();
                    }
                } catch (Exception e) {
                    Server.LOG.warn("Session writer terminated with exception: {}", e.toString());
                }
            });

        } catch (IOException e) {}
    }

    /**
     * Запускает процесс приема и обработки сообщений от клиента
     */
    @Override
    public void run() {
        // Состояние "неавторизован"
        try {
            String clientInputRaw;
            ProtocolMessage clientMessage;
            Server.LOG.debug("Session with {} started", socket.getInetAddress().toString());
            while ((clientInputRaw = this.in.readLine()) != null) {
                try {
                    clientMessage = ProtocolMessage.parse(clientInputRaw);
                    switch (clientMessage.header()) {
                        case ("register") -> {
                            if(!EmailValidator.getInstance().isValid((String)clientMessage.content().get("email"))) {
                                sendMessage(
                                        "registerFailure",
                                        "Invalid Email"
                                );
                                continue;
                            }
                            Server.userController.register(
                                    clientMessage.content().getString("username"),
                                    clientMessage.content().getString("password"),
                                    clientMessage.content().getString("email")
                            );
                            sendMessage(
                                    "registerSuccess",
                                    "Registered Successfully"
                            );
                            Server.LOG.info("User {} registered succesfully", clientMessage.content().getString("username"));
                            break;
                        }
                        case ("login") -> {
                            boolean loginResult = Server.userController.login(
                                    clientMessage.content().getString("username"),
                                    clientMessage.content().getString("password")
                            );
                            if (loginResult) {
                                User user = Server.userController.getUserByName(
                                        (String)clientMessage.content().get("username")
                                );
                                this.user = user;
                                sendMessage(
                                        "loginSuccess",
                                        user.toJSON()
                                );

                            } else {
                                sendMessage(
                                        "loginFailure",
                                        "Login failed"
                                );

                            }
                            Server.LOG.info("User {} logged in succesfully", clientMessage.content().getString("username"));
                            break;
                        }
                        default -> {
                            sendErrorMessage("Bad Request");
                        }
                    }
                    // Если пользователь авторизовался, производится переход в состояние "авторизован"
                    if (user != null) break;
                } catch (Exception e) {
                    sendErrorMessage("Bad Request");
                    continue;
                }
            }

            //Состояние "авторизован"
            while ((clientInputRaw = this.in.readLine()) != null) {
                try {
                    clientMessage = ProtocolMessage.parse(clientInputRaw);
                    switch (clientMessage.header()) {
                        case ("listRooms") -> {
                            List<Room> rooms = Server.roomController.listRooms();
                            JSONArray roomsJson = new JSONArray(
                                    rooms.stream().map(e -> e.toJSON()).toList()
                            );
                            JSONObject content = new JSONObject();
                            content.put("rooms", roomsJson);
                            sendMessage(
                                    "listRoomsSuccess",
                                    content
                            );


                        }
                        case ("subscribe") -> {
                            Server.roomSubscribe(this, clientMessage.content().getInt("roomId"));

                            sendMessage(
                                    "subscribeSuccess",
                                    "Subscribed Successfully"
                            );
                        }
                        case ("unsubscribe") -> {
                            Server.roomUnsubscribe(this, clientMessage.content().getInt("roomId"));
                            sendMessage(
                                    "unsubscribeSuccess",
                                    "Unsubscribed Successfully"
                            );
                        }
                        case ("listMessages") -> {
                            List<Message> messages = Server.messageController.getLatestMessages(clientMessage.content().getInt("roomId"));
                            JSONArray messagesJson = new JSONArray(
                                    messages.stream().map(e -> e.toJSON()).toList()
                            );
                            JSONObject content = new JSONObject();
                            content.put("messages", messagesJson);
                            content.put("roomId", clientMessage.content().getInt("roomId"));
                            sendMessage(
                                    "listMessagesSuccess",
                                    content
                            );

                        }
                        case ("sendMessage") -> {
                            Message message = Server.messageController.addMessage(
                                    this.user.id(),
                                    clientMessage.content().getInt("roomId"),
                                    clientMessage.content().getString("content")
                            );
                            Server.broadcast(
                                    this,
                                new OutMessage(
                                        new ProtocolMessage("message", message.toJSON()).toString(),
                                        OutMessage.MessageType.OUT
                                ),
                                    message.roomId()
                            );
                            sendMessage(
                                    "messageSuccess",
                                    message.toJSON()
                            );
                            Server.LOG.info(
                                    "Client [{} {} {}] sent a message",
                                    socket.getInetAddress(),
                                    user.id(),
                                    user.username()
                            );

                        }
                        default -> {
                            sendErrorMessage("Bad Request");
                        }
                    }
                } catch (Exception e) {
                    sendErrorMessage("Bad Request");
                    continue;
                }
            }
        } catch (IOException e) {} finally {
            try {
                this.socket.close();
            } catch (Exception e) {}
        }
        // Завершение сессии
        Server.roomUnsubscribeAll(this);
        Server.LOG.info(
                "Session closed with {}, logged in as {} {}",
                socket.getInetAddress(),
                user.id(),
                user.username()
        );
    }

    /**
     * Класс сообщения, подлежащего отправке клиенту
     */
    public final static class OutMessage {
        public enum MessageType {OUT, CLOSE}

        public final String content;
        public final MessageType type;

        public OutMessage(String content, MessageType type) {
            this.content = content;
            this.type = type;
        }

        public OutMessage closeMessage() {
            return new OutMessage("", MessageType.CLOSE);
        }

    }

    /**
     * Добавляет сообщение в очередь на отправку клиенту
     * @param message
     * @return
     */
    public boolean sendMessage(OutMessage message){
        System.out.println(message.content);
        return queue.offer(message);
    }

    /**
     * Добавляет сообщение в очередь на отправку клиенту
     * @param header
     * @param content
     * @return
     */
    public boolean sendMessage(String header, JSONObject content){
        return sendMessage(new OutMessage(
                new ProtocolMessage(header, content).toString(),
                OutMessage.MessageType.OUT)
        );
    }

    /**
     * Добавляет сообщение в очередь на отправку клиенту
     * @param header
     * @param message
     * @return
     */
    public boolean sendMessage(String header, String message){
        JSONObject content = new JSONObject();
        content.put("text", message);
        return sendMessage(header,content);
    }

    /**
     * Добавляет сообщение об ошибке в очередь на отправку клиенту
     * @param error
     * @return
     */
    public boolean sendErrorMessage(String error) {
        return sendMessage("error", error);
    }
}
