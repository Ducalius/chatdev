package chat.server.db.models;

import org.json.JSONObject;

/**
 * Класс-модель, отображающий строку в таблице users
 * @param id
 * @param username
 * @param role
 * @param email
 */
public record User(
        int id,
        String username,
        String role,
        String email
) {
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        result.put("id", id);
        result.put("username", username);
        result.put("role", role);
        result.put("email", email);
        return result;
    }
}