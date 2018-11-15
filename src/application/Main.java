package application;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;

public class Main extends Application
{
    public Main()
    {
    }

    public void start(Stage primaryStage)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("CameraApp.fxml"));
            TabPane rootElement = loader.load();
            Scene scene = new Scene(rootElement, 800.0D, 600.0D);
            primaryStage.setTitle("Camera");
            primaryStage.setScene(scene);
            primaryStage.show();
            final application.CameraAppFXController controller = loader.getController();
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>()
            {
                public void handle(WindowEvent we) {
                    controller.setClosed();
                }
            });
        }
        catch (Exception var6)
        {
            var6.printStackTrace();
        }

    }

    public static void main(String[] args)
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}