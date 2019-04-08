package Util;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ExceptionMessengerController
{
    @FXML
    private Label txtErrorMsg;

    @FXML
    private void btnAcceptError(ActionEvent event)
    {
        ((Stage) (((Button) event.getSource()).getScene().getWindow())).close();
    }

    void setErrorMsg(String errorMSg)

    {
        txtErrorMsg.setText(errorMSg);
    }
}
