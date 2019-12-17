package pl.betweenthelines.pikasso.window.image;

import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import lombok.Data;

import java.io.File;

/**
 * Klasa przechowująca informacje o otwartym obrazie.
 */
@Data
public class FileData {

    /**
     * Otwarty plik.
     */
    private File file;

    /**
     * <tt>ImageView</tt> zawierający z otwartym obrazem.
     */
    private ImageView imageView;

    /**
     * Zaznaczony fragment obrazu
     */
    private Image imageSelection;

    /**
     * Prostokąt reprezentujący zaznaczenie.
     */
    private Rectangle selection;

    /**
     * Poprzedni zachowany stan obrazu (przywracany w razie cofnięcia
     * ostatniej operacji).
     */
    private Image previousImage;

    /**
     * Opcja menu "cofnij". Przechowywana w FileData, aby ją zablokować,
     * po cofnięciu operacji (możliwe jest tylko jedno)
     */
    private MenuItem undoItem;

    /**
     * Konstruktor obiektu tworzony podczas otwarcia pliku.
     *
     * @param file      otwarty plik
     * @param imageView z otwartym obrazem
     * @param undoItem  opcja menu "cofnij"
     */
    public FileData(File file, ImageView imageView, MenuItem undoItem) {
        this.file = file;
        this.imageView = imageView;
        this.undoItem = undoItem;
        undoItem.setDisable(true);
    }

    /**
     * Konstruktor tworzący obiekt tylko na potrzeby wygenerowania histogramu.
     *
     * @param imageView z obrazem
     */
    public FileData(ImageView imageView) {
        this.imageView = imageView;
    }

    /**
     * Ustawia obraz w <tt>ImageView</tt> - zostanie on wyświetlony w oknie głównym.
     *
     * @param image do ustawienia
     */
    public void setImage(Image image) {
        previousImage = imageView.getImage();
        imageView.setImage(image);
        undoItem.setDisable(false);
    }

    /**
     * Przywraca poprzedni stan obrazu (cofa operację).
     */
    public void undo() {
        imageView.setImage(previousImage);
        previousImage = null;
        undoItem.setDisable(true);
    }

}
