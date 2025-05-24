package poiupv.controller;
//This is where the fun begins
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage; 

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import poiupv.utils.DatabaseConnector;

public class LoginController {

    @FXML
    private TextField user;

    @FXML
    private PasswordField contra;

    @FXML
    private Label errorUser;

    @FXML
    private Label errorPassword;

    @FXML
    private Label erLleno;

    @FXML
    private Button ini;

    @FXML
    private Button recu;

    @FXML
    private Button registro;

    @FXML
    public void initialize() {
        // Oculta todos los mensajes de error al iniciar
        errorUser.setVisible(false);
        errorPassword.setVisible(false);
        erLleno.setVisible(false);
    }

    @FXML
    private void handleLogin(ActionEvent Event){
    
    if (user.getText().isEmpty() || contra.getText().isEmpty()) {
    erLleno.setVisible(true);
    return;
    } 
    else {
    erLleno.setVisible(false);

    String nick = user.getText();
    String password = contra.getText();


     try (Connection conn = DatabaseConnector.connect()) {
            String query = "SELECT * FROM user WHERE nickName = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, nick);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Login correcto
                errorUser.setVisible(false);
                errorPassword.setVisible(false);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/poiupv/view/MainMenu.fxml"));
Parent root = loader.load();

// Obtener el controlador y pasarle el nombre de usuario
MainMenuController controller = loader.getController();
controller.setNombreUsuario(nick);

Stage stage = (Stage) ini.getScene().getWindow();
stage.setScene(new Scene(root));
stage.show();


            } else {
                // Mostrar errores adecuados
                errorUser.setVisible(true);
                errorPassword.setVisible(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }

    @FXML
    private void handleRecuperar(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/poiupv/view/RecPassword.fxml"));
            Stage stage = (Stage) recu.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegistro(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/poiupv/view/Register.fxml"));
            Stage stage = (Stage) registro.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
