package nl.marcokrikke.dao;

import nl.marcokrikke.model.User;

public interface UserDao {
    public void create(User user);

    public User find(String gitHubUsername);
}
