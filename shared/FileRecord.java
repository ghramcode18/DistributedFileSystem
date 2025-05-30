
package shared;

import java.io.Serializable;

public class FileRecord implements Serializable {
    private String fileName;
    private String department;
    private byte[] content;

    public FileRecord(String fileName, String department, byte[] content) {
        this.fileName = fileName;
        this.department = department;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDepartment() {
        return department;
    }

    public byte[] getContent() {
        return content;
    }
}
