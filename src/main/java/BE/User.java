package BE;


public class User
{
    private int userId;
    private String Username;

    public User(int userId, String Username)
    {
        this.userId = userId;
        this.Username = Username;
    }

    public String getUsername()
    {
        return Username;
    }

    public int getUserId()
    {
        return userId;
    }
}
