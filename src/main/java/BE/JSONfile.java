package BE;

import org.json.simple.JSONObject;

public class JSONfile
{
    private JSONObject planning;
    private String siteName;
    private String assetSerialNumber;
    private String type;
    private String externalWorkOrderId;
    private String systemStatus;
    private String userStatus;
    private String createdOn;
    private String createdBy;
    private String name;
    private String priority;
    private String status;

    public JSONfile()
    {
    }

    public void setPlanning(JSONObject planning)
    {
        this.planning = planning;
    }

    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }

    public void setAssetSerialNumber(String assetSerialNumber)
    {
        this.assetSerialNumber = assetSerialNumber;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setExternalWorkOrderId(String externalWorkOrderId)
    {
        this.externalWorkOrderId = externalWorkOrderId;
    }

    public void setSystemStatus(String systemStatus)
    {
        this.systemStatus = systemStatus;
    }

    public void setUserStatus(String userStatus)
    {
        this.userStatus = userStatus;
    }

    public void setCreatedOn(String createdOn)
    {
        this.createdOn = createdOn;
    }

    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
