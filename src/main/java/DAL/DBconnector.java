package DAL;

import Util.YSCException;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import java.sql.Connection;

class DBconnector
{
    private final SQLServerDataSource ds = new SQLServerDataSource();

    DBconnector()
    {
        ds.setDatabaseName("YogaShorelineConversion");
        ds.setUser("CS2017A_28");
        ds.setPassword("Ellap060174");
        ds.setServerName("EASV-DB2");
        ds.setPortNumber(1433);
    }

    Connection getConnection() throws YSCException
    {
        try
        {
            return ds.getConnection();
        }
        catch (SQLServerException e)
        {
            throw new YSCException("An error occurred. Please make sure you are connected to the internet.");
        }
    }
}
