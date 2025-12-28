package chat.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Класс, представляющий собой сообщение API
 * @param header
 * @param content
 */
public record ProtocolMessage(
        String header,
        JSONObject content
) {
    /**
     * Парсит сообщение
     * @param raw
     * @return
     * @throws ProtocolException
     */
    public static ProtocolMessage parse(String raw) throws ProtocolException {

        String temp[] = raw.split("\\?", 2);
        if(temp.length == 1) {
            return new ProtocolMessage(temp[0], new JSONObject());
        }

        if(temp[1].isEmpty()) {
            return new ProtocolMessage(temp[0], new JSONObject());
        }

        if(temp[0].isEmpty()){
            throw new ProtocolException("Empty Header");
        }

        try {
            JSONObject parsed = new JSONObject(temp[1]);
            return new ProtocolMessage(temp[0], parsed);
        } catch (JSONException e) {
            throw new ProtocolException(e.toString());
        }
    }

    /**
     * Возвращает сообщение в текстовом виде
     * @return
     */
    @Override
    public String toString() {
        return header+"?"+content.toString();
    }

}
