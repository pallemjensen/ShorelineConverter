package Controller;

import Model.Model;
import Util.WindowManager;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginWindowController implements Initializable
{
    private final Model model = Model.getInstance();

    @FXML
    private Pane pane;

    @FXML
    private ImageView closeBtn;

    @FXML
    private JFXTextField txtUsername;

    @FXML
    private JFXPasswordField txtPassword;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        WindowManager.addDraggability(pane);
    }

    @FXML
    public void login() throws IOException
    {
        model.login(txtUsername, txtPassword);
    }

    @FXML
    void switchCloseBlack(MouseEvent event)
    {
        closeBtn.setImage(new Image("Images/ic_close_black_16dp_1x.png"));
    }

    @FXML
    void switchCloseWhite(MouseEvent event)
    {
        closeBtn.setImage(new Image("Images/ic_close_white_16dp_1x.png"));
    }

    @FXML
    void closeWindow(MouseEvent event)
    {
        System.exit(0);
    }

    @FXML
    void minimizeWindow(MouseEvent event)
    {
        Stage stage = (Stage) ((Pane) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }
}
