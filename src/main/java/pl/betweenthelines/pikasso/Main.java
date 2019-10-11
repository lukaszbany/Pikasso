package pl.betweenthelines.pikasso;

import javafx.application.Application;
import javafx.stage.Stage;
import pl.betweenthelines.pikasso.window.MainWindow;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainWindow mainWindow = new MainWindow(primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }


}
