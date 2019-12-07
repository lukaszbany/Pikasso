package pl.betweenthelines.pikasso.error;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import pl.betweenthelines.pikasso.exception.FileOpenException;
import pl.betweenthelines.pikasso.exception.FileTypeNotSupported;
import pl.betweenthelines.pikasso.exception.ImageIsTooBigException;
import pl.betweenthelines.pikasso.exception.ImageNotLoadedYetException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ErrorHandler {

    private static final String DEFAULT_MESSAGE = "Nieobsługiwany błąd!";

    private static final Map<Class, String> MESSAGES = new HashMap<>();

    static {
        MESSAGES.put(ImageNotLoadedYetException.class, "Obraz nie został jeszcze załadowany. Spróbuj ponownie za chwilę.");
        MESSAGES.put(FileNotFoundException.class, "Plik nie istnieje!");
        MESSAGES.put(IOException.class, "Problem z odczytem z pliku!");
        MESSAGES.put(FileOpenException.class, "Problem z odczytem z pliku!");
        MESSAGES.put(FileTypeNotSupported.class, "Nieobsługiwany typ pliku!");
        MESSAGES.put(ImageIsTooBigException.class, "Obraz jest za duży!");
    }

    public static void handleError(Exception exception) {
        String message = MESSAGES.get(exception.getClass());

        showAlert(exception, message);
    }

    public static void showAlert(Exception exception, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Wystąpił błąd!");
        setHeaderText(message, alert);
        String stackTrace = buildStackTrace(exception);

        Label label = new Label("Szczegóły:");

        TextArea textArea = new TextArea(stackTrace);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        GridPane stackTraceContent = new GridPane();
        stackTraceContent.setMaxWidth(Double.MAX_VALUE);
        stackTraceContent.add(label, 0, 0);
        stackTraceContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(stackTraceContent);

        alert.showAndWait();
    }

    private static void setHeaderText(String message, Alert alert) {
        if (message != null) {
            alert.setHeaderText(message);
        } else {
            alert.setHeaderText(DEFAULT_MESSAGE);
        }
    }

    private static String buildStackTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

}
