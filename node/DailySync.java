//package node;
//
//import java.io.File;
//
//public class DailySync {
//    public static void main(String[] args) {
//        File folder = new File("storageA");
//        File[] files = folder.listFiles();
//
//        for (File file : files) {
//            SyncClient.sendFile("localhost", 2001, file);
//            SyncClient.sendFile("localhost", 2002, file);
//        }
//    }
//}
