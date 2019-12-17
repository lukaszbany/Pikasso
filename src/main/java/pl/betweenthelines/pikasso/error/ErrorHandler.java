package pl.betweenthelines.pikasso.error;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import pl.betweenthelines.pikasso.exception.FileOpenException;
import pl.betweenthelines.pikasso.exception.FileTypeNotSupported;
import pl.betweenthelines.pikasso.exception.ImageIsTooBigException;
import pl.betweenthelines.pikasso.exception.ImageNotLoadedYetException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Klasa do obsługi błędów. Przekazany do niej wyjątek wyświetli się
 * w formie komunikatu dla użytkownika.
 */
public class ErrorHandler {

    /**
     * Domyślna wiadomość dla użytkownika (w przypadku błędu, który nie jest obsługiwany).
     */
    private static final String DEFAULT_MESSAGE = "Nieobsługiwany błąd!";

    /**
     * Lista obsługiwanych błędów.
     */
    private static final Map<Class, String> MESSAGES = new HashMap<>();

    static {
        MESSAGES.put(ImageNotLoadedYetException.class, "Obraz nie został jeszcze załadowany. Spróbuj ponownie za chwilę.");
        MESSAGES.put(FileNotFoundException.class, "Plik nie istnieje!");
        MESSAGES.put(IOException.class, "Problem z odczytem z pliku!");
        MESSAGES.put(FileOpenException.class, "Problem z odczytem z pliku!");
        MESSAGES.put(FileTypeNotSupported.class, "Nieobsługiwany typ pliku!");
        MESSAGES.put(ImageIsTooBigException.class, "Obraz jest za duży!");
    }

    /**
     * Metoda służąca do wyświetlenia użytkownikowi wyjątku w formie
     * komunikatu. Jeśli wyjątek nie ma swojego komunikatu, wyświetlany
     * jest standardowy komunikat.
     *
     * @param exception wyjątek do wyświetlenia.
     */
    public static void handleError(Exception exception) {
        String message = MESSAGES.get(exception.getClass());

        showAlert(exception, message);
    }

    /**
     * Metoda służąca do wyświetlenia użytkownikowi wyjątku w formie
     * komunikatu o treści podanej jako argument.
     *
     * @param exception wyjątek do wyświetlenia.
     * @param message   treść komunikatu.
     */
    public static void showAlert(Exception exception, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Wystąpił błąd!");
        setHeaderText(message, alert);
        String stackTrace = buildStackTrace(exception);

        GridPane stackTraceContent = createStackTracePane(stackTrace);
        alert.getDialogPane().setExpandableContent(stackTraceContent);

        alert.showAndWait();
    }

    /**
     * Tworzy obszar, w którym wyświetlane są szczegóły błędu.
     *
     * @param stackTrace tekst ze szczegółami błędu
     * @return obszar ze szczegółami błędu.
     */
    private static GridPane createStackTracePane(String stackTrace) {
        Label label = new Label("Szczegóły:");
        TextArea textArea = createStackTraceText(stackTrace);

        GridPane stackTraceContent = new GridPane();
        stackTraceContent.setMaxWidth(Double.MAX_VALUE);
        stackTraceContent.add(label, 0, 0);
        stackTraceContent.add(textArea, 0, 1);
        return stackTraceContent;
    }

    /**
     * Tworzy tekst ze szczegółami błędu
     *
     * @param stackTrace tekst ze szczegółami błędu
     * @return <tt>TextArea</tt> ze szczegółami błędu
     */
    private static TextArea createStackTraceText(String stackTrace) {
        TextArea textArea = new TextArea(stackTrace);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        return textArea;
    }

    /**
     * Ustawia komunikat dla użytkownika na oknie z błędem.
     *
     * @param message treść komunikatu
     * @param alert   okno z błędem
     */
    private static void setHeaderText(String message, Alert alert) {
        if (message != null) {
            alert.setHeaderText(message);
        } else {
            alert.setHeaderText(DEFAULT_MESSAGE);
        }
    }

    /**
     * Pobiera szczegóły błędu (stack trace) z wyjątku.
     *
     * @param exception wyjątek
     * @return treść szczegółów błędu.
     */
    private static String buildStackTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

}
