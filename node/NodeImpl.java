package node;

import shared.NodeInterface;
import shared.FileRecord;

import java.io.*;
import java.nio.file.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


public class NodeImpl extends UnicastRemoteObject implements NodeInterface {

    private final String nodeName;
    private final String storagePath;

    private final File deleteLogFile;

    private final Set<String> deletedFilesSet;

    private final ConcurrentHashMap<String, ReentrantLock> fileLocks = new ConcurrentHashMap<>();

    public NodeImpl(String nodeName, String storagePath) throws IOException {
        super();
        this.nodeName = nodeName;
        this.storagePath = storagePath;
        File dir = new File(storagePath);
        if (!dir.exists()) dir.mkdirs();
        deleteLogFile = new File(storagePath + "/deleted.txt");
        deletedFilesSet = new HashSet<>();

        if (deleteLogFile.exists()) {
            List<String> lines = Files.readAllLines(deleteLogFile.toPath());
            deletedFilesSet.addAll(lines);
        }

    }
@Override
public boolean storeFile(FileRecord file) {
    ReentrantLock lock = fileLocks.computeIfAbsent(file.getFileName(), k -> new ReentrantLock());
    lock.lock();
    try {
        // (أ) حفظ الملف الفعلي
        Path filePath = Paths.get(storagePath, file.getFileName());
        Files.write(filePath, file.getContent());

        // (ب) حفظ ملف metadata باسم "<fileName>.meta" يحتوي قسم الملف
        Path metaPath = Paths.get(storagePath, file.getFileName() + ".meta");
        Files.writeString(metaPath, file.getDepartment(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("[" + nodeName + "] Stored file: " + file.getFileName() + " (dept: " + file.getDepartment() + ")");
        return true;
    } catch (IOException e) {
        System.err.println("[" + nodeName + "] Error storing file: " + e.getMessage());
        return false;
    } finally {
        lock.unlock();
    }
}



@Override
public FileRecord retrieveFile(String fileName) {
    ReentrantLock lock = fileLocks.computeIfAbsent(fileName, k -> new ReentrantLock());
    lock.lock();
    try {
        Path filePath = Paths.get(storagePath, fileName);
        if (!Files.exists(filePath)) return null;

        // (أ) قراءة المحتوى
        byte[] content = Files.readAllBytes(filePath);

        // (ب) قراءة القسم من ملف "<fileName>.meta"
        Path metaPath = Paths.get(storagePath, fileName + ".meta");
        String department = Files.exists(metaPath)
            ? Files.readString(metaPath).trim()
            : "unknown";

        return new FileRecord(fileName, department, content);
    } catch (IOException e) {
        System.err.println("[" + nodeName + "] Error retrieving file: " + e.getMessage());
        return null;
    } finally {
        lock.unlock();
    }
}




    @Override
    public boolean removeFile(String fileName) {
        ReentrantLock lock = fileLocks.computeIfAbsent(fileName, k -> new ReentrantLock());
        lock.lock();

        try {
            File file = new File(storagePath + "/" + fileName);
            File meta = new File(storagePath + "/" + fileName + ".meta");

            boolean deletedFile = file.exists() && file.delete();
            boolean deletedMeta = meta.exists() && meta.delete();

            if (deletedFile) {
                System.out.println("[" + nodeName + "] Deleted file: " + fileName);
                Files.write(deleteLogFile.toPath(), (fileName + "\n").getBytes(), StandardOpenOption.APPEND);
                deletedFilesSet.add(fileName);

            }

            return deletedFile || deletedMeta;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }



    @Override
    public List<String> getFileNames() {
        File folder = new File(storagePath);
        String[] names = folder.list();
        return names == null ? new ArrayList<>() : Arrays.asList(names);
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

    @Override
    public void markAsDeleted(String fileName) throws RemoteException {
    try {
        File deletedList = new File(storagePath + "/deleted.txt");
        Files.write(
            deletedList.toPath(),
            (fileName + "\n").getBytes(),
            Files.exists(deletedList.toPath())
                ? java.nio.file.StandardOpenOption.APPEND
                : java.nio.file.StandardOpenOption.CREATE
        );
        System.out.println("[" + nodeName + "] Marked as deleted: " + fileName);
    } catch (IOException e) {
        System.err.println("[" + nodeName + "] Failed to mark deletion: " + e.getMessage());
    }
}


}
