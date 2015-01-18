package nl.marcokrikke.security;

public class GitHubEmail {
    private String email;
    private boolean primary;
    private boolean verified;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GitHubEmail{");
        sb.append("email='").append(email).append('\'');
        sb.append(", primary=").append(primary);
        sb.append(", verified=").append(verified);
        sb.append('}');
        return sb.toString();
    }
}
