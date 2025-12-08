package chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Компонент, осуществляющий взаимодействие с сервером
 *
 */
public class ChatClientSocket extends Socket {

    private final PrintWriter out;
    private final BufferedReader in;
    private List<MessageListener> listeners;

    private MessageListener sendMsgListener;

    /**
     * Устанавливает подключение с сервером
     * @param address адрес, к которому нужно подключится
     * @param port порт, на котором открыт сервер
     * @param username юзернейм
     * @throws IOException ошибка при подключении
     */
    public ChatClientSocket(InetAddress address, int port, String username)
    throws IOException {
        // Установка подключения
        super(address, port);

        this.out = new PrintWriter(getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(getInputStream()));

        this.out.println(username);
        this.out.flush();

        this.listeners = new ArrayList<>();

        this.sendMsgListener = new MessageListener() {
            public void newMessage(MessageEvent evt) {
                String text = evt.getContent();
                out.println(text);
                out.flush();
            }
        };

        InputHandler h = new InputHandler(this.in);
        Thread t = new Thread(h);
        t.start();
    }

    /**
     * Добавляет <code>MessageListener</code>,
     * получающий оповещение при приеме нового сообщения от сервера
     * @param l
     */
    public void addListener(MessageListener l) {
        this.listeners.add(l);
    }

    /**
     * Возвращает <code>MessageListener</code>, отправляющий сообщение на сервер
     * @return
     */
    public MessageListener getMessageListener() {
        return sendMsgListener;
    }

    /**
     * Обработчик входящих сообщений, запускаемый в отдельном потоке
     */
    private class InputHandler implements Runnable {

        private final BufferedReader in;

        String serverInput;

        public InputHandler(BufferedReader in){
            this.in = in;
        }

        public void run() {
            try {
                // Цикл, обрабатывающий входящие сообщения
                // .readLine() блокирует поток до тех пор, пока от сервера не придет сообщение
                while((this.serverInput = this.in.readLine()) != null) {

                    // Формирование MessageEvent
                    String sender = serverInput.substring( 1, serverInput.indexOf("]"));
                    String msg = serverInput.substring( serverInput.indexOf("]")+1, serverInput.length());
                    MessageEvent evt = new MessageEvent(this, sender, msg);
                    // Отправка MessageEvent получателям
                    for (MessageListener l : listeners){

                        l.newMessage(evt);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
