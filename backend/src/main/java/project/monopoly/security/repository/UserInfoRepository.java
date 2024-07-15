package project.monopoly.security.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import project.monopoly.security.entity.UserInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class UserInfoRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String INSERT_USER_SQL = "INSERT INTO userinfo (name, email, password, roles, dob, gid) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_USER_BY_NAME_SQL = "SELECT * FROM userinfo WHERE name = ?";
    private static final String SELECT_USER_BY_EMAIL_SQL = "SELECT * FROM userinfo WHERE email = ?";
    private static final String UPDATE_USER_GAME_STATUS_BY_NAME = "UPDATE userinfo SET inGame = ?, gid = ? WHERE name = ?";

    public void save(UserInfo userInfo) {
        jdbcTemplate.update(INSERT_USER_SQL, userInfo.getName(), userInfo.getEmail(), userInfo.getPassword(), userInfo.getRoles(), userInfo.getDob(), "");
    }

    public Optional<UserInfo> findByName(String username) {
        PreparedStatementSetter setter = ps -> ps.setString(1, username);
        ResultSetExtractor<Optional<UserInfo>> extractor = rs -> {
            if (rs.next()) {
                return Optional.of(mapUserInfo(rs));
            } 
            else {
                return Optional.empty();
            }
        };

        return jdbcTemplate.query(SELECT_USER_BY_NAME_SQL, setter, extractor);
    }

    public Optional<UserInfo> findByEmail(String email) {
        PreparedStatementSetter setter = ps -> ps.setString(1, email);
        ResultSetExtractor<Optional<UserInfo>> extractor = rs -> {
            if (rs.next()) {
                return Optional.of(mapUserInfo(rs));
            } 
            else {
                return Optional.empty();
            }
        };

        return jdbcTemplate.query(SELECT_USER_BY_EMAIL_SQL, setter, extractor);
    }

    public boolean updateUserGameStatusByName(String name, String gid, boolean inGame){
        return jdbcTemplate.update(UPDATE_USER_GAME_STATUS_BY_NAME, inGame, gid, name) == 1;
    }
    
    private UserInfo mapUserInfo(ResultSet rs) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRoles(rs.getString("roles"));
        user.setDob(rs.getDate("dob"));
        user.setGid(rs.getString("gid"));
        user.setInGame(rs.getBoolean("inGame"));
        return user;
    }
}
