package controller;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import model.ImageCollection;
import model.Iterator;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ViewerController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private ImageView imageView;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnFirst;
    @FXML private Button btnLast;
    @FXML private Button btnAuto;
    @FXML private Label lblCounter;
    @FXML private Label lblInfo;
    @FXML private ComboBox<String> filterBox;

    private ImageCollection collection;
    private Iterator iterator;
    private boolean autoMode = false;
    private javafx.animation.Timeline autoTimeline;

    private File currentDirectory;
    private int currentIndex = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        filterBox.getItems().addAll("Все", "JPEG", "PNG");
        filterBox.getSelectionModel().selectFirst();

        currentDirectory = new File("src/main/resources/images");

        loadCollection();

        btnNext.setOnAction(e -> showNextWithEffect());
        btnPrev.setOnAction(e -> showPrevWithEffect());
        btnFirst.setOnAction(e -> showFirstWithEffect());
        btnLast.setOnAction(e -> showLastWithEffect());
        btnAuto.setOnAction(e -> toggleAutoMode());

        filterBox.setOnAction(e -> {
            loadCollection();
            showFirstWithEffect();
        });
    }

    private String[] getExtensionsForFilter() {
        String selected = filterBox.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals("Все")) {
            return new String[]{".jpg", ".jpeg", ".png", ".gif", ".bmp"};
        } else if (selected.equals("JPEG")) {
            return new String[]{".jpg", ".jpeg"};
        } else if (selected.equals("PNG")) {
            return new String[]{".png"};
        }
        return new String[]{};
    }

    private void loadCollection() {
        collection = new ImageCollection(currentDirectory, getExtensionsForFilter());
        iterator = collection.getIterator();
        currentIndex = 0;
        updateCounter();
    }

    private void updateCounter() {
        int size = collection.size();
        if (size == 0) {
            lblCounter.setText("0 из 0");
        } else {
            lblCounter.setText((currentIndex + 1) + " из " + size);
        }
    }

    private void showFirstWithEffect() {
        if (collection.size() == 0) {
            showEmpty();
            return;
        }
        Image img = collection.first();
        currentIndex = 0;
        playTransition(img, false);
        updateCounter();
        updateFileInfo();
    }

    private void showLastWithEffect() {
        if (collection.size() == 0) {
            showEmpty();
            return;
        }
        Image img = collection.last();
        currentIndex = collection.size() - 1;
        playTransition(img, true);
        updateCounter();
        updateFileInfo();
    }

    private void showNextWithEffect() {
        if (collection.size() == 0) {
            showEmpty();
            return;
        }
        Image img = iterator.next();
        currentIndex = (currentIndex + 1) % collection.size();
        playTransition(img, true);
        updateCounter();
        updateFileInfo();
    }

    private void showPrevWithEffect() {
        if (collection.size() == 0) {
            showEmpty();
            return;
        }
        Image img = iterator.preview();
        currentIndex = (currentIndex - 1 + collection.size()) % collection.size();
        playTransition(img, false);
        updateCounter();
        updateFileInfo();
    }

    private void showEmpty() {
        imageView.setImage(null);
        lblInfo.setText("Нет изображений (папка пуста или фильтр ничего не нашёл)");
        lblCounter.setText("0 из 0");
    }

    private void playTransition(Image newImage, boolean forward) {
        if (newImage == null) return;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), imageView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), imageView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        double direction = forward ? 1 : -1;

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), imageView);
        slideOut.setFromX(0);
        slideOut.setToX(-50 * direction);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), imageView);
        slideIn.setFromX(50 * direction);
        slideIn.setToX(0);

        fadeOut.setOnFinished(e -> imageView.setImage(newImage));

        ParallelTransition out = new ParallelTransition(fadeOut, slideOut);
        ParallelTransition in = new ParallelTransition(fadeIn, slideIn);

        out.setOnFinished(e -> in.play());
        out.play();
    }

    private void toggleAutoMode() {
        if (!autoMode) {
            autoMode = true;
            btnAuto.setText("Стоп");
            startAuto();
        } else {
            autoMode = false;
            btnAuto.setText("Авто");
            if (autoTimeline != null) autoTimeline.stop();
        }
    }

    private void startAuto() {
        if (collection.size() == 0) return;

        autoTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.seconds(2), e -> showNextWithEffect())
        );
        autoTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        autoTimeline.play();
    }

    private void updateFileInfo() {
        if (collection.size() == 0) {
            lblInfo.setText("");
            return;
        }
        File file = collection.getFile(currentIndex);
        if (file == null) {
            lblInfo.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Имя: ").append(file.getName())
                .append(" | Размер: ").append(file.length() / 1024).append(" КБ");

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            for (Directory dir : metadata.getDirectories()) {
                for (Tag tag : dir.getTags()) {
                    if (tag.getTagName().equalsIgnoreCase("Date/Time Original")) {
                        sb.append(" | Дата съёмки: ").append(tag.getDescription());
                    }
                }
            }
        } catch (Exception e) {
            // EXIF может отсутствовать — это нормально
        }

        lblInfo.setText(sb.toString());
    }
}
