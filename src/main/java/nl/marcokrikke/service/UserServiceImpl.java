package nl.marcokrikke.service;

import nl.marcokrikke.dao.UserDao;
import nl.marcokrikke.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    @Transactional
    public User create(User user) {
        userDao.create(user);

        return find(user.getGitHubUsername());
    }

    @Override
    public User find(String gitHubUsername) {
        return userDao.find(gitHubUsername);
    }
}
