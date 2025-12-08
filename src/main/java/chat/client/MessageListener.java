package chat.client;

import java.util.EventListener;

/**
 * <code>MessageListener</code> может быть зарегистрирован у объекта
 * для получения сообщений
 */
public interface MessageListener extends EventListener {
    /**
     * Вызывается объектом для отправки сообщения
     * @see MessageEvent
     * @param event
     */
    void newMessage(MessageEvent event);
}
