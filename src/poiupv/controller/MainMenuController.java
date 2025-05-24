package poiupv.controller;
//This is where the fun continues ;)
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.io.*;
import poiupv.utils.DatabaseConnector;

public class MainMenuController implements Initializable {

    @FXML
    private MenuButton profileMenu;

    @FXML
    private MenuItem menuCerrarSesion;

    @FXML
    private MenuItem menuVerPerfil;

    @FXML
    private Circle avatarCircle;

    @FXML
    private Label usuario;

    private String nombreUsuario;

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
        cargarDatosUsuario();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Por si acaso nos hiciera falta
    }

    private void cargarDatosUsuario() {
        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "SELECT * FROM user WHERE nickName = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombreUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                usuario.setText(rs.getString("nickName"));

                // Suponiendo que avatar es un BLOB
                byte[] avatarBytes = rs.getBytes("avatar");
                if (avatarBytes != null) {
                    InputStream avatarStream = new ByteArrayInputStream(avatarBytes);
                    Image avatarImage = new Image(avatarStream);
                    avatarCircle.setFill(new ImagePattern(avatarImage));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
private void onLogout(ActionEvent event) {
    try {
        // Usa cualquier nodo visible en pantalla (por ejemplo, el label del nombre)
        Stage stage = (Stage) usuario.getScene().getWindow(); // Usa aquí un nodo real
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/poiupv/view/Login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    @FXML
    private void onViewProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/poiupv/Perfil.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(loader.load());

            // Opcional: pasar el usuario a la vista Perfil
            // PerfilController controller = loader.getController();
            // controller.setNombreUsuario(nombreUsuario);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

