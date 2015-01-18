package nl.marcokrikke.service;

import nl.marcokrikke.model.User;

public interface UserService {
    public User create(User user);

    public User find(String gitHubUsername);
}
