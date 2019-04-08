package BE;

import java.util.Calendar;

public class LogEvent
{
    private int userId;
    private long timeInMilis;
    private String event;

    public LogEvent(String event)
    {
        this.event = event;
        this.timeInMilis = Calendar.getInstance().getTimeInMillis();
    }

    public void addUsernameToEvent(String name)
    {
        event = "User \"" + name + "\": " + event;
    }

    public String getEvent()
    {
        return event;
    }

    public int getUserId()
    {
        return userId;
    }

    public void setUserId(int userId)
    {
        this.userId = userId;
    }

    public long getTimeInMilis()
    {
        return timeInMilis;
    }

}
