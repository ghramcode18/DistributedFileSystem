package node;

import shared.CoordinatorInterface;
import shared.NodeInterface;

import java.rmi.Naming;
import java.util.List;
import java.util.Random;

public class NodeServer {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java NodeServer <NodeName> <StoragePath>");
            return;
        }

        String nodeName = args[0];
        String storagePath = args[1];

        try {
            NodeInterface node = new NodeImpl(nodeName, storagePath);
            Naming.rebind(nodeName, node);

            CoordinatorInterface coordinator = (CoordinatorInterface) Naming.lookup("rmi://localhost/Coordinator");
            coordinator.getClass().getMethod("registerNode", String.class, shared.NodeInterface.class)
                    .invoke(coordinator, nodeName, node);

            System.out.println("Node " + nodeName + " is running and registered.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // داخل main قبل السطر الأخير:
        int syncPort = switch (nodeName) {
            case "NodeA" -> 2000;
            case "NodeB" -> 2001;
            case "NodeC" -> 2002;
            default -> 2003;
        };

        new SyncServer(syncPort, storagePath).start();

// حدد العقد الأخرى (العقد التي تريد المزامنة معها)
        List<AutoSyncThread.PeerNode> peers = switch (nodeName) {
            case "NodeA" -> List.of(
                    new AutoSyncThread.PeerNode("localhost", 2001),
                    new AutoSyncThread.PeerNode("localhost", 2002)
            );
            case "NodeB" -> List.of(
                    new AutoSyncThread.PeerNode("localhost", 2000),
                    new AutoSyncThread.PeerNode("localhost", 2002)
            );
            case "NodeC" -> List.of(
                    new AutoSyncThread.PeerNode("localhost", 2000),
                    new AutoSyncThread.PeerNode("localhost", 2001)
            );
            default -> List.of();
        };

        new AutoSyncThread(storagePath, peers).start();

    }
}
