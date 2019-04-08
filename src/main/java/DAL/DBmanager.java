package DAL;

import BE.User;
import Util.YSCException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class DBmanager
{
    public User loginManager(String username, String epassword) throws YSCException
    {
        try (Connection con = new DBconnector().getConnection())
        {
            PreparedStatement ps;
            ResultSet rs;

            ps = con.prepareStatement("SELECT User_ID, Username FROM [User] WHERE Username = ? and Password = ?");

            ps.setString(1, username);
            ps.setString(2, epassword);

            rs = ps.executeQuery();

            if (rs.next())
            {
                return new User(rs.getInt("User_ID"), username);
            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new YSCException("An error occurred while trying to log in. Please make sure your Username and Password are correct.");
        }
        return null;
    }

    public void createNewPreset(String newPresetName, int siteName, int assetSerialNumber, int type, int externalWorkOrderId, int systemStatus, int userStatus, int createdOn, int createdBy, int name, int priority, int status, int latestFinishDate, int earliestStartDate, int latestStartDate, int estimatedTime,
                                String siteNameD, String assetSerialNumberD, String typeD, String externalWorkOrderIdD, String systemStatusD, String userStatusD, String createdOnD, String createdByD, String nameD, String priorityD, String statusD, String latestFinishDateD, String earliestStartDateD, String latestStartDateD, String estimatedTimeD) throws YSCException
    {
        try (Connection con = new DBconnector().getConnection())
        {
            PreparedStatement ps;

            ps = con.prepareStatement("INSERT INTO Preset (PresetName, siteName, assetSerialNumber, type, externalWorkOrderId, systemStatus, userStatus, createdOn, createdBy, name, priority, status, latestFinishDate, earliestStartDate, latestStartDate, estimatedTime, " +
                    "siteNameD, assetSerialNumberD, typeD, externalWorkOrderIdD, systemStatusD, userStatusD, createdOnD, createdByD, nameD, priorityD, statusD, latestFinishDateD, earliestStartDateD, latestStartDateD, estimatedTimeD)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, newPresetName);
            ps.setInt(2, siteName);
            ps.setInt(3, assetSerialNumber);
            ps.setInt(4, type);
            ps.setInt(5, externalWorkOrderId);
            ps.setInt(6, systemStatus);
            ps.setInt(7, userStatus);
            ps.setInt(8, createdOn);
            ps.setInt(9, createdBy);
            ps.setInt(10, name);
            ps.setInt(11, priority);
            ps.setInt(12, status);
            ps.setInt(13, latestFinishDate);
            ps.setInt(14, earliestStartDate);
            ps.setInt(15, latestStartDate);
            ps.setInt(16, estimatedTime);
            ps.setString(17, siteNameD);
            ps.setString(18, assetSerialNumberD);
            ps.setString(19, typeD);
            ps.setString(20, externalWorkOrderIdD);
            ps.setString(21, systemStatusD);
            ps.setString(22, userStatusD);
            ps.setString(23, createdOnD);
            ps.setString(24, createdByD);
            ps.setString(25, nameD);
            ps.setString(26, priorityD);
            ps.setString(27, statusD);
            ps.setString(28, latestFinishDateD);
            ps.setString(29, earliestStartDateD);
            ps.setString(30, latestStartDateD);
            ps.setString(31, estimatedTimeD);

            ps.execute();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new YSCException("An error occurred while trying to create a preset.");
        }

    }

    public ObservableList getPresets() throws YSCException
    {
        ObservableList<String> presets = FXCollections.observableArrayList();

        try (Connection con = new DBconnector().getConnection())
        {
            PreparedStatement ps;
            ResultSet rs;

            ps = con.prepareStatement("SELECT PresetName FROM Preset");

            rs = ps.executeQuery();

            while (rs.next())
            {
                presets.add((rs.getString("PresetName")));
            }
            return presets;

        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new YSCException("An error occurred while loading your chosen preset. Please make sure you are connected to the internet.");
        }

    }

    public HashMap<Integer, ArrayList> convertUsingPreset(String selectedPreset) throws YSCException
    {
        ArrayList<Integer> excelColumnsNumbers = new ArrayList<>();
        ArrayList<String> defaultPresetValues = new ArrayList<>();

        HashMap<Integer, ArrayList> presetHM = new HashMap<>();

        try (Connection con = new DBconnector().getConnection())
        {
            PreparedStatement ps;
            ResultSet rs;

            ps = con.prepareStatement("SELECT siteName, assetSerialNumber, type, externalWorkOrderId, systemStatus, userStatus, createdOn, createdBy, name, priority, status, latestFinishDate, earliestStartDate, latestStartDate, estimatedTime," +
                    "siteNameD, assetSerialNumberD, typeD, externalWorkOrderIdD, systemStatusD, userStatusD, createdOnD, createdByD, nameD, priorityD, statusD, latestFinishDateD, earliestStartDateD, latestStartDateD, estimatedTimeD FROM Preset where PresetName = ?");

            ps.setString(1, selectedPreset);

            rs = ps.executeQuery();

            if (rs.next())
            {
                excelColumnsNumbers.add(rs.getInt(1));
                excelColumnsNumbers.add(rs.getInt(2));
                excelColumnsNumbers.add(rs.getInt(3));
                excelColumnsNumbers.add(rs.getInt(4));
                excelColumnsNumbers.add(rs.getInt(5));
                excelColumnsNumbers.add(rs.getInt(6));
                excelColumnsNumbers.add(rs.getInt(7));
                excelColumnsNumbers.add(rs.getInt(8));
                excelColumnsNumbers.add(rs.getInt(9));
                excelColumnsNumbers.add(rs.getInt(10));
                excelColumnsNumbers.add(rs.getInt(11));
                excelColumnsNumbers.add(rs.getInt(12));
                excelColumnsNumbers.add(rs.getInt(13));
                excelColumnsNumbers.add(rs.getInt(14));
                excelColumnsNumbers.add(rs.getInt(15));
                presetHM.put(0, excelColumnsNumbers);

                defaultPresetValues.add(rs.getString(16));
                defaultPresetValues.add(rs.getString(17));
                defaultPresetValues.add(rs.getString(18));
                defaultPresetValues.add(rs.getString(19));
                defaultPresetValues.add(rs.getString(20));
                defaultPresetValues.add(rs.getString(21));
                defaultPresetValues.add(rs.getString(22));
                defaultPresetValues.add(rs.getString(23));
                defaultPresetValues.add(rs.getString(24));
                defaultPresetValues.add(rs.getString(25));
                defaultPresetValues.add(rs.getString(26));
                defaultPresetValues.add(rs.getString(27));
                defaultPresetValues.add(rs.getString(28));
                defaultPresetValues.add(rs.getString(29));
                defaultPresetValues.add(rs.getString(30));
                presetHM.put(1, defaultPresetValues);

            }

            return presetHM;

        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new YSCException("An error occurred while trying to convert a file using the chosen preset.");
        }

    }

    public void deletePreset(String selectedPreset)
    {
        try (Connection con = new DBconnector().getConnection())
        {
            PreparedStatement ps;

            ps = con.prepareStatement("DELETE FROM Preset where PresetName = ?");
            ps.setString(1, selectedPreset);

            ps.execute();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
