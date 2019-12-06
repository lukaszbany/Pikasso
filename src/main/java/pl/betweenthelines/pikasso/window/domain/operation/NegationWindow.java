package pl.betweenthelines.pikasso.window.domain.operation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.domain.FileData;

public class NegationWindow {

    ImageView beforeImageView;
    ImageView afterImageView;

    Stage negationStage;
    VBox vBox;
    HBox hBox;

    public NegationWindow(FileData openedFileData) {
        Image before = ImageUtils.toGrayscale(openedFileData.getImageView().getImage());
        beforeImageView = new ImageView((before));
        beforeImageView.setPreserveRatio(true);
        beforeImageView.setFitWidth(400);
        beforeImageView.setFitHeight(400);

        Image after = negation(before);
        afterImageView = new ImageView(after);
        afterImageView.setPreserveRatio(true);
        afterImageView.setFitWidth(400);
        afterImageView.setFitHeight(400);

        hBox = new HBox(beforeImageView, afterImageView);
        Button cancel = new Button("Odrzuć");
        cancel.setOnAction(event -> {
            negationStage.close();
        });
        Button save = new Button("Zachowaj");
        save.setOnAction(event -> {
            openedFileData.setImage(after);
            negationStage.close();
        });
        HBox buttons = new HBox(cancel, save);
        buttons.setPadding(new Insets(13, 10, 10, 0));
        buttons.setSpacing(15);
        buttons.setMaxHeight(55);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        vBox = new VBox(hBox, buttons);

        double windowWidth = afterImageView.getBoundsInLocal().getWidth() * 2;
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + 55;
        Scene negationScene = new Scene(vBox, windowWidth, windowHeight);

        negationStage = new Stage();
        negationStage.initModality(Modality.APPLICATION_MODAL);

        negationStage.setScene(negationScene);
        negationStage.setTitle("Negacja");
        save.requestFocus();
        negationStage.showAndWait();
    }

    private Image negation(Image before) {

        Mat inImage = ImageUtils.imageToMat(before);
        Mat outImage = new Mat();
        Imgproc.cvtColor(inImage, outImage, Imgproc.COLOR_BGR2GRAY);
        Core.bitwise_not(outImage, outImage);

        return ImageUtils.mat2Image(outImage);
    }

}
