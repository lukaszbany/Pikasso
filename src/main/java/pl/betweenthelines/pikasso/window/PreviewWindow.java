package pl.betweenthelines.pikasso.window;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;

public class PreviewWindow {

    Stage previewStage;
    VBox vBox;
    HBox hBox;

    @Getter
    Image result;

    public PreviewWindow(Image before, Image after) {
        ImageView beforeImageView = new ImageView(before);
        beforeImageView.setPreserveRatio(true);
        beforeImageView.setFitWidth(400);
        beforeImageView.setFitHeight(400);
        ImageView afterImageView = new ImageView(after);
        afterImageView.setPreserveRatio(true);
        afterImageView.setFitWidth(400);
        afterImageView.setFitHeight(400);

        hBox = new HBox(beforeImageView, afterImageView);
        Button cancel = new Button("Odrzuć");
        cancel.setOnAction(event -> {
            result = before;
            previewStage.close();
        });
        Button save = new Button("Zachowaj");
        save.setOnAction(event -> {
            result = after;
            previewStage.close();
        });
        HBox buttons = new HBox(cancel, save);
        buttons.setPadding(new Insets(13, 0, 10, 0));
        buttons.setSpacing(15);
        buttons.setMaxHeight(55);
        buttons.setAlignment(Pos.CENTER);
        vBox = new VBox(hBox, buttons);

        double windowWidth = afterImageView.getBoundsInLocal().getWidth() * 2;
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + 55;
        Scene previewScene = new Scene(vBox, windowWidth, windowHeight);
        previewScene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) {
                result = before;
                previewStage.close();
            }
        });

        previewStage = new Stage();
        previewStage.initModality(Modality.APPLICATION_MODAL);
        previewStage.setOnCloseRequest(event -> result = before);

        previewStage.setScene(previewScene);
        previewStage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        previewStage.setTitle("Podgląd");
        save.requestFocus();
        previewStage.showAndWait();
    }
}
