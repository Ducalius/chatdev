package chat.client;

import chat.util.ProtocolMessage;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Компонент, управляющий списком доступных пользователю чатов
 */
public class RoomTreeBox extends JPanel {
    private final JTree roomTree;

    private final HashMap<String, Integer> roomTreeMap = new LinkedHashMap<>();

    private final ProtocolMessageListener newMsg;

    public ProtocolMessageListener getProtocolMessageListener() {
        return newMsg;
    }

    public RoomTreeBox() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Rooms");
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.setLayout(new BorderLayout());

        this.roomTree = new JTree(new DefaultTreeModel(root));
        this.roomTree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.roomTree.setMinimumSize(new Dimension(300,500));
        JScrollPane treeScroll = new JScrollPane(this.roomTree);
        treeScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        treeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(treeScroll, BorderLayout.CENTER);

        newMsg = (new ProtocolMessageListener() {
            @Override
            public void newMessage(ProtocolMessage msg) {
                if(msg.header() == "message") {
                    RoomNode node = (RoomNode) roomTree.getLastSelectedPathComponent();
                    if (node.getRoomId() != msg.content().getInt("roomId"))
                        node.setUnread(true);
                }
            }
        });

        this.roomTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                RoomNode node = (RoomNode) roomTree.getLastSelectedPathComponent();
                node.setUnread(false);
            }
        });
    }

    /**
     * Добавляет новый чат в список
     * @param roomId
     * @param roomName
     */
    public void addRoom(int roomId, String roomName){
        this.roomTreeMap.put(roomName, roomId);

        DefaultTreeModel model = (DefaultTreeModel) this.roomTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        model.insertNodeInto(new RoomNode(roomName, roomId), root, root.getChildCount());
    }

    /**
     * Регистрирует наблюдателя для сообщений типа {@link TreeSelectionEvent}. Данное событие возникает,
     * когда пользователь выбирает другой чат в списке
     */
    public void addSelectionListener(TreeSelectionListener l) {
        this.roomTree.addTreeSelectionListener(l);
    }

    /**
     * Делает первый в списке чат активным
     */
    public void selectFirstNode(){
        DefaultMutableTreeNode firstLeaf = ((DefaultMutableTreeNode)this.roomTree.getModel().getRoot()).getFirstLeaf();
        this.roomTree.setSelectionPath(new TreePath(firstLeaf.getPath()));
    }
}
