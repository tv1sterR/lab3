package model;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageLoader {

    // Загрузка изображения из файла
    public Image loadFromFile(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try (InputStream is = new FileInputStream(file)) {
            return new Image(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Загрузка из ресурсов (для тестирования)
    public Image loadFromResource(String path) {
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) return null;
        return new Image(is);
    }
}
