package chat.client;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Компонент, управляющий окном получения данных для подключения
 */
public class ConnectPopup {
    // Поля окна
    private final JTextField addressField = new JTextField();
    private final JTextField portField = new JTextField();
    private final JLabel errorLabel = new JLabel();

    private final Object[] message = {
            "Address:", addressField,
            "Port:", portField,
            errorLabel
    };

    private InetAddress address = null;
    private int port;

    /**
     *
     * @param defaultPort Устанавливает в поле порта значение по умолчанию
     */
    public ConnectPopup(int defaultPort) {
        portField.setText(Integer.toString(defaultPort));
        this.port = defaultPort;
    }

    /**
     * Возвращает адрес, указанный пользователем
     * @return адрес, указанный пользователем
     */
    public InetAddress getAddress(){
        return this.address;
    }

    /**
     * Возвращает порт, указанный пользователем
     * @return порт, указанный пользователем
     */
    public int getPort(){
        return this.port;
    }

    /**
     * Отображает окно
     * @param error сообщение об ошибке, которое нужно отобразить. null если сообщение отображать не нужно
     * @return результат взаимодействия с пользователем. Подробнее в {@link javax.swing.JOptionPane}
     */
    private int show(String error) {
        errorLabel.setText(error);
        return JOptionPane.showConfirmDialog(
                null,
                this.message,
                "Connect",
                JOptionPane.OK_CANCEL_OPTION
        );
    }

    /**
     * Показывает окно до тех пор, пока пользователь не введет все поля корректно, или не закроет окно
     * @return результат взаимодействия с пользователем. Подробнее в {@link javax.swing.JOptionPane}
     */
    public int run() {
        int option;
        String errorMsg = "";
        while (true) {
            option = show(errorMsg);

            // Если пользователь нажал что-то кроме OK,
            // то он не хочет дальше взаимодействовать с окном.
            // Завершаем выполнение
            if (option != JOptionPane.OK_OPTION)
                break;

            // Валидация адреса
            try {
                this.address = InetAddress.getByName(addressField.getText());
            } catch (UnknownHostException e) {
                errorMsg = "Invalid address";
                continue;
            }

            // Валидация порта
            try {
                this.port = Integer.parseInt(portField.getText());
            } catch (NumberFormatException e) {
                errorMsg = "Invalid port number";
                continue;
            }

            // Если выполнение дошло до сюда,
            // значит пользователь ввел все значения корректно,
            // и выполнение можно завершить
            break;

        }
        return option;
    }
}
