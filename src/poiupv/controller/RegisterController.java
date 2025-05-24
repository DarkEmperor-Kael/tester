package poiupv.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.shape.Circle;
import javafx.scene.paint.ImagePattern;
import javafx.scene.Parent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import poiupv.utils.DatabaseConnector;


public class RegisterController {

    @FXML private TextField nicknameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthdatePicker;
    @FXML private Button Retroceso;

    @FXML private Label nicknameError;
    @FXML private Label passwordError;
    @FXML private Label emailError;
    @FXML private Label birthdateError;
    @FXML private Label generalError;

    @FXML private Circle avatarCircle;

    @FXML
    private void initialize() {
        // Ocultar errores al inicio
        nicknameError.setVisible(false);
        passwordError.setVisible(false);
        emailError.setVisible(false);
        birthdateError.setVisible(false);
        generalError.setVisible(false);
        setAvatarImage(new Image(getClass().getResourceAsStream("/poiupv/utils/1144760.png")));
    }

    private void setAvatarImage(Image image) {
    avatarCircle.setFill(new ImagePattern(image, 0, 0, 1, 1, true));
}
@FXML
private void onAvatarSelect(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Seleccionar imagen de avatar");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
    );
    File selectedFile = fileChooser.showOpenDialog(null);

    if (selectedFile != null) {
        Image avatar = new Image(selectedFile.toURI().toString());
        setAvatarImage(avatar);
    }
}
    @FXML
    private void onRegister(ActionEvent event) {
        boolean hasEmptyFields = false;

        // Limpiar errores previos
        nicknameError.setVisible(false);
        passwordError.setVisible(false);
        emailError.setVisible(false);
        birthdateError.setVisible(false);
        generalError.setVisible(false);

        String nickname = nicknameField.getText().trim();
        String password = passwordField.getText();
        String email = emailField.getText();
        LocalDate birthdate = birthdatePicker.getValue();

         if (nickname.isEmpty() || password.isEmpty() || email.isEmpty() || birthdate == null) {
        generalError.setVisible(true);
        return;
    }
         boolean isValid = true;
        
        // Validaciones
        if (nickname.length() < 5 || nickname.length() > 15) {
            nicknameError.setVisible(true);
            isValid = false;
        }

        if (!isValidPassword(password)) {
            passwordError.setVisible(true);
            isValid = false;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            emailError.setVisible(true);
            isValid = false;
        }

        if (Period.between(birthdate, LocalDate.now()).getYears() < 16) {
            birthdateError.setVisible(true);
            isValid = false;
        }

        if (!isValid) {
            return;
        }
           try (Connection conn = DatabaseConnector.connect()) {
        // Comprobar duplicados
        String checkQuery = "SELECT * FROM user WHERE nickName = ? OR email = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setString(1, nickname);
        checkStmt.setString(2, email);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            generalError.setText("El nickname o email ya está registrado.");
            generalError.setVisible(true);
            return;
        }

        // Insertar nuevo usuario
        String insertQuery = "INSERT INTO user (nickName, password, email, birthDate, avatar) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.setString(1, nickname);
        insertStmt.setString(2, password);
        insertStmt.setString(3, email);
        insertStmt.setString(4, birthdate.toString());

        // Avatar como BLOB (puede ser null si no se cargó)
        byte[] avatarBytes = getAvatarImageBytes();
        insertStmt.setBytes(5, avatarBytes);

        insertStmt.executeUpdate();

        // Ir al menú principal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/poiupv/view/MainMenu.fxml"));
        AnchorPane root = loader.load();
        MainMenuController controller = loader.getController();
        controller.setNombreUsuario(nickname);
        Stage stage = (Stage) nicknameField.getScene().getWindow();
        stage.setScene(new Scene(root));

    } catch (SQLException e) {
        e.printStackTrace();
        generalError.setText("Error de base de datos.");
        generalError.setVisible(true);
    } catch (IOException e) {
        e.printStackTrace();
    }
} 
        
    @FXML
private void handleRetroceso(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/poiupv/view/Login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) Retroceso.getScene().getWindow();
        stage.setScene(new Scene(root));
    } catch (IOException e) {
        e.printStackTrace();
    }
}
private byte[] getAvatarImageBytes() {
    try {
        // Obtener imagen desde el Circle como ruta
        Image img = ((ImagePattern) avatarCircle.getFill()).getImage();
        String url = img.getUrl();

        if (url != null && url.startsWith("file:")) {
            File file = new File(url.substring(5));
            try (InputStream is = new FileInputStream(file);
                 ByteArrayOutputStream os = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                return os.toByteArray();
            }
            
        }
        else {
                return null; // O cargar una imagen por defecto desde recursos
}
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}
private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 20)
            return false;

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%&*()\\-+=].*");

        return hasUpper && hasLower && hasSpecial && hasDigit;
    }

}


