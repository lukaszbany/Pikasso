package pl.betweenthelines.pikasso.window;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;


/**
 * Klasa reprezentująca okno z informacjami o programie.
 */
public class AboutWindow {

    Stage previewStage;

    public AboutWindow() {
        Image icon = new Image("PIKAsso-icon.jpg");
        ImageView iconView = new ImageView(icon);
        iconView.setPreserveRatio(true);
        iconView.setFitWidth(100);

        Label appName = new Label("Pikasso");
        appName.setFont(new Font("Arial", 30));

        Label appVersion = new Label("v. 1.0.1");
        appName.setFont(new Font("Arial", 20));

        VBox appDataVBox = new VBox(appName, appVersion);
        appDataVBox.setAlignment(Pos.CENTER);
        appDataVBox.setSpacing(15);

        HBox appNameHBox = new HBox(iconView, appDataVBox);
        appNameHBox.setAlignment(Pos.CENTER);
        appNameHBox.setSpacing(15);

        Button okButton = new Button("OK");
        okButton.setPrefWidth(80);
        okButton.setOnAction(event -> closeWindow());

        Label appDescription = new Label("Aplikacja powstała jako projekt na zajęcia z przedmiotu\n" +
                "Algorytmy przetwarzania obrazów. Autorem aplikacji jest Łukasz Bany.");

        VBox vBox = new VBox(appNameHBox, appDescription, okButton);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(15);

        Scene previewScene = new Scene(vBox, 400, 230);
        previewScene.setOnKeyPressed(this::handleKeyPressed);

        previewStage = new Stage();
        previewStage.initModality(Modality.APPLICATION_MODAL);

        previewStage.setScene(previewScene);
        previewStage.getIcons().add(icon);
        previewStage.setTitle("O programie");

        okButton.requestFocus();
        previewStage.showAndWait();
    }

    /**
     * Obsługuje naciśnięcie przycisku ESC (zamyka okno)
     *
     * @param event akcja naciśnięcia przycisku
     */
    private void handleKeyPressed(KeyEvent event) {
        if (KeyCode.ESCAPE.equals(event.getCode())) {
            closeWindow();
        }
    }

    /**
     * Zamyka okno.
     */
    private void closeWindow() {
        previewStage.close();
    }
}
