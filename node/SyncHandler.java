package node;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SyncHandler extends Thread {

    private final Socket socket;
    private final String storagePath;

    public SyncHandler(Socket socket, String storagePath) {
        this.socket = socket;
        this.storagePath = storagePath;
    }

    public void run() {
        try (
                DataInputStream in = new DataInputStream(socket.getInputStream())
        ) {
            String fileName = in.readUTF();
            int length = in.readInt();
            byte[] content = new byte[length];
            in.readFully(content);

            Files.write(Paths.get(storagePath + "/" + fileName), content);
            System.out.println("File synced: " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
