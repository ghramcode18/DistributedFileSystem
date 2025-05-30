package client;

import shared.CoordinatorInterface;
import shared.FileRecord;

import java.rmi.Naming;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientApp {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            CoordinatorInterface coordinator = (CoordinatorInterface) Naming.lookup("rmi://localhost/Coordinator");

            System.out.println("=== Welcome to the Distributed File System ===");
            System.out.print("Username: ");
            String username = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            if (!coordinator.login(username, password)) {
                System.out.println("Invalid login!");
                return;
            }

            System.out.println("Login successful. Welcome, " + username + "!");
            while (true) {
                System.out.println("\nOptions:");
                System.out.println("1. Upload File");
                System.out.println("2. Download File");
                System.out.println("3. Delete File");
                System.out.println("4. List All Files");
                System.out.println("5. Exit");
                System.out.print("Choice: ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        System.out.print("File path to upload: ");
                        String filePath = scanner.nextLine();
                        Path path = Path.of(filePath);
                        if (!Files.exists(path)) {
                            System.out.println("File not found.");
                            break;
                        }

                        System.out.print("Department: ");
                        String dept = scanner.nextLine();

                        byte[] content = Files.readAllBytes(path);
                        String fileName = path.getFileName().toString();

                        FileRecord file = new FileRecord(fileName, dept, content);
                        boolean uploaded = coordinator.uploadFile(file, username);
                        System.out.println(uploaded ? "Upload successful." : "Upload failed.");
                        break;

                    case "2":
                        System.out.print("File name to download: ");
                        String downloadName = scanner.nextLine();
                        FileRecord downloaded = coordinator.downloadFile(downloadName, username);
                        if (downloaded != null) {
                            Files.write(Path.of("client_" + downloadName), downloaded.getContent());
                            System.out.println("Downloaded to: client_" + downloadName);
                        } else {
                            System.out.println("File not found.");
                        }
                        break;

                    case "3":
                        System.out.print("File name to delete: ");
                        String deleteName = scanner.nextLine();
                        boolean deleted = coordinator.deleteFile(deleteName, username);
                        System.out.println(deleted ? "Deleted successfully." : "Delete failed.");
                        break;

                    case "4":
                        var files = coordinator.listFiles(username);
                        System.out.println("Available files:");
                        for (String name : files) {
                            System.out.println("- " + name);
                        }
                        break;

                    case "5":
                        System.out.println("Goodbye!");
                        return;

                    default:
                        System.out.println("Invalid choice.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
