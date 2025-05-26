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
import model.User;
import poiupv.utils.DatabaseConnector;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import poiupv.controller.RegisterController;


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
    private Image avatarImage;
    private String nombreUsuario;

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
        cargarDatosUsuario();
    }

@Override
public void initialize(URL url, ResourceBundle resourceBundle) {
    //Por si acaso
}

private void cargarDatosUsuario() {
    try (Connection conn = DatabaseConnector.connect()) {
        String sql = "SELECT nickName, avatar FROM user WHERE nickName = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, nombreUsuario);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            usuario.setText(rs.getString("nickName"));

            InputStream avatarStream = rs.getBinaryStream("avatar");

            if (avatarStream != null) {
                Image image = new Image(avatarStream);
                if (!image.isError()) {
                    setAvatarImage(image);
                } else {
                    System.err.println("Error al cargar la imagen (formato no soportado).");
                    setAvatarImage(null);
                }
            } else {
                setAvatarImage(null);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}


public void setAvatarImage(Image avatarImage) {
    this.avatarImage = avatarImage;
    if (avatarImage != null) {
        Image mirroredImage = flipImageHorizontally(avatarImage);
        avatarCircle.setFill(new ImagePattern(mirroredImage));
    } else {
        Image defaultImage = new Image(getClass().getResourceAsStream("/poiupv/utils/1144760.png"));
        avatarCircle.setFill(new ImagePattern(defaultImage));
    }
}



    @FXML
private void onLogout(ActionEvent event) {
    try {
        // Usa cualquier nodo visible en pantalla (por ejemplo, el label del nombre)
        Stage stage = (Stage) usuario.getScene().getWindow(); // Usa aquí un nodo real
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/poiupv/view/Login.fxml"));
        Parent root = loader.load();
        System.out.println("Cerrar sesión");
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
            ProfileController.setNombreUsuarioEstatico(nombreUsuario);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/poiupv/view/Profile.fxml"));
            Stage stage = (Stage) usuario.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Image flipImageHorizontally(Image originalImage) {
    int width = (int) originalImage.getWidth();
    int height = (int) originalImage.getHeight();

    WritableImage flippedImage = new WritableImage(width, height);
    PixelReader reader = originalImage.getPixelReader();
    PixelWriter writer = flippedImage.getPixelWriter();

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            writer.setArgb(width - x - 1, y, reader.getArgb(x, y));
        }
    }

    return flippedImage;
}

}
