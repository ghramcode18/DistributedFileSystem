package coordinator;

import shared.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class CoordinatorImpl extends UnicastRemoteObject implements CoordinatorInterface {

    private Map<String, User> users;
    private Map<String, NodeInterface> nodes;
    private Map<String, List<String>> departmentFiles;
    private Iterator<String> loadBalancer;

    private Set<String> deletedFiles = new HashSet<>();


    public CoordinatorImpl() throws RemoteException {
        super();
        users = new HashMap<>();
        nodes = new HashMap<>();
        departmentFiles = new HashMap<>();
        loadBalancer = null;


        users.put("ali", new User("ali", "1234", "dev", false));
        users.put("sara", new User("sara", "1234", "qa", true));
        users.put("nada", new User("nada", "1234", "design", false));
    }

    public void registerNode(String nodeName, NodeInterface node) {
        nodes.put(nodeName, node);
        updateLoadBalancer();
        System.out.println("Node registered: " + nodeName);
    }

    private void updateLoadBalancer() {
        loadBalancer = nodes.keySet().iterator();
    }

    private NodeInterface getNextNode() {
        if (nodes.isEmpty()) {
            System.out.println("No storage nodes available.");
            return null;
        }

        List<String> keys = new ArrayList<>(nodes.keySet());
        for (String nodeName : keys) {
            try {
                NodeInterface node = nodes.get(nodeName);
                node.getNodeName();
                return node;
            } catch (Exception e) {
                System.out.println("⚠️ Node " + nodeName + " is unreachable. Skipping.");
                nodes.remove(nodeName);
            }
        }

        System.out.println("❌ No reachable nodes.");
        return null;
    }



    @Override
    public boolean login(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword().equals(password);
    }

    @Override
    public boolean uploadFile(FileRecord file, String username) throws RemoteException {
        User user = users.get(username);
        if (user == null || !user.getDepartment().equals(file.getDepartment())) {
            System.out.println("Unauthorized upload attempt.");
            return false;
        }

        NodeInterface node = getNextNode();
        if (node == null) {
            System.out.println("Upload failed: no available nodes.");
            return false;
        }

        if (node.storeFile(file)) {
            departmentFiles.computeIfAbsent(file.getDepartment(), k -> new ArrayList<>()).add(file.getFileName());
            System.out.println("In node "+node.getNodeName() +" File uploaded: " + file.getFileName());
            return true;
        }
        return false;
    }

    @Override
    public FileRecord downloadFile(String fileName, String requester) throws RemoteException {
        for (String nodeName : new ArrayList<>(nodes.keySet())) {
            try {
                NodeInterface node = nodes.get(nodeName);
                FileRecord file = node.retrieveFile(fileName);
                if (file != null) return file;
            } catch (Exception e) {
                System.out.println("⚠️ Failed to reach " + nodeName + " during download.");
                nodes.remove(nodeName);
            }
        }
        return null;
    }


    @Override
    public boolean deleteFile(String fileName, String username) throws RemoteException {
        User user = users.get(username);
        if (user == null) return false;

        for (String nodeName : new ArrayList<>(nodes.keySet())) {
            try {
                NodeInterface node = nodes.get(nodeName);
                FileRecord file = node.retrieveFile(fileName);
                if (file != null && file.getDepartment().equals(user.getDepartment())) {
                    deletedFiles.add(fileName); // 🟢 سجل الحذف هنا
                    return node.removeFile(fileName);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Node " + nodeName + " unreachable during delete.");
                nodes.remove(nodeName);
            }
        }
        return false;
    }



    @Override
    public List<String> listFiles(String requester) throws RemoteException {
        List<String> all = new ArrayList<>();
        for (NodeInterface node : nodes.values()) {
            try {
                all.addAll(node.getFileNames());
            } catch (Exception ignored) {}
        }
        return all;
    }
}
