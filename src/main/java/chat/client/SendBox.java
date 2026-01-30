package chat.client;

import chat.util.ProtocolMessage;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Компонент, предоставляющий пользователю интерфейс для отправки сообщений
 */
private class SendBox extends JPanel {
    //private final JLabel usernameLabel;
    private final JButton sendBtn;
    private final JTextField msgField;
    private int currentRoomId = -1;

    private List<ProtocolMessageListener> listeners;

    private final TreeSelectionListener switchRoom;

    public TreeSelectionListener getTreeSelectionListener() {
        return switchRoom;
    }

    public SendBox() {
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
                String msgText = msgField.getText();
                msgField.setText("");
                // Если пользователь ничего не ввел, то сообщение не отправляется
                if(msgText.length() < 1) return;
                if(currentRoomId == -1) return;
                // Формирование ProtocolMessageEvent
                JSONObject content = new JSONObject();
                content.put("content", msgText);
                content.put("roomId", currentRoomId);
                ProtocolMessage msg = new ProtocolMessage("sendMessage", content);
                //MessageEvent evt = new MessageEvent(this, username, msg,  MessageEvent.MessageType.OUT);
                // Отправка сообщения слушателям
                for (ProtocolMessageListener l : listeners){
                    l.newMessage(msg);
                }
            }
        };

        switchRoom = new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                RoomNode node = (RoomNode)e.getPath().getLastPathComponent();
                currentRoomId = node.getRoomId();
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
    public void addListener(ProtocolMessageListener l) {
        this.listeners.add(l);
    }


}
