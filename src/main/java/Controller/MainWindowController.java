package Controller;

import Model.Model;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTabPane;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable
{
    private final Model model = Model.getInstance();

    @FXML
    private Label lblQueueStatus;
    @FXML
    private Label txtDefaultSaveLocation;
    @FXML
    private Label lblInputFileStatus;
    @FXML
    private ListView<String> lvPresets;
    @FXML
    private JFXListView<String> lvPresetsHome;
    @FXML
    private JFXListView<String> lvQueue;
    @FXML
    private Tab homeTab;
    @FXML
    private Tab presetsTab;
    @FXML
    private Tab logTab;
    @FXML
    private JFXTabPane mainTPane;
    @FXML
    private JFXButton addDragAndDropFiles;
    @FXML
    private Label userLoggedInLbl;
    @FXML
    private FontAwesomeIconView dragAndDropIcon;
    @FXML
    private JFXButton pauseConversionBtn;
    @FXML
    private JFXDatePicker startDate;
    @FXML
    private JFXDatePicker endDate;
    @FXML
    private JFXListView<String> lvLog;


    @FXML
    void openConfigWindow() throws IOException
    {
        model.openConfigWindow(lvPresets, lvPresetsHome);
    }

    @FXML
    private void loadFile()
    {
        model.loadFile(lblInputFileStatus);
    }

    @FXML
    void loadQueueFiles()
    {
        model.loadQueueFiles(lblQueueStatus, dragAndDropIcon, addDragAndDropFiles);
    }

    @FXML
    void logout()
    {
        model.logout(userLoggedInLbl);
    }

    @FXML
    void saveSettings()
    {
        model.saveSettings(txtDefaultSaveLocation, mainTPane, homeTab, presetsTab, logTab);
    }

    @FXML
    void chooseDefaultSaveLocation() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException
    {
        model.chooseDefaultSaveLocation(txtDefaultSaveLocation);
    }

    @FXML
    void convertUsingPreset()
    {
        model.convertUsingPreset(lvPresetsHome.getSelectionModel().getSelectedItem(), lvQueue, lblQueueStatus, pauseConversionBtn);
    }

    @FXML
    void startPauseConversion()
    {
        model.startPauseConversion(lvQueue, pauseConversionBtn);
    }

    @FXML
    private void btnShowLog()
    {
        model.btnShowLog(lvLog, startDate, endDate);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        model.initializeStuff(lblQueueStatus, mainTPane, homeTab, presetsTab, logTab, lvPresets, lvPresetsHome, lvQueue, startDate, endDate, userLoggedInLbl, txtDefaultSaveLocation, dragAndDropIcon, addDragAndDropFiles);
    }
}
