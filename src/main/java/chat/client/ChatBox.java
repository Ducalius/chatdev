package chat.client;

import chat.util.ProtocolMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Компонент, отображающий сообщения
 */
private class ChatBox extends JPanel {

    private final JPanel textArea = new JPanel(new CardLayout());

    private final ProtocolMessageListener appendNewMsg;
    private final TreeSelectionListener switchRoom;

    private int currentRoomId = -1;

    private final HashMap<Integer, JScrollPane> roomContentMap = new LinkedHashMap<>();
    private final HashMap<String, Integer> roomTreeMap = new LinkedHashMap<>();


    /**
     * Возвращает наблюдателя для сообщений типа {@link ProtocolMessage}, добавляющего сообщение в соответствующий чат
     * @return
     */
    public ProtocolMessageListener getProtocolMessageListener() {
        return appendNewMsg;
    }

    /**
     * Возвращает наблюдателя для сообщений типа {@link TreeSelectionEvent}, переключающего активный чат
     * @return
     */
    public TreeSelectionListener getTreeSelectionListener() {
        return switchRoom;
    }

    public ChatBox(){
        // Настройка текстового окна
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.setLayout(new BorderLayout());



        add(this.textArea, BorderLayout.CENTER);


        // Стиль для отображения жирного текста
        SimpleAttributeSet boldStyle = new SimpleAttributeSet();
        StyleConstants.setBold(boldStyle, true);

        appendNewMsg = (new ProtocolMessageListener() {
            @Override
            public void newMessage(ProtocolMessage msg) {
                switch(msg.header()) {
                    case "messageSuccess":
                    case "message": {
                        String header = String.format("[%s]: ",msg.content().getString("senderName"));
                        String body = msg.content().getString("content")+"\n";
                        StyledDocument doc = getRoomTextBox(
                                msg.content().getInt("roomId")
                        ).getStyledDocument();
                        try {
                            doc.insertString(doc.getLength(), header, boldStyle);
                            doc.insertString(doc.getLength(), body, null);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                        break;
                    }

                    case "listMessagesSuccess": {
                        StyledDocument doc = getRoomTextBox(
                                msg.content().getInt("roomId")
                        ).getStyledDocument();

                        JSONArray messagesJson = msg.content().getJSONArray("messages");
                        for (Object messageObject : messagesJson) {
                            JSONObject messageJson = (JSONObject)messageObject;
                            String header = String.format("[%s]: ",messageJson.getString("senderName"));
                            String body = messageJson.getString("content")+"\n";
                            try {
                                doc.insertString(doc.getLength(), header, boldStyle);
                                doc.insertString(doc.getLength(), body, null);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                }
            }
        });

        switchRoom = new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {

                RoomNode node = (RoomNode)e.getPath().getLastPathComponent();
                CardLayout cl = (CardLayout)(textArea.getLayout());
                currentRoomId = node.getRoomId();
                cl.show(textArea, String.valueOf(node.getRoomId()));
            }
        };
    }

    /**
     * Добавляет новый чат
     * @param roomId
     * @param roomName
     */
    public void addRoom(int roomId, String roomName){
        this.roomTreeMap.put(roomName, roomId);

        JTextPane roomTextBox = new JTextPane();
        roomTextBox.setEditable(false);
        roomTextBox.setMargin(new Insets(5,5,5,5));
        roomTextBox.setPreferredSize(new Dimension(700,500));
        JScrollPane roomTextScroll = new JScrollPane (roomTextBox);
        roomTextScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        roomTextScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.textArea.add(roomTextScroll, String.valueOf(roomId));
        this.roomContentMap.put(roomId, roomTextScroll);
    }

    /**
     * Возвращает окно активного чата
     * @param roomName
     * @return
     */
    public JTextPane getRoomTextBox(String roomName) {
        return getRoomTextBox(roomTreeMap.get(roomName));
    }

    /**
     * Возвращает окно активного чата
     * @param roomId
     * @return
     */
    public JTextPane getRoomTextBox(int roomId) {
        return (JTextPane) roomContentMap.get(roomId).getViewport().getView();
    }
}
