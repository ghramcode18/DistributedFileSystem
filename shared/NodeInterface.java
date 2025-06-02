
package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface NodeInterface extends Remote {
    boolean storeFile(FileRecord file) throws RemoteException;
    FileRecord retrieveFile(String fileName) throws RemoteException;
    boolean removeFile(String fileName) throws RemoteException;
    List<String> getFileNames() throws RemoteException;
    String getNodeName() throws RemoteException;
    void markAsDeleted(String fileName) throws RemoteException;

}
