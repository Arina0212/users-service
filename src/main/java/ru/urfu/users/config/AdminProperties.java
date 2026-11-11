package ru.urfu.users.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {

    private boolean enabled = true;
    private String email;
    private String password;
    private String firstname = "Администратор";
    private String lastname = "Системный";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname == null || firstname.isBlank() ? "Администратор" : firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname == null || lastname.isBlank() ? "Системный" : lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
