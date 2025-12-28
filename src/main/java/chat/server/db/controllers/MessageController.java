package chat.server.db.controllers;

import chat.server.db.models.Message;

import java.net.http.HttpResponse;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Класс-контроллер, позволяющий производить  базе данных действия, связанные с таблицей <code>messages</code>
 */
public class MessageController {
    private final Connection db;
    private final PreparedStatement getLatestMessagesStatement;
    private final PreparedStatement addMessageStatement;
    private final PreparedStatement getUsernameStatement;

    public MessageController(Connection db) throws SQLException {
        this.db = db;
        this.getLatestMessagesStatement = this.db.prepareStatement(
                "SELECT m.id, m.senderid, m.roomid, m.sentat, m.body, u.username " +
                        "FROM messages m " +
                        "INNER JOIN users u " +
                        "ON m.senderid = u.id " +
                        "WHERE m.roomid = ? " +
                        "ORDER BY sentat " +
                        "LIMIT 100"
        );
        this.addMessageStatement = this.db.prepareStatement(
                "INSERT INTO messages (senderid,roomid,body) VALUES (?,?,?) RETURNING *"
        );
        this.getUsernameStatement = this.db.prepareStatement(
                "SELECT username FROM users WHERE id = ?"

        );
    }

    /**
     * Возвращает список из 100 последних сообщений, отправленных в указанный чат
     * @param roomId
     * @return
     * @throws SQLException
     */
    public List<Message> getLatestMessages(int roomId) throws SQLException{
        this.getLatestMessagesStatement.setInt(1, roomId);
        ResultSet rst = this.getLatestMessagesStatement.executeQuery();
        System.out.println("query success");
        List<Message> result = new LinkedList<>();
        while (rst.next()) {
            result.add(new Message(
                    rst.getInt("id"),
                    rst.getInt("senderid"),
                    rst.getInt("roomid"),
                    rst.getTimestamp("sentat"),
                    rst.getString("username"),
                    rst.getString("body")
            ));
        }
        return result;
    }

    /**
     * Добавляет новое сообщение в указанный чат
     * @param senderId
     * @param roomId
     * @param body
     * @return
     * @throws SQLException
     */
    public Message addMessage(int senderId, int roomId, String body) throws SQLException{
        this.addMessageStatement.setInt(1,senderId);
        this.addMessageStatement.setInt(2,roomId);
        this.addMessageStatement.setString(3,body);
        ResultSet rst = this.addMessageStatement.executeQuery();
        rst.next();

        this.getUsernameStatement.setInt(1,rst.getInt("senderid"));
        ResultSet rst2 = this.getUsernameStatement.executeQuery();
        rst2.next();
        return new Message(
                rst.getInt("id"),
                rst.getInt("senderid"),
                rst.getInt("roomid"),
                rst.getTimestamp("sentat"),
                rst2.getString("username"),
                rst.getString("body")
        );
    }
}
