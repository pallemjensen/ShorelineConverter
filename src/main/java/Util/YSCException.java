package Util;

public class YSCException extends Exception
{
    public YSCException(String string)
    {
        super(string);
    }

    public YSCException(String string, Throwable thrwbl)
    {
        super(string, thrwbl);
    }

    public YSCException(Throwable thrwbl)
    {
        super(thrwbl);
    }
}