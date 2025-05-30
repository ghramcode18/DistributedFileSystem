package node;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class SyncClient {

    public static boolean trySendFile(String host, int port, File file) {
        try (
                Socket socket = new Socket(host, port);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream())
        ) {
            byte[] content = Files.readAllBytes(file.toPath());

            out.writeUTF(file.getName());
            out.writeInt(content.length);
            out.write(content);

            System.out.println("📤 Sent " + file.getName() + " to " + host + ":" + port);
            return true;
        } catch (IOException e) {
            // ما نطبع الخطأ، نرجع false فقط
            return false;
        }
    }
}
