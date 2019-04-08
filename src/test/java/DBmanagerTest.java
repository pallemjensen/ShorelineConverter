import BE.User;
import BLL.BLLmanager;
import Util.YSCException;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class DBmanagerTest
{
    private BLLmanager blLmanager = new BLLmanager();

    // Tried to create BeforeAll & AfterAll methods, but that did not work with our non-static method..
    // Used solution is to just do it before & after all the tests which need it..

    @org.junit.jupiter.api.Test
    void loginManager() throws YSCException
    {
        User user = blLmanager.loginManager("SLogin", "SPassword");

        String actualUsername = user.getUsername();

        assertEquals("SLogin", actualUsername);
    }

    @org.junit.jupiter.api.Test
    void getPresets() throws YSCException
    {
        blLmanager.createNewPreset("jUnitTesting", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "test", "test", "test", "test", "test", "test", "test", "test", "test", "test", "test", "test", "test", "test", "test");


        ObservableList presets = blLmanager.getPresets();

        int actualSize = presets.size();

        assertTrue(actualSize > 0);


        blLmanager.deletePreset("jUnitTesting");
    }

    @org.junit.jupiter.api.Test
    void convertUsingPreset() throws YSCException
    {
        blLmanager.createNewPreset("jUnitTesting", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "test", "test", "test", "test", "test", "test", "test", "test", "test", "test", "test", "test", "test", "test", "test");


        HashMap<Integer, ArrayList> jUnitTesting = blLmanager.convertUsingPreset("jUnitTesting");

        int actualSize = jUnitTesting.size();

        assertEquals(2, actualSize);


        blLmanager.deletePreset("jUnitTesting");
    }
}