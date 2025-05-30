package node;

import shared.FileRecord;
import shared.NodeInterface;

import java.io.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
            // حفظ الملف الفعلي
            FileOutputStream fos = new FileOutputStream(storagePath + "/" + file.getFileName());
            fos.write(file.getContent());
            fos.close();

            // حفظ ملف metadata فيه اسم القسم
            FileWriter metaWriter = new FileWriter(storagePath + "/" + file.getFileName() + ".meta");
            metaWriter.write(file.getDepartment());
            metaWriter.close();

            System.out.println("[" + nodeName + "] Stored file: " + file.getFileName());
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
            File file = new File(storagePath + "/" + fileName);
            if (!file.exists()) return null;

            FileInputStream fis = new FileInputStream(file);
            byte[] content = fis.readAllBytes();
            fis.close();

            // قراءة القسم من ملف metadata
            String department = "unknown";
            File meta = new File(storagePath + "/" + fileName + ".meta");
            if (meta.exists()) {
                department = new String(Files.readAllBytes(meta.toPath())).trim();
            }

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
}
