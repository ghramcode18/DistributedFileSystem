
package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface CoordinatorInterface extends Remote {
    boolean login(String username, String password) throws RemoteException;
    boolean uploadFile(FileRecord file, String username) throws RemoteException;
    FileRecord downloadFile(String fileName, String requester) throws RemoteException;
    boolean deleteFile(String fileName, String username) throws RemoteException;
    List<String> listFiles(String requester) throws RemoteException;
    public void registerNode(String nodeName, NodeInterface node)throws RemoteException;
    User getUserByToken(String token) throws RemoteException;

}
/*

javac coordinator/*.java node/*.java client/*.java shared/*.java
java coordinator.CoordinatorServer
 java node.NodeServer
java node.NodeServer NodeA storageA
java node.NodeServer NodeB storageB
java node.NodeServer NodeC storageC

java client.ClientApp
/home/ghram/Downloads/test.txt

            case "NodeA" -> 2000;
            case "NodeB" -> 2001;
            case "NodeC" -> 2002;
            default -> 2003;
 */