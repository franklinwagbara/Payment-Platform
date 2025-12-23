package com.walletplatform.shared.dto;

import java.util.UUID;

public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private boolean active;

    public UserResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final UserResponse r = new UserResponse();
        public Builder id(UUID id) { r.id = id; return this; }
        public Builder email(String email) { r.email = email; return this; }
        public Builder firstName(String firstName) { r.firstName = firstName; return this; }
        public Builder lastName(String lastName) { r.lastName = lastName; return this; }
        public Builder fullName(String fullName) { r.fullName = fullName; return this; }
        public Builder role(String role) { r.role = role; return this; }
        public Builder active(boolean active) { r.active = active; return this; }
        public UserResponse build() { return r; }
    }
}
