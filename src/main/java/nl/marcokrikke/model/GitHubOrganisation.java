package nl.marcokrikke.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubOrganisation {
    private long id;
    private String login;
    private String url;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String description;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GitHubOrganisation{");
        sb.append("id=").append(id);
        sb.append(", login='").append(login).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", avatarUrl='").append(avatarUrl).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
