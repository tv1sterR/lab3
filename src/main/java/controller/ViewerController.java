package controller;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import model.ImageCollection;
import model.Iterator;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ViewerController implements Initializable {

    @FXML private ImageView imageView;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnFirst;
    @FXML private Button btnLast;
    @FXML private Button btnAuto;
    @FXML private Label lblCounter;
    @FXML private Label lblInfo;
    @FXML private ComboBox<String> filterBox;
    @FXML private ComboBox<String> effectBox;

    private ImageCollection collection;
    private Iterator iterator;
    private boolean autoMode = false;
    private Timeline autoTimeline;

    private File currentDirectory;
    private int currentIndex = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        filterBox.getItems().addAll("Все", "JPEG", "PNG");
        filterBox.getSelectionModel().selectFirst();

        effectBox.getItems().addAll("Fade", "Slide", "Scale", "Rotate", "Combo");
        effectBox.getSelectionModel().select("Combo");

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
        if (selected.equals("Все")) return new String[]{".jpg", ".jpeg", ".png", ".gif", ".bmp"};
        if (selected.equals("JPEG")) return new String[]{".jpg", ".jpeg"};
        if (selected.equals("PNG")) return new String[]{".png"};
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
        if (size == 0) lblCounter.setText("0 из 0");
        else lblCounter.setText((currentIndex + 1) + " из " + size);
    }

    private void showFirstWithEffect() {
        if (collection.size() == 0) return;
        currentIndex = 0;
        Image img = iterator.next(currentIndex);
        playTransition(img, false);
        updateCounter();
        updateFileInfo();
    }

    private void showLastWithEffect() {
        if (collection.size() == 0) return;
        currentIndex = collection.size() - 1;
        Image img = iterator.next(currentIndex);
        playTransition(img, true);
        updateCounter();
        updateFileInfo();
    }

    private void showNextWithEffect() {
        if (collection.size() == 0) return;
        currentIndex = (currentIndex + 1) % collection.size();
        Image img = iterator.next(currentIndex);
        playTransition(img, true);
        updateCounter();
        updateFileInfo();
    }

    private void showPrevWithEffect() {
        if (collection.size() == 0) return;
        currentIndex = (currentIndex - 1 + collection.size()) % collection.size();
        Image img = iterator.preview(currentIndex);
        playTransition(img, false);
        updateCounter();
        updateFileInfo();
    }

    private void playTransition(Image newImage, boolean forward) {
        if (newImage == null) return;

        String effect = effectBox.getSelectionModel().getSelectedItem();
        double direction = forward ? 1 : -1;

        // Fade
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), imageView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), imageView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // Slide
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), imageView);
        slideOut.setFromX(0);
        slideOut.setToX(-80 * direction);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), imageView);
        slideIn.setFromX(80 * direction);
        slideIn.setToX(0);

        // Scale
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), imageView);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.8);
        scaleOut.setToY(0.8);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), imageView);
        scaleIn.setFromX(1.2);
        scaleIn.setFromY(1.2);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        // Rotate
        RotateTransition rotateOut = new RotateTransition(Duration.millis(200), imageView);
        rotateOut.setFromAngle(0);
        rotateOut.setToAngle(20 * direction);

        RotateTransition rotateIn = new RotateTransition(Duration.millis(200), imageView);
        rotateIn.setFromAngle(-20 * direction);
        rotateIn.setToAngle(0);

        // Выбор эффекта
        ParallelTransition out;
        ParallelTransition in;

        switch (effect) {
            case "Fade":
                out = new ParallelTransition(fadeOut);
                in = new ParallelTransition(fadeIn);
                break;

            case "Slide":
                out = new ParallelTransition(slideOut);
                in = new ParallelTransition(slideIn);
                break;

            case "Scale":
                out = new ParallelTransition(scaleOut);
                in = new ParallelTransition(scaleIn);
                break;

            case "Rotate":
                out = new ParallelTransition(rotateOut);
                in = new ParallelTransition(rotateIn);
                break;

            default: // Combo
                out = new ParallelTransition(fadeOut, slideOut, scaleOut, rotateOut);
                in = new ParallelTransition(fadeIn, slideIn, scaleIn, rotateIn);
                break;
        }

        // 🔥 ВАЖНО: смена изображения должна быть здесь
        out.setOnFinished(e -> {
            imageView.setImage(newImage);
            in.play();
        });

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

        autoTimeline = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> showNextWithEffect())
        );
        autoTimeline.setCycleCount(Animation.INDEFINITE);
        autoTimeline.play();
    }

    private void updateFileInfo() {
        if (collection.size() == 0) {
            lblInfo.setText("");
            return;
        }

        File file = collection.getFile(currentIndex);
        if (file == null) return;

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
        } catch (Exception ignored) {}

        lblInfo.setText(sb.toString());
    }
}
