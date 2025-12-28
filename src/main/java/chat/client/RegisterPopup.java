package chat.client;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * Компонент, управляющий окном получения данных для регистрации
 */
public class RegisterPopup {
    // Поля окна
    private final JTextField usernameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField passwordField = new JPasswordField();
    private final JLabel errorLabel = new JLabel();

    public enum UserAction {OK, CLOSE, LOGIN};

    private final Object[] message = {
            "Username:", usernameField,
            "Email:", emailField,
            "Password:", passwordField,
            errorLabel
    };

    private String username = null;
    private String password = null;
    private String email = null;



    /**
     * Возвращает юзернейм, указанный пользователем
     * @return юзернейм, указанный пользователем
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Возвращает почту, указанную пользователем
     * @return почта, указанная пользователем
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Возвращает пароль, указанный пользователем
     * @return юзернейм, указанный пользователем
     */
    public String getPassword() {
        return this.password;
    }

    public void clearPassword() {
        passwordField.setText("");
        this.password = null;
    }

    public void clearAll() {
        passwordField.setText("");
        this.password = null;
        usernameField.setText("");
        this.username = null;
        emailField.setText("");
        this.email = null;
    }

    /**
     * Отображает окно
     * @param error сообщение об ошибке, которое нужно отобразить. null если сообщение отображать не нужно
     * @return результат взаимодействия с пользователем. Подробнее в {@link JOptionPane}
     */
    private int show(String error) {
        Object[] options = { "Open Login Page", "Continue"};
        errorLabel.setText(error);
        return JOptionPane.showOptionDialog(
                null,
                this.message,
                "Register",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
    }

    /**
     * Показывает окно до тех пор, пока пользователь не введет все поля корректно, или не закроет окно
     *
     * @return результат взаимодействия с пользователем. Подробнее в {@link JOptionPane}
     */
    public UserAction run() {
        return run("");
    }

    /**
     * Показывает окно до тех пор, пока пользователь не введет все поля корректно, или не закроет окно
     * @param error сообщение об ошибке, которое нужно вывести.
     * @return результат взаимодействия с пользователем. Подробнее в {@link JOptionPane}
     */
    public UserAction run(String error) {
        int option;
        UserAction result;
        String errorMsg = error;
        while (true) {
            option = show(errorMsg);

            this.username = usernameField.getText();
            this.email = emailField.getText();
            this.password = passwordField.getText();
            passwordField.setText("");


            if (option == -1)
                return UserAction.CLOSE;

            if (option == 0)
                return UserAction.LOGIN;


            // Валидация юзернейма
            if(username.length() < 3) {
                errorMsg = "Username should be at least 3 characters long";
                continue;
            }

            // Валидация почты
            if(!EmailValidator.getInstance().isValid(email)) {
                errorMsg = "Invalid Email Address";
                continue;
            }
            // Валидация пароля
            if(password.length() < 8) {
                errorMsg = "Password should be at least 8 characters long";
                continue;
            }

            // Если выполнение дошло до сюда,
            // значит пользователь ввел все значения корректно,
            // и выполнение можно завершить
            break;

        }
        return UserAction.OK;
    }
}
