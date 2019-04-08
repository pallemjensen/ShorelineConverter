package Util;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ExceptionHandler
{
    /**
     * Method to show a exception message window
     *
     * @param ex
     */
    public static void exceptionHandler(Exception ex)
    {
        Platform.runLater(() ->
        {
            String errorMsg = ex.getMessage();
            FXMLLoader fxmlLoader1 = new FXMLLoader(ExceptionMessengerController.class.getResource("/View/ExceptionMessenger.fxml"));
            Parent root = null;
            try
            {
                root = fxmlLoader1.load();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            ExceptionMessengerController emc = fxmlLoader1.getController();
            emc.setErrorMsg(errorMsg);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        });
    }
}
