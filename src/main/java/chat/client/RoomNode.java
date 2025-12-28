package chat.client;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Класс, представляющий отдельный чат в окне {@link chat.client.RoomTreeBox}
 */
public class RoomNode extends DefaultMutableTreeNode {
    public String roomName;
    public int roomId;
    public boolean isUnreadVal;

    public RoomNode(String roomName, int roomId) {
        this.roomName = roomName;
        this.roomId = roomId;
        setUnread(false);
    }

    public int getRoomId() {
        return this.roomId;
    }

    public void setUnread(boolean val){
        isUnreadVal = val;
    }

    public boolean isUnread(){
        return isUnreadVal;
    }

    @Override
    public String toString() {

        return isUnread() ? "<html><b>"+roomName+"</html></b>" : roomName;
    }
}
