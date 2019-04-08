package Model;

import BE.JSONfile;
import BE.LogEvent;
import BE.User;
import BLL.BLLmanager;
import Controller.LoginWindowController;
import Controller.MainWindowController;
import Controller.PresetWindowController;
import Util.WindowManager;
import Util.YSCException;
import com.google.gson.Gson;
import com.jfoenix.controls.*;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.*;
import org.controlsfx.control.Notifications;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static Util.ExceptionHandler.exceptionHandler;
import static de.jensd.fx.glyphs.GlyphsDude.createIcon;

public class Model
{
    private static final Model INSTANCE = new Model();
    private User currentlyLoggedIn = null;
    private BLLmanager blLmanager = new BLLmanager();

    private ArrayList<String> keys = new ArrayList<>();

    private ObservableList presets = null;

    private ArrayList<Integer> excelColumnsNumbers;
    private ArrayList<String> defaultPresetValues;

    private boolean pauseFlag;

    private Sheet sheet;

    private ObservableList<String> OLqueue = FXCollections.observableArrayList();

    private String pausedConversion;

    private File importedFileFullPath;

    private ObservableList<String> logList;

    private Properties prop = new Properties();

    private Model()
    {
    }

    public static Model getInstance()
    {
        return INSTANCE;
    }

    private String getFileExtension(String fileName)
    {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1)
        {
            return fileName.substring(i + 1).toLowerCase();
        }
        return extension;
    }

    private void loadDragAndDropFiles(FontAwesomeIconView dragAndDropIcon, JFXButton addDragAndDropFiles, Label lblQueueStatus)
    {
        List<String> validExtensions = Arrays.asList("csv", "xlsx", "xls");
        final String[] queueImportedFileName = new String[1];

        try
        {
            addDragAndDropFiles.setOnDragEntered(event ->
            {
                Dragboard db = event.getDragboard();
                if (event.getGestureSource() != addDragAndDropFiles && db.hasFiles())
                {
                    if (!validExtensions.containsAll(
                            db.getFiles().stream()
                                    .map(file -> getFileExtension(file.getName()))
                                    .collect(Collectors.toList())))
                    {
                        addDragAndDropFiles.getStyleClass().clear();
                        addDragAndDropFiles.getStyleClass().add("addDragAndDropButtonForbid");
                        dragAndDropIcon.setGlyphName("EXCLAMATION_CIRCLE");
                        addDragAndDropFiles.setText("Not correct file type(s)!\n" +
                                "(Only .xls, .xlsx, .csv)");
                    }
                    else
                    {
                        addDragAndDropFiles.getStyleClass().clear();
                        addDragAndDropFiles.getStyleClass().add("addDragAndDropButtonAllowed");
                        dragAndDropIcon.setGlyphName("CLOUD_DOWNLOAD");
                        addDragAndDropFiles.setText("Drop file(s) here!");
                    }
                }
                else
                {
                    event.consume();
                }
            });

            addDragAndDropFiles.setOnDragExited(event ->
            {
                Dragboard db = event.getDragboard();
                if (event.getGestureSource() != addDragAndDropFiles && db.hasFiles())
                {
                    addDragAndDropFiles.getStyleClass().clear();
                    addDragAndDropFiles.getStyleClass().add("addDragAndDropButton");
                    dragAndDropIcon.setGlyphName("PLUS");
                    addDragAndDropFiles.setText("Drop file(s) here\n" +
                            "or\n" +
                            "Click to select file(s)");
                }
                else
                {
                    event.consume();
                }
            });


            addDragAndDropFiles.setOnDragOver(event ->
            {
                Dragboard db = event.getDragboard();
                if (event.getGestureSource() != addDragAndDropFiles && db.hasFiles())
                {
                    if (!validExtensions.containsAll(
                            db.getFiles().stream()
                                    .map(file -> getFileExtension(file.getName()))
                                    .collect(Collectors.toList())))
                    {
                        event.consume();
                        return;
                    }
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                else
                {
                    event.consume();
                }
            });

            addDragAndDropFiles.setOnDragDropped(event ->
            {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (event.getGestureSource() != addDragAndDropFiles && db.hasFiles())
                {
                    success = true;
                    String queueFile;
                    for (File file : db.getFiles())
                    {
                        queueImportedFileName[0] = file.getName();
                        queueFile = file.getAbsolutePath();
                        OLqueue.add(queueFile);

                        try
                        {
                            saveToLog(new LogEvent("Loaded file: " + queueImportedFileName[0]));
                        }
                        catch (YSCException e)
                        {
                            e.printStackTrace();
                            exceptionHandler(e);
                        }
                    }
                    lblQueueStatus.setText("Queued files left: " + OLqueue.size());
                }
                event.setDropCompleted(success);
                if (event.isDropCompleted())
                {
                    conversionSuccessfulAndDelay(dragAndDropIcon, addDragAndDropFiles);
                }
                event.consume();
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private User loginManager(String username, String password) throws YSCException
    {
        User temp = blLmanager.loginManager(username, password);
        if (temp != null)
        {
            currentlyLoggedIn = temp;
        }
        return temp;
    }

    private String getCurrentUser()
    {
        String noUser = "No User found!";
        if (currentlyLoggedIn != null)
        {
            return currentlyLoggedIn.getUsername();
        }
        else
        {
            return noUser;
        }
    }

    private void saveToLog(LogEvent event) throws YSCException
    {
        if (currentlyLoggedIn != null)
        {
            event.setUserId(currentlyLoggedIn.getUserId());
            event.addUsernameToEvent(currentlyLoggedIn.getUsername());
        }
        else
        {
            event.setUserId(0);
        }

        blLmanager.saveToLog(event);
    }

    public void createNewPreset(JFXTextField txtPresetName, JFXComboBox<String> cbSiteName, JFXComboBox<String> cbAssetSerialNumber, JFXComboBox<String> cbType, JFXComboBox<String> cbExternalWorkOrderId, JFXComboBox<String> cbSystemStatus, JFXComboBox<String> cbUserStatus, JFXComboBox<String> cbCreatedOn, JFXComboBox<String> cbCreatedBy, JFXComboBox<String> cbName, JFXComboBox<String> cbPriority, JFXComboBox<String> cbStatus, JFXComboBox<String> cbLatestFinishDate, JFXComboBox<String> cbEarliestStartDate, JFXComboBox<String> cbLatestStartDate, JFXComboBox<String> cbEstimatedTime, JFXTextField cbSiteNameD, JFXTextField cbAssetSerialNumberD, JFXTextField cbTypeD, JFXTextField cbExternalWorkOrderIdD, JFXTextField cbSystemStatusD, JFXTextField cbUserStatusD, JFXTextField cbCreatedOnD, JFXTextField cbCreatedByD, JFXTextField cbNameD, JFXTextField cbPriorityD, JFXTextField cbStatusD, JFXTextField cbLatestFinishDateD, JFXTextField cbEarliestStartDateD, JFXTextField cbLatestStartDateD, JFXTextField cbEstimatedTimeD)
    {
        if (txtPresetName.getText().trim().equals(""))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No name entered!");
            alert.setHeaderText("Please insert some text/a name for the preset");
            alert.show();
        }
        else
        {
            //<editor-fold desc="set combo box values">
            int cbSiteNameValue;
            try
            {
                cbSiteNameValue = Integer.parseInt(cbSiteName.getSelectionModel().getSelectedItem().substring(cbSiteName.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbSiteNameValue = -1;
            }

            int cbAssetSerialNumberValue;
            try
            {
                cbAssetSerialNumberValue = Integer.parseInt(cbAssetSerialNumber.getSelectionModel().getSelectedItem().substring(cbAssetSerialNumber.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbAssetSerialNumberValue = -1;
            }

            int cbTypeValue;
            try
            {
                cbTypeValue = Integer.parseInt(cbType.getSelectionModel().getSelectedItem().substring(cbType.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbTypeValue = -1;
            }

            int cbExternalWorkOrderIdValue;
            try
            {
                cbExternalWorkOrderIdValue = Integer.parseInt(cbExternalWorkOrderId.getSelectionModel().getSelectedItem().substring(cbExternalWorkOrderId.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbExternalWorkOrderIdValue = -1;
            }

            int cbSystemStatusValue;
            try
            {
                cbSystemStatusValue = Integer.parseInt(cbSystemStatus.getSelectionModel().getSelectedItem().substring(cbSystemStatus.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbSystemStatusValue = -1;
            }

            int cbUserStatusValue;
            try
            {
                cbUserStatusValue = Integer.parseInt(cbUserStatus.getSelectionModel().getSelectedItem().substring(cbUserStatus.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbUserStatusValue = -1;
            }

            int cbCreatedOnValue;
            try
            {
                cbCreatedOnValue = Integer.parseInt(cbCreatedOn.getSelectionModel().getSelectedItem().substring(cbCreatedOn.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbCreatedOnValue = -1;
            }

            int cbCreatedByValue;
            try
            {
                cbCreatedByValue = Integer.parseInt(cbCreatedBy.getSelectionModel().getSelectedItem().substring(cbCreatedBy.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbCreatedByValue = -1;
            }

            int cbNameValue;
            try
            {
                cbNameValue = Integer.parseInt(cbName.getSelectionModel().getSelectedItem().substring(cbName.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbNameValue = -1;
            }

            int cbPriorityValue;
            try
            {
                cbPriorityValue = Integer.parseInt(cbPriority.getSelectionModel().getSelectedItem().substring(cbPriority.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbPriorityValue = -1;
            }

            int cbStatusValue;
            try
            {
                cbStatusValue = Integer.parseInt(cbStatus.getSelectionModel().getSelectedItem().substring(cbStatus.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbStatusValue = -1;
            }

            int cbLatestFinishDateValue;
            try
            {
                cbLatestFinishDateValue = Integer.parseInt(cbLatestFinishDate.getSelectionModel().getSelectedItem().substring(cbLatestFinishDate.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbLatestFinishDateValue = -1;
            }

            int cbEarliestStartDateValue;
            try
            {
                cbEarliestStartDateValue = Integer.parseInt(cbEarliestStartDate.getSelectionModel().getSelectedItem().substring(cbEarliestStartDate.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbEarliestStartDateValue = -1;
            }

            int cbLatestStartDateValue;
            try
            {
                cbLatestStartDateValue = Integer.parseInt(cbLatestStartDate.getSelectionModel().getSelectedItem().substring(cbLatestStartDate.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbLatestStartDateValue = -1;
            }

            int cbEstimatedTimeValue;
            try
            {
                cbEstimatedTimeValue = Integer.parseInt(cbEstimatedTime.getSelectionModel().getSelectedItem().substring(cbEstimatedTime.getSelectionModel().getSelectedItem().lastIndexOf(' ') + 1)) - 1;
            }
            catch (Exception e)
            {
                cbEstimatedTimeValue = -1;
            }
            //</editor-fold>
            try
            {
                blLmanager.createNewPreset(txtPresetName.getText().trim(),
                        cbSiteNameValue,
                        cbAssetSerialNumberValue,
                        cbTypeValue,
                        cbExternalWorkOrderIdValue,
                        cbSystemStatusValue,
                        cbUserStatusValue,
                        cbCreatedOnValue,
                        cbCreatedByValue,
                        cbNameValue,
                        cbPriorityValue,
                        cbStatusValue,
                        cbLatestFinishDateValue,
                        cbEarliestStartDateValue,
                        cbLatestStartDateValue,
                        cbEstimatedTimeValue,
                        cbSiteNameD.getText().trim(),
                        cbAssetSerialNumberD.getText().trim(),
                        cbTypeD.getText().trim(),
                        cbExternalWorkOrderIdD.getText().trim(),
                        cbSystemStatusD.getText().trim(),
                        cbUserStatusD.getText().trim(),
                        cbCreatedOnD.getText().trim(),
                        cbCreatedByD.getText().trim(),
                        cbNameD.getText().trim(),
                        cbPriorityD.getText().trim(),
                        cbStatusD.getText().trim(),
                        cbLatestFinishDateD.getText().trim(),
                        cbEarliestStartDateD.getText().trim(),
                        cbLatestStartDateD.getText().trim(),
                        cbEstimatedTimeD.getText().trim());
            }
            catch (YSCException ex)
            {
                Logger.getLogger(PresetWindowController.class.getName()).log(Level.SEVERE, null, ex);
                exceptionHandler(ex);
            }

            try
            {
                saveToLog(new LogEvent("Saved new preset: " + txtPresetName.getText().trim()));
            }
            catch (YSCException ex)
            {
                Logger.getLogger(PresetWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }

            Stage stage = (Stage) cbSiteName.getScene().getWindow();
            stage.close();
        }

    }

    private String getSetting()
    {
        InputStream input = null;
        try
        {
            Properties prop = new Properties();

            input = new FileInputStream("settings.properties");
            prop.load(input);

            return prop.getProperty("defaultSaveLocation"); //possible to make dynamic with parameters (if we make other settings)
        }
        catch (IOException ex)
        {
//            ex.printStackTrace();
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return "Invalid Path";
    }

    public void convertUsingPreset(String selectedPresetString, JFXListView<String> lvQueue, javafx.scene.control.Label lblQueueStatus, JFXButton pauseConversionBtn)
    {
        if (selectedPresetString != null)
        {
            try
            {
                HashMap<Integer, ArrayList> presetHM = blLmanager.convertUsingPreset(selectedPresetString);
                excelColumnsNumbers = presetHM.get(0);
                defaultPresetValues = presetHM.get(1);
            }
            catch (YSCException e)
            {
                try
                {
                    saveToLog(new LogEvent(e.getMessage()));
                }
                catch (YSCException ex)
                {
                    Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                }
                e.printStackTrace();
                exceptionHandler(e);
            }

            Thread thread = new Thread(() ->
            {
                pauseFlag = false;

                FileInputStream inp;

                try
                {
                    int queueSize = lvQueue.getItems().size();
                    for (int i = 0; i < queueSize; i++)
                    {
                        if (lvQueue.getItems().size() > 0)
                        {
                            if (lvQueue.getItems().get(0).endsWith(".csv"))
                            {
                                CSVtoExcel(lvQueue.getItems().get(0));

                                inp = new FileInputStream("CSVtoExcel.xls");
                            }
                            else
                            {
                                inp = new FileInputStream(lvQueue.getItems().get(0));
                            }

                            Workbook workbook = WorkbookFactory.create(inp);
                            sheet = workbook.getSheetAt(0);

                            JSONArray jsoNfile = makeJSONfileObject();

                            Gson gson = new Gson();
                            gson.toJson(jsoNfile);

                            String fileName = lvQueue.getItems().get(0);

                            Platform.runLater(() ->
                            {
                                lvQueue.getItems().remove(0);
                                lblQueueStatus.setText("Queued files left: " + OLqueue.size());
                                System.out.println("Conversions left: " + lvQueue.getItems().size());
                            });
                            Thread.sleep(1);

                            int fileNameLast = fileName.lastIndexOf("\\");
                            fileName = fileName.substring(fileNameLast, fileName.length());
                            try (FileWriter writer = new FileWriter(getSetting() + "\\" + fileName + ".json"))
                            {
                                gson.toJson(jsoNfile, writer);
                                saveToLog(new LogEvent("Converted file: " + fileName + " to json using the [" + selectedPresetString + "] preset."));

                            }
                            catch (IOException e)
                            {
                                try
                                {
                                    saveToLog(new LogEvent("The outputfile could not be written. Check folder existance and restrictions."));
                                }
                                catch (YSCException ex)
                                {
                                    Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                e.printStackTrace();
                                exceptionHandler(new YSCException("The outputfile could not be written. Check folder existance and restrictions."));
                            }
                            catch (YSCException ex)
                            {
                                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            finally
                            {
                                workbook.close();

                                if (lvQueue.getItems().size() == 0)
                                {
                                    Platform.runLater(() ->
                                            Notifications.create()
                                                    .title("Conversion queue information")
                                                    .hideAfter(Duration.INDEFINITE)
                                                    .text("All queued conversions has finished")
                                                    .showInformation());
                                }
                            }

                            synchronized (this)
                            {
                                if (pauseFlag)
                                {
                                    wait();
                                }
                            }
                        }
                    }

                    pauseConversionBtn.setDisable(true);
                    System.out.println("Conversion done...");
                }
                catch (IOException | InvalidFormatException e)
                {
                    e.printStackTrace();
                    exceptionHandler(new YSCException("The excel file was not found."));
                }
                catch (InterruptedException e)
                {
                    System.out.println("Conversion Thread interrupted.");
                }
            });
            thread.setDaemon(true);
            thread.start();
            pauseConversionBtn.setDisable(false);
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No preset chosen");
            alert.setHeaderText("Please select a preset to use for the queued conversions");
            alert.show();
        }

    }

    private JSONArray makeJSONfileObject()
    {
        JSONArray JSONfileArray = new JSONArray();

        for (int i = 1; i < sheet.getLastRowNum() + 1; i++)
        {
            JSONfile jsoNfile = new JSONfile();

            try
            {
                jsoNfile.setSiteName(String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(0))));
            }
            catch (Exception e)
            {
                jsoNfile.setSiteName(defaultPresetValues.get(0));
            }
            try
            {
                jsoNfile.setAssetSerialNumber(String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(1))));
            }
            catch (Exception e)
            {
                jsoNfile.setAssetSerialNumber(defaultPresetValues.get(1));
            }
            try
            {
                jsoNfile.setType(String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(2))));
            }
            catch (Exception e)
            {
                jsoNfile.setType(defaultPresetValues.get(2));
            }
            try
            {
                jsoNfile.setExternalWorkOrderId(String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(3))));
            }
            catch (Exception e)
            {
                jsoNfile.setExternalWorkOrderId(defaultPresetValues.get(3));
            }
            try
            {
                jsoNfile.setSystemStatus(String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(4))));
            }
            catch (Exception e)
            {
                jsoNfile.setSystemStatus(defaultPresetValues.get(4));
            }
            try
            {
                jsoNfile.setUserStatus(String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(5))));
            }
            catch (Exception e)
            {
                jsoNfile.setUserStatus(defaultPresetValues.get(5));
            }
            try
            {
                jsoNfile.setCreatedOn(String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(6))));
            }
            catch (Exception e)
            {
                jsoNfile.setCreatedOn(defaultPresetValues.get(6));
            }
            try
            {
                jsoNfile.setCreatedBy(String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(7))));
            }
            catch (Exception e)
            {
                jsoNfile.setCreatedBy(defaultPresetValues.get(7));
            }
            try
            {
                jsoNfile.setName(String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(8))));
            }
            catch (Exception e)
            {
                jsoNfile.setName(defaultPresetValues.get(8));
            }
            try
            {
                jsoNfile.setPriority(String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(9))));
            }
            catch (Exception e)
            {
                jsoNfile.setPriority(defaultPresetValues.get(9));
            }
            try
            {
                jsoNfile.setStatus(String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(10))));
            }
            catch (Exception e)
            {
                jsoNfile.setStatus(defaultPresetValues.get(10));
            }

            JSONObject planning = new JSONObject();
            try
            {
                planning.put("latestFinishDate", String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(11))));
            }
            catch (Exception e)
            {
                planning.put("latestFinishDate", defaultPresetValues.get(11));
            }
            try
            {
                planning.put("earliestStartDate", String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(12))));
            }
            catch (Exception e)
            {
                planning.put("earliestStartDate", defaultPresetValues.get(12));
            }
            try
            {
                planning.put("latestStartDate", String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(13))));
            }
            catch (Exception e)
            {
                planning.put("latestStartDate", defaultPresetValues.get(13));
            }
            try
            {
                planning.put("estimatedTime", String.valueOf(sheet.getRow(i).getCell(excelColumnsNumbers.get(14))));
            }
            catch (Exception e)
            {
                planning.put("estimatedTime", defaultPresetValues.get(14));
            }
            jsoNfile.setPlanning(planning);

            JSONfileArray.add(jsoNfile);
        }

        return JSONfileArray;
    }

    public void setupPresetConfigurator(File importedFileFullPath, JFXComboBox[] allComboBoxes)
    {
        FileInputStream inp = null;
        try
        {
            inp = new FileInputStream(importedFileFullPath);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            exceptionHandler(new YSCException("Error creating File Input Stream. File not found."));

        }
        Workbook workbook = null;
        try
        {
            workbook = WorkbookFactory.create(inp);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InvalidFormatException e)
        {
            e.printStackTrace();
            exceptionHandler(new YSCException("Error loading file.Wrong format."));
        }

        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter dataFormatter = new DataFormatter();

        int iterCounter = 1;
        for (Cell cell : sheet.getRow(0))
        {
            String cellValue = dataFormatter.formatCellValue(cell);
            keys.add(cellValue + " - " + iterCounter);
            iterCounter++;
        }

        for (JFXComboBox jfxComboBox : allComboBoxes)
        {
            jfxComboBox.getItems().addAll(keys);
        }

        try
        {
            workbook.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            exceptionHandler(new YSCException("An error occurred while closing the file."));
        }
        try
        {
            inp.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            exceptionHandler(new YSCException("An error occurred while closing the File Input Stream."));
        }

    }

    public void setupAddRemoveCBitems(JFXComboBox[] allComboBoxes)
    {
        ArrayList<Object> removedItems = new ArrayList<>();
        for (JFXComboBox comboBox : allComboBoxes)
        {
            comboBox.valueProperty().addListener(observable ->
            {
                Object selectedItem = comboBox.getSelectionModel().getSelectedItem();
                for (JFXComboBox cBox : allComboBoxes)
                {
                    try
                    {
                        if (cBox.getSelectionModel().getSelectedItem().equals(selectedItem))
                        {
                            removedItems.add(selectedItem);
                        }
                        else
                        {
                            cBox.getItems().remove(selectedItem);
                        }
                    }
                    catch (Exception ignored)
                    {
                        cBox.getItems().remove(selectedItem);
                    }
                }


                for (Object removedItem : removedItems)
                {
                    boolean trigger = false;
                    for (JFXComboBox box : allComboBoxes)
                    {
                        try
                        {
                            if (box.getSelectionModel().getSelectedItem().equals(removedItem))
                            {
                                trigger = true;
                            }
                        }
                        catch (Exception ignored)
                        {
                        }
                    }

                    if (!trigger)
                    {
                        int removedItemIndex = Integer.parseInt(removedItem.toString().substring(removedItem.toString().lastIndexOf(' ') + 1));
                        for (JFXComboBox addToCB : allComboBoxes)
                        {
                            ObservableList addToCBItems = addToCB.getItems();
                            int indexCounter = 0;
                            for (Object addToCBItem : addToCBItems)
                            {
                                if (addToCBItem.equals(removedItem))
                                {
                                    break;
                                }
                                indexCounter++;
                                int addToCBItemIndex = Integer.parseInt(addToCBItem.toString().substring(addToCBItem.toString().lastIndexOf(' ') + 1));
                                if (removedItemIndex < addToCBItemIndex)
                                {
                                    addToCB.getItems().add(indexCounter - 1, removedItem);
                                    break;
                                }
                            }
                        }
                        Platform.runLater(() -> removedItems.remove(removedItem));
                    }
                }
            });
        }

    }

    public void setupComboBoxListener(JFXTextField[] allTextFields, JFXComboBox[] allComboBoxes)
    {
        int iterCount = 0;
        for (JFXTextField textField : allTextFields)
        {
            int finalIterCount = iterCount;
            textField.textProperty().addListener(observable ->
            {
                if (textField.getLength() > 0)
                {
                    allComboBoxes[finalIterCount].getSelectionModel().clearSelection();
                    allComboBoxes[finalIterCount].setDisable(true);
                }
                else
                {
                    allComboBoxes[finalIterCount].setDisable(false);
                }
            });
            iterCount++;
        }
    }

    public void login(JFXTextField txtUsername, JFXPasswordField txtPassword) throws IOException
    {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        User user = null;
        try
        {
            user = loginManager(username, password);
        }
        catch (YSCException e)
        {
            exceptionHandler(e);
        }
        if (user != null)
        {
            FXMLLoader fxmlLoader1 = new FXMLLoader(getClass().getResource("/View/MainWindow.fxml"));
            Parent root = fxmlLoader1.load();

            Stage stage = new Stage();

            Image appIcon = new Image(getClass().getResourceAsStream("/Images/ShoreLineIcon.png"));
            stage.getIcons().add(appIcon);

            stage.setScene(new Scene(root));
            stage.setTitle("ShoreLine Converter - Logged in as: " + user.getUsername());
            stage.setMinHeight(640);
            stage.setMinWidth(918);

            try
            {
                saveToLog(new LogEvent("Logged in"));
            }
            catch (YSCException e)
            {
                exceptionHandler(e);
            }

            stage.show();

            Stage stageClose = (Stage) txtUsername.getScene().getWindow();
            stageClose.close();
        }
        else
        {
            try
            {
                saveToLog(new LogEvent("Someone tried unsucsessfully to log in with the username: " + username));
            }
            catch (YSCException ex)
            {
                Logger.getLogger(LoginWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("The username or password is wrong!");
            alert.showAndWait();
        }

    }

    public void openConfigWindow(ListView<String> lvPresets, JFXListView<String> lvPresetsHome) throws IOException
    {
        FXMLLoader fxmlLoader1 = new FXMLLoader(getClass().getResource("/View/PresetWindow.fxml"));
        Parent root = null;
        try
        {
            root = fxmlLoader1.load();
        }
        catch (IOException e)
        {
            exceptionHandler(new YSCException("Can not load Preset Window."));
        }
        Stage stage = new Stage();

        Image appIcon = new Image(getClass().getResourceAsStream("/Images/ShoreLineIcon.png"));
        stage.getIcons().add(appIcon);

        Scene scene;

        scene = WindowManager.getOSDependentScene(root, stage);

        if (importedFileFullPath.toString().endsWith(".csv"))
        {
            CSVtoExcel(importedFileFullPath.toString());

            PresetWindowController presetWindowController = fxmlLoader1.getController();
            presetWindowController.setupPresetConfigurator(new File("CSVtoExcel.xls"));
        }
        else
        {
            PresetWindowController presetWindowController = fxmlLoader1.getController();
            presetWindowController.setupPresetConfigurator(importedFileFullPath);
        }

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("ShoreLine Converter - Preset Configurator");
        stage.setScene(scene);
        stage.showAndWait();

        try
        {
            presets = blLmanager.getPresets();
        }
        catch (YSCException e)
        {
            try
            {
                saveToLog(new LogEvent(e.getMessage()));
            }
            catch (YSCException ex)
            {
                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
            exceptionHandler(e);
        }
        lvPresets.setItems(presets);
        lvPresetsHome.setItems(presets);

    }

    private void CSVtoExcel(String CSVfile) throws IOException
    {
        ArrayList arrayList;
        ArrayList arrayList2;
        String thisLine;

        FileInputStream fileInputStream = null;
        try
        {
            fileInputStream = new FileInputStream(CSVfile);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            exceptionHandler(new YSCException("The CSV file was not found."));
        }
        BufferedReader myInput = new BufferedReader(new InputStreamReader(fileInputStream));
        arrayList2 = new ArrayList();
        while ((thisLine = myInput.readLine()) != null)
        {
            String strar[] = thisLine.split(",");
            arrayList = new ArrayList(Arrays.asList(strar));
            arrayList2.add(arrayList);
        }

        try
        {
            HSSFWorkbook hwb = new HSSFWorkbook();
            HSSFSheet sheet = hwb.createSheet("1");
            for (int k = 0; k < arrayList2.size(); k++)
            {
                ArrayList arrayData = (ArrayList) arrayList2.get(k);
                HSSFRow row = sheet.createRow(k);
                for (int p = 0; p < arrayData.size(); p++)
                {
                    HSSFCell cell = row.createCell((short) p);
                    String data = arrayData.get(p).toString();
                    if (data.startsWith("="))
                    {
                        cell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING);
                        data = data.replaceAll("\"", "");
                        data = data.replaceAll("=", "");
                        cell.setCellValue(data);
                    }
                    else if (data.startsWith("\""))
                    {
                        data = data.replaceAll("\"", "");
                        cell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING);
                        cell.setCellValue(data);
                    }
                    else
                    {
                        data = data.replaceAll("\"", "");
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cell.setCellValue(data);
                    }
                }
            }
            FileOutputStream fileOut = new FileOutputStream("CSVtoExcel.xls");
            hwb.write(fileOut);
            fileOut.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            exceptionHandler(new YSCException("An error occurred while creating an excel file from csv."));
        }
    }

    public void loadQueueFiles(Label lblQueueStatus, FontAwesomeIconView dragAndDropIcon, JFXButton addDragAndDropFiles)
    {
        String queueImportedFileName;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File To Convert");
        fileChooser.getExtensionFilters().clear();
        FileChooser.ExtensionFilter fileFilter = new FileChooser.ExtensionFilter("select media(.xls, .xlsx, .csv)", "*.csv", "*.xlsx", "*.xls");
        fileChooser.getExtensionFilters().add(fileFilter);

        List<File> queueFiles = fileChooser.showOpenMultipleDialog(new Stage());

        try
        {
            for (File queueFile : queueFiles)
            {
                OLqueue.add(queueFile.getAbsolutePath());
                queueImportedFileName = queueFile.getName();

                try
                {
                    saveToLog(new LogEvent("Loaded file: " + queueImportedFileName));
                }
                catch (YSCException e)
                {
                    e.printStackTrace();
                    exceptionHandler(e);
                }
            }

            conversionSuccessfulAndDelay(dragAndDropIcon, addDragAndDropFiles);
        }
        catch (Exception ignored)
        {
        }

        lblQueueStatus.setText("Queued files left: " + OLqueue.size());

    }

    private void conversionSuccessfulAndDelay(FontAwesomeIconView dragAndDropIcon, JFXButton addDragAndDropFiles)
    {
        Task<Void> sleeper = new Task<Void>()
        {
            @Override
            protected Void call()
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                return null;
            }
        };
        sleeper.setOnScheduled(event1 ->
        {
            addDragAndDropFiles.getStyleClass().clear();
            addDragAndDropFiles.getStyleClass().add("addDragAndDropButtonSuccess");
            dragAndDropIcon.setGlyphName("CHECK_CIRCLE");
            addDragAndDropFiles.setText("Successfully added file(s)\n" +
                    "to conversion queue");
        });

        sleeper.setOnSucceeded(event1 ->
        {
            addDragAndDropFiles.getStyleClass().clear();
            addDragAndDropFiles.getStyleClass().add("addDragAndDropButton");
            dragAndDropIcon.setGlyphName("PLUS");
            addDragAndDropFiles.setText("Drop file(s) here\n" +
                    "or\n" +
                    "Click to select file(s)");
        });
        new Thread(sleeper).start();
    }

    public void initializeStuff(Label lblQueueStatus, JFXTabPane mainTPane, Tab homeTab, Tab presetsTab, Tab logTab, ListView<String> lvPresets, JFXListView<String> lvPresetsHome, JFXListView<String> lvQueue, JFXDatePicker startDate, JFXDatePicker endDate, Label userLoggedInLbl, Label txtDefaultSaveLocation, FontAwesomeIconView dragAndDropIcon, JFXButton addDragAndDropFiles)
    {
        String setting = getSetting();
        if (!setting.contains("\\"))
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Settings info");
            alert.setHeaderText("No default save location chosen.");
            alert.setContentText("We can see that there is no default save location chosen or that it is not a valid path...\n"
                    + "Please choose one now.");
            alert.showAndWait();

            mainTPane.getSelectionModel().select(3);
            homeTab.setDisable(true);
            presetsTab.setDisable(true);
            logTab.setDisable(true);
        }

        try
        {
            presets = blLmanager.getPresets();
        }
        catch (YSCException e)
        {
            try
            {
                saveToLog(new LogEvent(e.getMessage()));
            }
            catch (YSCException ex)
            {
                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
            exceptionHandler(e);
        }
        lvPresets.setItems(presets);
        lvPresetsHome.setItems(presets);

        lvQueue.setItems(OLqueue);

        getLeftoverConversions();
        saveLeftoverConversions(lvQueue);

        setuplvQueueMenu(lvQueue, lblQueueStatus);
        setuplvPresetsMenu(lvPresets, lvPresetsHome);

        lblQueueStatus.setText("Queued files left: " + OLqueue.size());

        setHomeIcon(homeTab);
        setUserLoggedIn(userLoggedInLbl);
        setDatePickers(startDate, endDate);

        lvQueue.setCellFactory(list -> new ListCell<String>()
        {
            {
                Text text = new Text();
                text.wrappingWidthProperty().bind(list.widthProperty().subtract(15));
                text.textProperty().bind(itemProperty());

                setPrefWidth(0);
                setGraphic(text);
            }
        });

        loadDragAndDropFiles(dragAndDropIcon, addDragAndDropFiles, lblQueueStatus);

        loadSettings(prop, txtDefaultSaveLocation);

    }

    private void loadSettings(Properties prop, Label txtDefaultSaveLocation)
    {
        InputStream input = null;
        try
        {
            input = new FileInputStream("settings.properties");
            prop.load(input);

            txtDefaultSaveLocation.setText(prop.getProperty("defaultSaveLocation"));
        }
        catch (IOException ex)
        {
//            ex.printStackTrace();
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setDatePickers(JFXDatePicker startDate, JFXDatePicker endDate)
    {
        startDate.setValue(LocalDate.now());
        endDate.setValue(LocalDate.now());
    }

    private void setUserLoggedIn(Label userLoggedInLbl)
    {
        userLoggedInLbl.setText(getCurrentUser());
    }

    private void setHomeIcon(Tab homeTab)
    {
        Text homeIcon = createIcon(FontAwesomeIcon.HOME, "24");
        homeIcon.setFill(Color.WHITE);

        homeTab.setGraphic(homeIcon);
    }

    private void getLeftoverConversions()
    {
        try (BufferedReader reader = new BufferedReader(new FileReader("leftoverConversions.txt")))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                OLqueue.add(line);
            }
        }
        catch (IOException e)
        {
            try
            {
                saveToLog(new LogEvent("An error occurred while getting the leftover conversions."));
            }
            catch (YSCException ex)
            {
                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
            e.printStackTrace();
            exceptionHandler(new YSCException("An error occurred while getting the leftover conversions."));
        }
    }

    private void saveLeftoverConversions(JFXListView<String> lvQueue)
    {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("leftoverConversions.txt")))
            {
                int queueSize = lvQueue.getItems().size();
                for (int i = 0; i < queueSize; i++)
                {
                    writer.write(lvQueue.getItems().get(i));
                    writer.newLine();
                }
            }
            catch (IOException e)
            {
                try
                {
                    saveToLog(new LogEvent("An error occurred while saving the leftover conversions."));
                }
                catch (YSCException ex)
                {
                    Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                }
                e.printStackTrace();
                exceptionHandler(new YSCException("An error occurred while saving the leftover conversions."));
            }

        }));
    }

    private void setuplvPresetsMenu(ListView<String> lvPresets, JFXListView<String> lvPresetsHome)
    {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deletePreset = new MenuItem("Delete preset");
        contextMenu.getItems().addAll(deletePreset);
        lvPresets.setContextMenu(contextMenu);

        deletePreset.setOnAction(ee ->
        {
            try
            {
                saveToLog(new LogEvent("Deleted preset: " + lvPresets.getSelectionModel().getSelectedItem()));
            }
            catch (YSCException ex)
            {
                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
            blLmanager.deletePreset(lvPresets.getSelectionModel().getSelectedItem());

            try
            {
                presets = blLmanager.getPresets();
            }
            catch (YSCException e)
            {
                try
                {
                    saveToLog(new LogEvent(e.getMessage()));
                }
                catch (YSCException ex)
                {
                    Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                }
                exceptionHandler(e);
            }
            lvPresets.setItems(presets);
            lvPresetsHome.setItems(presets);
        });
    }

    private void setuplvQueueMenu(JFXListView<String> lvQueue, Label lblQueueStatus)
    {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteQueuedConversion = new MenuItem("Delete queued conversion");
        contextMenu.getItems().addAll(deleteQueuedConversion);
        lvQueue.setContextMenu(contextMenu);

        deleteQueuedConversion.setOnAction(ee ->
        {
            OLqueue.remove(lvQueue.getSelectionModel().getSelectedIndex());
            lblQueueStatus.setText("Queued files left: " + OLqueue.size());
        });
    }

    private synchronized void resumeConversion(JFXButton pauseConversionBtn)
    {
        pauseFlag = false;
        notify();
        pauseConversionBtn.setText("Pause");
        System.out.println("Conversion Thread resumed");
    }

    public void startPauseConversion(JFXListView<String> lvQueue, JFXButton pauseConversionBtn)
    {
        try
        {
            if (lvQueue.getItems().size() > 0)
            {
                if (pausedConversion == null)
                {
                    pauseFlag = true;
                    pauseConversionBtn.setText("Resume");
                    System.out.println("Conversion Thread paused.");

                    pausedConversion = "Paused";
                }
                else
                {
                    resumeConversion(pauseConversionBtn);
                    pausedConversion = null;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            exceptionHandler(new YSCException("No conversions to be paused."));
        }

    }

    public void loadFile(Label lblInputFileStatus)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File To Convert");
        fileChooser.getExtensionFilters().clear();
        FileChooser.ExtensionFilter fileFilter = new FileChooser.ExtensionFilter("select media(.xls, .xlsx, .csv)", "*.csv", "*.xlsx", "*.xls");
        fileChooser.getExtensionFilters().add(fileFilter);

        importedFileFullPath = fileChooser.showOpenDialog(new Stage());
        File importedFile = new File(String.valueOf(importedFileFullPath));
        String importedFileName = importedFile.getName();
        try
        {
            saveToLog(new LogEvent("Loaded file: " + importedFileName));
        }
        catch (YSCException e)
        {
            e.printStackTrace();
            exceptionHandler(e);
        }

        lblInputFileStatus.setText("Inputfile loaded: " + importedFileName);

    }

    public void logout(Label userLoggedInLbl)
    {
        try
        {
            FXMLLoader fxmlLoader1 = new FXMLLoader(getClass().getResource("/View/LoginWindow.fxml"));
            Parent root = fxmlLoader1.load();

            Stage stage = new Stage();

            Image appIcon = new Image(getClass().getResourceAsStream("/Images/ShoreLineIcon.png"));
            stage.getIcons().add(appIcon);

            Scene scene;

            scene = WindowManager.getOSDependentScene(root, stage);

            stage.setTitle("ShoreLine Converter - Login");
            stage.setScene(scene);

            Stage stageClose = (Stage) userLoggedInLbl.getScene().getWindow();
            stageClose.close();

            try
            {
                saveToLog(new LogEvent("Logged out"));
            }
            catch (YSCException e)
            {
                exceptionHandler(e);
            }

            stage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            exceptionHandler(new YSCException("Failed to log you out, please try again..."));
        }

    }

    public void saveSettings(Label txtDefaultSaveLocation, JFXTabPane mainTPane, Tab homeTab, Tab presetsTab, Tab logTab)
    {
        if (txtDefaultSaveLocation.getText().trim().contains("\\"))
        {
            OutputStream output = null;
            try
            {
                output = new FileOutputStream("settings.properties");

                prop.setProperty("defaultSaveLocation", txtDefaultSaveLocation.getText().trim());

                prop.store(output, null);

            }
            catch (IOException io)
            {
                io.printStackTrace();
            }
            finally
            {
                if (output != null)
                {
                    try
                    {
                        output.close();

                        mainTPane.getSelectionModel().select(0);
                        if (homeTab.isDisabled() && presetsTab.isDisabled() && logTab.isDisabled())
                        {
                            homeTab.setDisable(false);
                            presetsTab.setDisable(false);
                            logTab.setDisable(false);
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Please choose a valid path");
            alert.setHeaderText("No default save location chosen");
            alert.show();
        }

    }

    public void chooseDefaultSaveLocation(Label txtDefaultSaveLocation) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException
    {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Select folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
        {
            txtDefaultSaveLocation.setText(String.valueOf(chooser.getSelectedFile()));
        }
        else
        {
            txtDefaultSaveLocation.setText("No folder selected... Please try again.");
        }

    }

    public void btnShowLog(JFXListView<String> lvLog, JFXDatePicker startDate, JFXDatePicker endDate)
    {
        LocalDateTime ldtStart = startDate.getValue().atStartOfDay().minusHours(2);
        long startEpoch = ldtStart.toInstant(ZoneOffset.UTC).toEpochMilli();
        LocalDateTime ldtEnd = endDate.getValue().plusDays(1).atStartOfDay().minusHours(2);
        long endEpoch = ldtEnd.toInstant(ZoneOffset.UTC).toEpochMilli();
        try
        {
            logList = blLmanager.getLog(startEpoch, endEpoch);
        }
        catch (YSCException ex)
        {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
            exceptionHandler(ex);
        }
        lvLog.setItems(logList);
    }
}
