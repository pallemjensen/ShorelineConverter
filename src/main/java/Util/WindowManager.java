package Util;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class WindowManager
{
    private static double xOffset = 0;
    private static double yOffset = 0;

    public static void addDraggability(final Node node)
    {
        node.setOnMousePressed(event12 ->
        {
            xOffset = event12.getSceneX();
            yOffset = event12.getSceneY();
        });

        node.setOnMouseDragged(event1 ->
        {
            node.getScene().getWindow().setX(event1.getScreenX() - xOffset);
            node.getScene().getWindow().setY(event1.getScreenY() - yOffset);
        });
    }

    public static Scene getOSDependentScene(Parent root, Stage stage)
    {
        Scene scene;
        String OSName = System.getProperty("os.name");
        if (OSName != null && OSName.startsWith("Windows"))
        {
            scene = WindowManager.getShadowScene(root);
            stage.initStyle(StageStyle.TRANSPARENT);
        }
        else
        {
            scene = new Scene(root);
            stage.initStyle(StageStyle.UNDECORATED);
        }
        return scene;
    }

    private static Scene getShadowScene(Parent p)
    {
        Scene scene;
        StackPane outer = new StackPane();
        outer.getChildren().add(p);
        outer.setPadding(new Insets(10.0d));
        outer.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0), new CornerRadii(0), new
                Insets(0))));

        p.setEffect(new DropShadow());
        ((StackPane) p).setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(0), new Insets(0)
        )));

        scene = new Scene(outer);
        scene.setFill(Color.rgb(0, 255, 0, 0));
        return scene;
    }
}
