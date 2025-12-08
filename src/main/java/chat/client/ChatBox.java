package chat.client;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * Компонент, отображающий сообщения
 */
public class ChatBox extends JPanel {

    private final JTextPane textBox;

    private final MessageListener appendNewMsg;

    /**
     * Возвращает <code>MessageListener</code>, печатающий сообщение в окно
     * @return обработчик событий
     */
    public MessageListener getMessageListener() {
        return appendNewMsg;
    }

    public ChatBox(){
        // Настройка текстового окна
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.setLayout(new BorderLayout());
        this.textBox = new JTextPane();
        this.textBox.setEditable(false);
        this.textBox.setMargin(new Insets(5,5,5,5));
        this.textBox.setPreferredSize(new Dimension(700,500));
        JScrollPane scroll = new JScrollPane (this.textBox);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll);

        // Стиль для отображения жирного текста
        SimpleAttributeSet boldStyle = new SimpleAttributeSet();
        StyleConstants.setBold(boldStyle, true);

        // Обработчик событий MessageEvent
        appendNewMsg = (new MessageListener() {
            @Override
            public void newMessage(MessageEvent evt) {
                String header = String.format("[%s]: ",evt.getSender());
                String msg = evt.getContent()+"\n";
                StyledDocument doc = textBox.getStyledDocument();
                try {
                    doc.insertString(doc.getLength(), header, boldStyle);
                    doc.insertString(doc.getLength(), msg, null);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
