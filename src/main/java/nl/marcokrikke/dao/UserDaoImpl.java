package nl.marcokrikke.dao;

import nl.marcokrikke.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UserDaoImpl implements UserDao {
    protected NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("dataSource")
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void create(User user) {
        String query = "INSERT INTO user (username, github_username, name, email, avatar_url) VALUES (:username, " +
                ":githubusername, :name, :email, :avatarurl)";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", user.getUsername());
        parameters.put("githubusername", user.getGitHubUsername());
        parameters.put("name", user.getName());
        parameters.put("email", user.getEmail());
        parameters.put("avatarurl", user.getAvatarUrl());

        this.jdbcTemplate.update(query, parameters);
    }

    @Override
    public User find(String gitHubUsername) {
        String query = "SELECT * FROM user WHERE github_username = :githubusername";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("githubusername", gitHubUsername);

        try {
            return this.jdbcTemplate.queryForObject(query, parameters, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User.Builder builder = new User.Builder();

            builder.id(rs.getLong("id"));
            builder.username(rs.getString("username"));
            builder.gitHubUsername(rs.getString("github_username"));
            builder.name(rs.getString("name"));
            builder.email(rs.getString("email"));
            builder.avatarUrl(rs.getString("avatar_url"));

            return builder.build();
        }
    }
}
