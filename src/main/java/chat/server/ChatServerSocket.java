package chat.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServerSocket extends ServerSocket{
    private final int port;

    private static final CopyOnWriteArrayList<Session> sessions = new CopyOnWriteArrayList<>();

    public ChatServerSocket(int port) throws IOException {
        super(port);
        this.port = port;
        System.out.printf("Server started on port %d\n", this.port);
    }

    /**
     * Запускает основной цикл. Метод <code>.accept()</code>
     * блокирует поток, пока не будет установлено новое соединение.
     * В результате установки соединения получается экземпляр <code>Socket</code>,
     * с помощью которого создается новый <code>Session</code> и добавляется в список сессий
     *
     * @throws IOException
     */
    public void start() throws IOException{
        while(true) {
            Socket sessionSocket = this.accept();
            System.out.printf("Client [%s] connected\n", sessionSocket.getInetAddress().getHostAddress());

            Session session = new Session(sessionSocket);
            new Thread(session).start();
            sessions.add(session);

        }
    }

    /**
     * Отправляет сообщение <code>msg</code> во все сессии <code>Session</code>,
     * кроме <code>sender</code>, если он указан
     * @param msg текст сообщения
     * @param sender отправитель. Если не null, то ему сообщение не отправляется
     */
    public void broadcast(String msg, Session sender) {
        System.out.println(msg);
        for (Session session : sessions) {
            if (session != sender)
                session.sendMsg(msg);
        }
    }

    /**
     * Код, реализующий взаимодействие с клиентом
     */
     public class Session implements Runnable {
        private final Socket sessionSocket;
        private final PrintWriter out;
        private final BufferedReader in;
        private String username;

        /**
         *
         * @param socket установленное с клиентом подключение, полученное в результате <code>.accept()</code>
         * @see ServerSocket
         * @throws IOException
         */
        public Session(Socket socket) throws IOException {
            this.sessionSocket = socket;
            this.out = new PrintWriter(
                    this.sessionSocket.getOutputStream(), true
            );
            this.in = new BufferedReader(
                    new InputStreamReader(this.sessionSocket.getInputStream())
            );

        }

        @Override
        public void run() {
            try {
                // Считывание имени пользователя
                this.username = in.readLine();
                // Оповещение остальных клиентов о подключении
                broadcast(
                        String.format("[SERVER]%s has joined!", this.username),
                        this
                );

                String clientInput;
                // Цикл, обрабатывающий входящие сообщения
                // .readLine() блокирует поток до тех пор, пока от клиента не придет сообщение
                while((clientInput = this.in.readLine()) != null) {
                    broadcast(
                            String.format("[%s]%s", this.username,clientInput),
                            this
                    );
                }

            } catch (IOException e) {
                System.out.println(e);
            } finally {
                sessions.remove(this);
                try {
                    in.close();
                    out.close();
                    sessionSocket.close();
                    System.out.printf("Client [%s] disconnected\n", username);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }

        /**
         * Отправляет сообщение <code>msg</code> клиенту
         * @param msg текст сообщения
         */
        public void sendMsg(String msg) {
            out.println(msg);
        }
    }
}
