
package shared;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String password;
    private String department;
    private boolean isManager;
    private String token;

// getter & setter
public String getToken() {
    return token;
}

public void setToken(String token) {
    this.token = token;
}

    public User(String username, String password, String department, boolean isManager) {
        this.username = username;
        this.password = password;
        this.department = department;
        this.isManager = isManager;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDepartment() {
        return department;
    }

    public boolean isManager() {
        return isManager;
    }
}
