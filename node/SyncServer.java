package node;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class SyncServer extends Thread {

    private final int port;
    private final String storagePath;

    public SyncServer(int port, String storagePath) {
        this.port = port;
        this.storagePath = storagePath;
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("SyncServer running on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                new SyncHandler(socket, storagePath).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
