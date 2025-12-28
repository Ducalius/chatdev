package chat.server.db.controllers;

import chat.server.db.models.Room;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Класс-контроллер, позволяющий производить  базе данных действия, связанные с таблицей <code>rooms</code>
 */
public class RoomController {
    private final Connection db;
    private final PreparedStatement listRoomsStatement;

    public RoomController(Connection db) throws SQLException{
        this.db = db;
        this.listRoomsStatement = this.db.prepareStatement(
                "SELECT * FROM rooms"
        );
    }

    /**
     * Возвращает список доступных чатов
     * @return
     * @throws SQLException
     */
    public List<Room> listRooms() throws SQLException {
        ResultSet rst = this.listRoomsStatement.executeQuery();
        List<Room> result = new LinkedList<>();
        while (rst.next()) {
            result.add(new Room(
                    rst.getInt("id"),
                    rst.getString("roomname")
            ));
        }
        return result;
    }
}
