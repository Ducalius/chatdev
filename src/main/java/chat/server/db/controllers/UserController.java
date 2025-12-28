package chat.server.db.controllers;

import chat.server.db.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

/**
 * Класс-контроллер, позволяющий производить  базе данных действия, связанные с таблицей <code>users</code>
 */
public class UserController {

    private final Connection db;
    private final PreparedStatement registerStatement;
    private final PreparedStatement getUserByNameStatement;
    private final PreparedStatement getUserByIdStatement;

    public UserController(Connection db) throws SQLException{
        this.db = db;
        this.registerStatement = this.db.prepareStatement(
                "INSERT INTO users (username, passhash, email) VALUES (?,?,?)"
        );
        this.getUserByNameStatement = this.db.prepareStatement(
                "SELECT * FROM users WHERE username = ?"
        );
        this.getUserByIdStatement = this.db.prepareStatement(
                "SELECT * FROM users WHERE id = ?"
        );
    }

    /**
     * Регистрирует новую учетную запись
     * @param username
     * @param password
     * @param email
     * @throws SQLException
     */
    public void register(String username, String password, String email) throws SQLException {
        try {
            String passhash = BCrypt.hashpw(password, BCrypt.gensalt());
            this.registerStatement.setString(1, username);
            this.registerStatement.setString(2, passhash);
            this.registerStatement.setString(3, email);
            this.registerStatement.executeUpdate();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Осуществляет авторизацию пользователя
     * @param username
     * @param passwordCandidate
     * @return
     * @throws SQLException
     */
    public boolean login(String username, String passwordCandidate) throws SQLException {
        try {
            this.getUserByNameStatement.setString(1, username);
            ResultSet rst = this.getUserByNameStatement.executeQuery();
            rst.next();
            return BCrypt.checkpw(passwordCandidate, rst.getString("passhash"));
        } catch (Exception e) {
            throw e;
        }
    }

    public User getUserByName(String username) throws SQLException {
        try {
            this.getUserByNameStatement.setString(1, username);
            ResultSet rst = this.getUserByNameStatement.executeQuery();
            rst.next();
            User user = new User(
                    rst.getInt("id"),
                    rst.getString("username"),
                    rst.getString("privilege"),
                    rst.getString("email")
            );
            return user;
        } catch (Exception e) {
            throw e;
        }
    }
}
