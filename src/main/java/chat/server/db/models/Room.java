package chat.server.db.models;

import org.json.JSONObject;

/**
 * Класс-модель, отображающий строку в таблице rooms
 * @param id
 * @param name
 */
public record Room(
        int id,
        String name
) {
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        result.put("id", id);
        result.put("name", name);
        return result;
    }
}
