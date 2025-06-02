package node;

import shared.FileRecord; // 👈 ضروري

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption; // 👈 كمان ضروري

public class SyncHandler extends Thread {
    private final Socket socket;
    private final String storagePath;

    public SyncHandler(Socket socket, String storagePath) {
        this.socket = socket;
        this.storagePath = storagePath;
    }

    @Override
    public void run() {
        try (
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            FileRecord record = (FileRecord) in.readObject();

            // اكتب الملف الفعلي
            Path filePath = Paths.get(storagePath, record.getFileName());
            Files.write(filePath, record.getContent());

            // اكتب .meta بالقسم مباشرة
            Path metaPath = Paths.get(storagePath, record.getFileName() + ".meta");
            Files.writeString(metaPath, record.getDepartment(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("✅ Synced file: " + record.getFileName() + " (dept: " + record.getDepartment() + ")");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
