package chat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Компонент, предоставляющий пользователю интерфейс для отправки сообщений
 */
public class SendBox extends JPanel {
    //private final JLabel usernameLabel;
    private final JButton sendBtn;
    private final JTextField msgField;

    private List<MessageListener> listeners;

    /**
     *
     * @param username юзернейм
     */
    public SendBox(String username) {
        // Настройка интерфейса
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.setLayout(new BorderLayout());
        //this.usernameLabel = new JLabel(username);
        this.msgField = new JTextField(1);
        this.listeners = new ArrayList<>();
        this.sendBtn = new JButton("Send");
        //add(this.usernameLabel, BorderLayout.WEST);
        add(this.msgField, BorderLayout.CENTER);
        add(this.sendBtn, BorderLayout.EAST);

        // Обработчик события ActionEvent,
        // возникающего когда пользователь хочет отправить сообщение
        ActionListener onSend = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                msgField.requestFocus();
                String msg = msgField.getText();
                msgField.setText("");
                // Если пользователь ничего не ввел, то сообщение не отправляется
                if(msg.length() < 1) return;
                // Формирование MessageEvent
                MessageEvent evt = new MessageEvent(this, username, msg);
                // Отправка сообщения слушателям
                for (MessageListener l : listeners){
                    l.newMessage(evt);
                }
            }
        };

        msgField.addActionListener(onSend);
        sendBtn.addActionListener(onSend);

    }

    /**
     * Добавляет <code>MessageListener</code> получающий
     * оповещение при отправке сообщения пользователем
     * @param l
     */
    public void addListener(MessageListener l) {
        this.listeners.add(l);
    }


}
