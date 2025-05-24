package poiupv.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import poiupv.utils.DatabaseConnector;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RecPasswordController {

    @FXML private TextField name;
    @FXML private PasswordField contra, conf;
    @FXML private Label generalErr, NoCon, Mal, mal2, usuMal;
    @FXML private Button actu, Retroceso;

    @FXML
    private void initialize() {
        generalErr.setVisible(false);
        usuMal.setVisible(false);
        NoCon.setVisible(false);
        Mal.setVisible(false);
        mal2.setVisible(false);
    }

    @FXML
    private void handleActualizar(ActionEvent event) {
        String usuario = name.getText().trim();
        String nuevaContra = contra.getText();
        String confirmar = conf.getText();

        boolean camposVacios = usuario.isEmpty() || nuevaContra.isEmpty() || confirmar.isEmpty();
        boolean hayError = false;

        // Ocultar errores previos
        generalErr.setVisible(false);
        usuMal.setVisible(false);
        NoCon.setVisible(false);
        Mal.setVisible(false);
        mal2.setVisible(false);

        if (camposVacios) {
            generalErr.setVisible(true);
            return;
        }

        try (Connection conn = DatabaseConnector.connect()) {
            // Verificar si existe por nick o correo
            String checkQuery = "SELECT * FROM user WHERE nickName = ? OR email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, usuario);
            checkStmt.setString(2, usuario);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                usuMal.setVisible(true);
                hayError = true;
            }

            if (!esContrasenaValida(nuevaContra)) {
                Mal.setVisible(true);
                mal2.setVisible(true);
                hayError = true;
            }

            if (!nuevaContra.equals(confirmar)) {
                NoCon.setVisible(true);
                hayError = true;
            }

            if (!hayError) {
                // Actualizar la contraseña
                String updateQuery = "UPDATE user SET password = ? WHERE nickName = ? OR email = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, nuevaContra);
                updateStmt.setString(2, usuario);
                updateStmt.setString(3, usuario);
                updateStmt.executeUpdate();

                // Volver al login
                Parent root = FXMLLoader.load(getClass().getResource("/poiupv/view/Login.fxml"));
                Stage stage = (Stage) actu.getScene().getWindow();
                stage.setScene(new Scene(root));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean esContrasenaValida(String password) {
        return password.length() >= 8 && password.length() <= 20 &&
               password.matches(".*[A-Z].*") &&
               password.matches(".*[a-z].*") &&
               password.matches(".*[0-9].*") &&
               password.matches(".*[@#$%&/()=^].*");
    }

    @FXML
    private void handleRetroceso(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/poiupv/view/Login.fxml"));
            Stage stage = (Stage) Retroceso.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



