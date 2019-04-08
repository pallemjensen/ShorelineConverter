import Util.WindowManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class Main extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("/View/LoginWindow.fxml"));

        Image appIcon = new Image(getClass().getResourceAsStream("/Images/ShoreLineIcon.png"));
        primaryStage.getIcons().add(appIcon);

        Scene scene;

        scene = WindowManager.getOSDependentScene(root, primaryStage);

        primaryStage.setTitle("ShoreLine Converter - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
