package chat.client;

import chat.util.ProtocolMessage;

/**
 * Интерфейс наблюдателя для сообщений типа {@link ProtocolMessage}
 *
 */
public interface ProtocolMessageListener{
    /**
     * Вызывается объектом для отправки сообщения
     * @see chat.util.ProtocolMessage
     * @param msg
     */
    void newMessage(ProtocolMessage msg);
}
