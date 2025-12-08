package chat.client;

import java.util.EventObject;

/**
 * Событие, обозначающее отправку сообщения
 * @see MessageListener
 */
public class MessageEvent extends EventObject {

    /**
     * Отправитель сообщения
     */
    private String content;
    /**
     * Содержание сообщения
     */
    private String sender;

    /**
     * Возвращает содержание сообщения
     * @return содержание сообщения
     */
    public String getContent(){
        return content;
    }
    /**
     * Возвращает отправителя сообщения
     * @return отправитель сообщения
     */
    public String getSender(){
        return sender;
    }

    /**
     * @param source источник сообщения. Наследовано от {@link EventObject}
     * @param sender отправитель сообщения
     * @param content содержание сообщения
     */
    public MessageEvent(Object source, String sender, String content) {
        super(source);
        this.content = content;
        this.sender = sender;
    }
}
