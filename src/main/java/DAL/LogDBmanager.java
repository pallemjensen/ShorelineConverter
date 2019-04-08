package DAL;

import BE.LogEvent;
import Util.YSCException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LogDBmanager
{
    public void saveToLog(LogEvent event) throws YSCException
    {
        try (Connection con = new DBconnector().getConnection())
        {
            PreparedStatement ps;
            java.sql.Timestamp time = new java.sql.Timestamp(event.getTimeInMilis());
            ps = con.prepareStatement("INSERT INTO Log(user_id, time, event) VALUES(?,?,?)");

            ps.setInt(1, event.getUserId());
            ps.setTimestamp(2, time);
            ps.setString(3, event.getEvent());

            ps.execute();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new YSCException("An error occurred while the application was logging. (logEvent)");
        }
    }

    public ObservableList<String> getLog(long start, long end) throws YSCException
    {
        ObservableList<String> log = FXCollections.observableArrayList();

        try (Connection con = new DBconnector().getConnection())
        {
            PreparedStatement ps;
            ResultSet rs;

            java.sql.Timestamp stime = new java.sql.Timestamp(start);
            java.sql.Timestamp etime = new java.sql.Timestamp(end);
            ps = con.prepareStatement("SELECT*\n"
                    + "  FROM [YogaShorelineConversion].[dbo].[Log]\n"
                    + "  WHERE time > ? AND time < ?\n"
                    + "  order by time desc");
            ps.setTimestamp(1, stime);
            ps.setTimestamp(2, etime);
            rs = ps.executeQuery();

            while (rs.next())
            {
                String s = rs.getTimestamp("time").toString().subSequence(0, 19) + " | " + rs.getNString("event");
                log.add(s);
            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new YSCException("An error occurred while the application loading the log.");
        }

        return log;
    }
}
