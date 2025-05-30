package coordinator;

import shared.CoordinatorInterface;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class CoordinatorServer {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            CoordinatorImpl coordinator = new CoordinatorImpl();
            Naming.rebind("Coordinator", coordinator);
            System.out.println("Coordinator is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
