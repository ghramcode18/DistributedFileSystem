package node;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import shared.FileRecord;

public class SyncClient {

public static boolean trySendFile(String host, int port, FileRecord record) {
    try (
        Socket socket = new Socket(host, port);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
    ) {
        out.writeObject(record);
        return true;
    } catch (IOException e) {
        return false;
    }
}

}
