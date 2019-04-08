package BLL;

import BE.LogEvent;
import BE.User;
import DAL.DBmanager;
import DAL.LogDBmanager;
import Util.YSCException;
import javafx.collections.ObservableList;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class BLLmanager
{
    private final DBmanager dBmanager = new DBmanager();
    private LogDBmanager logDb = new LogDBmanager();

    public User loginManager(String username, String password) throws YSCException
    {
        String ePassword = DigestUtils.sha512Hex(password);

        return dBmanager.loginManager(username, ePassword);
    }

    public void saveToLog(LogEvent event) throws YSCException
    {
        logDb.saveToLog(event);
    }

    public void createNewPreset(String newPresetName, int siteName, int assetSerialNumber, int type, int externalWorkOrderId, int systemStatus, int userStatus, int createdOn, int createdBy, int name, int priority, int status, int latestFinishDate, int earliestStartDate, int latestStartDate, int estimatedTime,
                                String siteNameD, String assetSerialNumberD, String typeD, String externalWorkOrderIdD, String systemStatusD, String userStatusD, String createdOnD, String createdByD, String nameD, String priorityD, String statusD, String latestFinishDateD, String earliestStartDateD, String latestStartDateD, String estimatedTimeD) throws YSCException
    {
        dBmanager.createNewPreset(newPresetName, siteName, assetSerialNumber, type, externalWorkOrderId, systemStatus, userStatus, createdOn, createdBy, name, priority, status, latestFinishDate, earliestStartDate, latestStartDate, estimatedTime,
                siteNameD, assetSerialNumberD, typeD, externalWorkOrderIdD, systemStatusD, userStatusD, createdOnD, createdByD, nameD, priorityD, statusD, latestFinishDateD, earliestStartDateD, latestStartDateD, estimatedTimeD);
    }

    public ObservableList getPresets() throws YSCException
    {
        return dBmanager.getPresets();
    }

    public HashMap<Integer, ArrayList> convertUsingPreset(String selectedPreset) throws YSCException
    {
        return dBmanager.convertUsingPreset(selectedPreset);
    }

    public void deletePreset(String selectedPreset)
    {
        dBmanager.deletePreset(selectedPreset);
    }

    public ObservableList<String> getLog(long start, long end) throws YSCException
    {
        return logDb.getLog(start, end);
    }
}
