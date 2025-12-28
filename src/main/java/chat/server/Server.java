package chat.server;

import chat.server.db.controllers.*;
import chat.server.db.models.Room;
import org.slf4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Класс синглтон, являющийся главным классом серверного приложения
 */
public class Server implements Runnable {
    private final int port;
    static private Connection dbConnection;
    static UserController userController;
    static RoomController roomController;
    static MessageController messageController;
    static Logger LOG;
    static final ConcurrentSkipListMap<
                Integer,
                ConcurrentLinkedQueue<Session>
                > ROOMS_MAP = new ConcurrentSkipListMap<>();

    public Server(int port, Connection db, Logger log){
        this.port = port;
        LOG = log;
        dbConnection = db;
        List<Room> rooms;
        try {
            userController = new UserController(dbConnection);
            roomController = new RoomController(dbConnection);
            messageController = new MessageController(dbConnection);
            rooms = roomController.listRooms();
            for (Room room : rooms) {
                ROOMS_MAP.put(room.id(), new ConcurrentLinkedQueue<>());
            }
        } catch (SQLException e) {
            System.out.println(e);
        }

    }

    /**
     * Запускает процесс приема подключений от клиентов
     */
    @Override
    public void run(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket sessionSocket = serverSocket.accept();
                Thread.startVirtualThread(new Session(sessionSocket));
            }
        } catch (Exception e) {
            System.out.println(e);
            LOG.warn(e.toString());
        }
    }

    /**
     * Возвращает список сессий клиентов, подписанных на указанный чат
     * @param roomId
     * @return
     */
    public static Iterable<Session> getRoomMembers(int roomId) {
        return ROOMS_MAP.get(roomId);
    }

    /**
     * Рассылает сообщение подписчика указанного чата
     * @param sender - отправитель, который будет исключен из рассылки
     * @param message
     * @param roomId
     */
    public static void broadcast(Session sender, Session.OutMessage message, int roomId) {
        Iterable<Session> room = getRoomMembers(roomId);

        for (Session session : room) {
            if (session != sender) {
                session.sendMessage(message);
            }
        }
    }

    /**
     * Подписывает клиента на рассылку указанного чата
     * @param session
     * @param roomId
     */
    public static void roomSubscribe(Session session, int roomId) {
        ROOMS_MAP.get(roomId).add(session);
    }
    /**
     * Отписывает клиента от рассылки указанного чата
     * @param session
     * @param roomId
     */
    public static void roomUnsubscribe(Session session, int roomId) {
        ROOMS_MAP.get(roomId).remove(session);
    }

    /**
     * Отписывает клиента всех чатов
     * @param session
     */
    public static void roomUnsubscribeAll(Session session) {
        for (Map.Entry<Integer, ConcurrentLinkedQueue<Session>> room : ROOMS_MAP.entrySet())
            room.getValue().remove(session);
    }
}
