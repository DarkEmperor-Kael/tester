package poiupv.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import poiupv.utils.DatabaseConnector;

import javax.imageio.ImageIO;
import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;

public class ProfileController {

    @FXML private Label user; // Nombre de usuario
    @FXML private Label currentPasswordLabel;
    @FXML private Label currentEmailLabel;
    @FXML private Label currentBirthdateLabel;
    private File nuevaImagen; // Imagen seleccionada, solo se guarda si se actualiza el perfil
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthdatePicker;
    @FXML private Circle avatarCircle;

    @FXML private Label passwordError;
    @FXML private Label emailError;
    @FXML private Label birthdateError;
    @FXML private Label generalError;

    private Image selectedAvatarImage;
    private static String nombreUsuario;
    public static void setNombreUsuarioEstatico(String usuario) {
        nombreUsuario = usuario;
    }
    
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
        loadUserData(); // Cargar datos al establecer nombre
    }

    @FXML
    private void initialize() {
        passwordError.setVisible(false);
        emailError.setVisible(false);
        birthdateError.setVisible(false);
        generalError.setVisible(false);
        if(nombreUsuario != null){
            loadUserData();
        }
    }

    private void loadUserData() {
        try (Connection conn = DatabaseConnector.connect()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user WHERE nickName = ?");
            stmt.setString(1, nombreUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Labels actuales
                String password = rs.getString("password");
                String email = rs.getString("email");
                String birthDate = rs.getString("birthDate");

                user.setText("Nombre de usuario: " + nombreUsuario);
                currentPasswordLabel.setText(password);
                currentEmailLabel.setText(email);
                currentBirthdateLabel.setText(birthDate);

                // Rellenar campos editables
                passwordField.setText(password);
                emailField.setText(email);
                birthdatePicker.setValue(LocalDate.parse(birthDate));

                // Avatar
                InputStream avatarStream = rs.getBinaryStream("avatar");
                if (avatarStream != null) {
                    Image avatar = new Image(avatarStream);
                    selectedAvatarImage = avatar;
                    avatarCircle.setFill(new ImagePattern(avatar));
                } else {
                    selectedAvatarImage = new Image(getClass().getResourceAsStream("/poiupv/utils/1144760.png"));
                    avatarCircle.setFill(new ImagePattern(selectedAvatarImage));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
private void onAvatarSelect() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Seleccionar avatar");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
    );

    File selectedFile = fileChooser.showOpenDialog(avatarCircle.getScene().getWindow());
    if (selectedFile != null) {
        nuevaImagen = selectedFile; // Se guarda pero aún no se actualiza en la BD

        Image preview = new Image(selectedFile.toURI().toString());
        avatarCircle.setFill(new ImagePattern(preview)); // Mostrar visualmente
    }
}

    @FXML
private void onUpdateProfile(ActionEvent event) {
    passwordError.setVisible(false);
    emailError.setVisible(false);
    birthdateError.setVisible(false);
    generalError.setVisible(false);

    String password = passwordField.getText();
    String email = emailField.getText();
    LocalDate birthDate = birthdatePicker.getValue();

    boolean valid = true;

    if (!isValidPassword(password)) {
        passwordError.setVisible(true);
        valid = false;
    }

    if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
        emailError.setVisible(true);
        valid = false;
    }

    if (birthDate == null || Period.between(birthDate, LocalDate.now()).getYears() < 16) {
        birthdateError.setVisible(true);
        valid = false;
    }

    if (!valid) return;

    try (Connection conn = DatabaseConnector.connect()) {
    PreparedStatement stmt;

    if (nuevaImagen != null) {
        String updateQuery = "UPDATE user SET password = ?, email = ?, birthDate = ?, avatar = ? WHERE nickName = ?";
        stmt = conn.prepareStatement(updateQuery);
        stmt.setString(1, password);
        stmt.setString(2, email);
        stmt.setString(3, birthDate.toString());
        FileInputStream fis = new FileInputStream(nuevaImagen);
        stmt.setBinaryStream(4, fis, (int) nuevaImagen.length());
        stmt.setString(5, nombreUsuario);
    } else {
        String updateQuery = "UPDATE user SET password = ?, email = ?, birthDate = ? WHERE nickName = ?";
        stmt = conn.prepareStatement(updateQuery);
        stmt.setString(1, password);
        stmt.setString(2, email);
        stmt.setString(3, birthDate.toString());
        stmt.setString(4, nombreUsuario);
    }

    stmt.executeUpdate();

    // Recargar vista del perfil
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/poiupv/view/Profile.fxml"));
    AnchorPane root = loader.load();
    ProfileController controller = loader.getController();
    controller.setNombreUsuario(nombreUsuario);
    Stage stage = (Stage) passwordField.getScene().getWindow();
    stage.setScene(new Scene(root));

} catch (Exception e) {
    e.printStackTrace();
    generalError.setText("Error al actualizar perfil.");
    generalError.setVisible(true);
}

}


    @FXML
    private void onBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/poiupv/view/MainMenu.fxml"));
            AnchorPane root = loader.load();
            MainMenuController controller = loader.getController();
            controller.setNombreUsuario(nombreUsuario);
            controller.setAvatarImage(selectedAvatarImage);
            Stage stage = (Stage) passwordField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] getAvatarImageBytes() {
        try {
            if (selectedAvatarImage == null) return null;
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(SwingFXUtils.fromFXImage(selectedAvatarImage, null), "png", os);
            return os.toByteArray();
        } catch (IOException e) {
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

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
