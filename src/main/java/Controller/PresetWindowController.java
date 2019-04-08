package Controller;

import Model.Model;
import Util.WindowManager;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class PresetWindowController implements Initializable
{
    private final Model model = Model.getInstance();

    private JFXComboBox[] allComboBoxes;
    private JFXTextField[] allTextFields;

    @FXML
    private JFXComboBox<String> cbSiteName;
    @FXML
    private JFXComboBox<String> cbAssetSerialNumber;
    @FXML
    private JFXComboBox<String> cbType;
    @FXML
    private JFXComboBox<String> cbExternalWorkOrderId;
    @FXML
    private JFXComboBox<String> cbSystemStatus;
    @FXML
    private JFXComboBox<String> cbUserStatus;
    @FXML
    private JFXComboBox<String> cbCreatedOn;
    @FXML
    private JFXComboBox<String> cbCreatedBy;
    @FXML
    private JFXComboBox<String> cbName;
    @FXML
    private JFXComboBox<String> cbPriority;
    @FXML
    private JFXComboBox<String> cbStatus;
    @FXML
    private JFXComboBox<String> cbLatestFinishDate;
    @FXML
    private JFXComboBox<String> cbEarliestStartDate;
    @FXML
    private JFXComboBox<String> cbLatestStartDate;
    @FXML
    private JFXComboBox<String> cbEstimatedTime;
    @FXML
    private JFXTextField cbSiteNameD;
    @FXML
    private JFXTextField cbAssetSerialNumberD;
    @FXML
    private JFXTextField cbTypeD;
    @FXML
    private JFXTextField cbExternalWorkOrderIdD;
    @FXML
    private JFXTextField cbSystemStatusD;
    @FXML
    private JFXTextField cbUserStatusD;
    @FXML
    private JFXTextField cbCreatedOnD;
    @FXML
    private JFXTextField cbCreatedByD;
    @FXML
    private JFXTextField cbNameD;
    @FXML
    private JFXTextField cbPriorityD;
    @FXML
    private JFXTextField cbStatusD;
    @FXML
    private JFXTextField cbLatestFinishDateD;
    @FXML
    private JFXTextField cbEarliestStartDateD;
    @FXML
    private JFXTextField cbLatestStartDateD;
    @FXML
    private JFXTextField cbEstimatedTimeD;
    @FXML
    private JFXTextField txtPresetName;
    @FXML
    private Pane pane;
    @FXML
    private ImageView closeBtn;

    public void setupPresetConfigurator(File importedFileFullPath)
    {
        model.setupPresetConfigurator(importedFileFullPath, allComboBoxes);
    }

    @FXML
    void createNewPreset()
    {
        model.createNewPreset(txtPresetName, cbSiteName, cbAssetSerialNumber, cbType, cbExternalWorkOrderId, cbSystemStatus, cbUserStatus, cbCreatedOn, cbCreatedBy, cbName, cbPriority, cbStatus, cbLatestFinishDate, cbEarliestStartDate, cbLatestStartDate, cbEstimatedTime, cbSiteNameD, cbAssetSerialNumberD, cbTypeD, cbExternalWorkOrderIdD, cbSystemStatusD, cbUserStatusD, cbCreatedOnD, cbCreatedByD, cbNameD, cbPriorityD, cbStatusD, cbLatestFinishDateD, cbEarliestStartDateD, cbLatestStartDateD, cbEstimatedTimeD);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        allComboBoxes = new JFXComboBox[]{cbSiteName, cbAssetSerialNumber, cbType, cbExternalWorkOrderId, cbSystemStatus, cbUserStatus, cbCreatedOn, cbCreatedBy, cbName, cbPriority, cbStatus, cbLatestFinishDate, cbEarliestStartDate, cbLatestStartDate, cbEstimatedTime};
        allTextFields = new JFXTextField[]{cbSiteNameD, cbAssetSerialNumberD, cbTypeD, cbExternalWorkOrderIdD, cbSystemStatusD, cbUserStatusD, cbCreatedOnD, cbCreatedByD, cbNameD, cbPriorityD, cbStatusD, cbLatestFinishDateD, cbEarliestStartDateD, cbLatestStartDateD, cbEstimatedTimeD};


        model.setupComboBoxListener(allTextFields, allComboBoxes);

        model.setupAddRemoveCBitems(allComboBoxes);

        WindowManager.addDraggability(pane);
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
        Stage stage = (Stage) ((Pane) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void minimizeWindow(MouseEvent event)
    {
        Stage stage = (Stage) ((Pane) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }
}
