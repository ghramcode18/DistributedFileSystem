package node;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AutoSyncThread extends Thread {

    private final String storagePath;
    private final List<PeerNode> peers;
    private final Set<PeerNode> failedPeers = new HashSet<>();

    public AutoSyncThread(String storagePath, List<PeerNode> peers) {
        this.storagePath = storagePath;
        this.peers = peers;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 🟡 اقرأ ملفات الحذف من deleted.txt
                Set<String> deletedFiles = new HashSet<>();
                File deletedListFile = new File(storagePath + "/deleted.txt");
                if (deletedListFile.exists()) {
                    List<String> lines = Files.readAllLines(deletedListFile.toPath());
                    for (String line : lines) {
                        deletedFiles.add(line.trim());
                    }
                }

                File[] files = new File(storagePath).listFiles();
                if (files == null) continue;

                for (File file : files) {
                    // ⚠️ تجاهل ملفات meta و deleted.txt نفسها
                    if (file.getName().endsWith(".meta") || file.getName().equals("deleted.txt")) continue;

                    // ⚠️ تجاهل الملفات المحذوفة
                    if (deletedFiles.contains(file.getName())) continue;

                    for (PeerNode peer : peers) {
                        if (failedPeers.contains(peer)) continue;

                        boolean success = SyncClient.trySendFile(peer.host(), peer.port(), file);
                        if (!success) {
                            System.out.println("⚠️ Failed to sync with " + peer);
                            failedPeers.add(peer);
                        }
                    }
                }

                Thread.sleep(10000);
                failedPeers.clear();

                System.out.println("✅ Auto-sync cycle completed.\n");

                Thread.sleep(50000); // باقي الدقيقة
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public record PeerNode(String host, int port) {
        @Override
        public String toString() {
            return host + ":" + port;
        }
    }
}
