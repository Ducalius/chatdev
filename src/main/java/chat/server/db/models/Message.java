package chat.server.db.models;

import java.sql.Timestamp;
import org.json.*;

/**
 * Класс-модель, отображающий строку в таблице messages
 * @param id
 * @param senderId
 * @param roomId
 * @param timestamp
 * @param senderName
 * @param content
 */
public record Message(
        int id,
        int senderId,
        int roomId,
        Timestamp timestamp,
        String senderName,
        String content
) {
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        result.put("id", id);
        result.put("senderId", senderId);
        result.put("senderName", senderName);
        result.put("roomId", roomId);
        result.put("timestamp", timestamp.toString());
        result.put("content", content);
        return result;
    }
}
