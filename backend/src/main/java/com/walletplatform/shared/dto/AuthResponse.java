package com.walletplatform.shared.dto;

public class AuthResponse {
    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private String userId;
    private String role;

    public AuthResponse() {}

    public AuthResponse(String token, String email, String firstName, String lastName, String userId, String role) {
        this.token = token;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userId = userId;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token;
        private String email;
        private String firstName;
        private String lastName;
        private String userId;
        private String role;

        public Builder token(String token) { this.token = token; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder role(String role) { this.role = role; return this; }

        public AuthResponse build() {
            return new AuthResponse(token, email, firstName, lastName, userId, role);
        }
    }
}
