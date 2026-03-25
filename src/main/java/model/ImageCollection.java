package model;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FilenameFilter;

public class ImageCollection implements Aggregate {

    private File[] files;
    private final ImageLoader loader = new ImageLoader();

    public ImageCollection(File directory, String[] extensions) {
        if (directory == null || !directory.isDirectory()) {
            this.files = new File[0];
            return;
        }

        FilenameFilter filter = (dir, name) -> {
            if (extensions == null || extensions.length == 0) return true;
            String lower = name.toLowerCase();
            for (String ext : extensions) {
                if (lower.endsWith(ext.toLowerCase())) return true;
            }
            return false;
        };

        this.files = directory.listFiles(filter);
        if (this.files == null) {
            this.files = new File[0];
        }
    }

    @Override
    public Iterator getIterator() {
        return new ImageFileIterator();
    }

    // Получить файл по индексу
    public File getFile(int index) {
        if (index < 0 || index >= files.length) return null;
        return files[index];
    }

    public int size() {
        return files.length;
    }

    // Первый кадр
    public Image first() {
        if (files.length == 0) return null;
        return loader.loadFromFile(files[0]);
    }

    // Последний кадр
    public Image last() {
        if (files.length == 0) return null;
        return loader.loadFromFile(files[files.length - 1]);
    }

    // Внутренний итератор
    private class ImageFileIterator implements Iterator {
        private int currentIndex = -1; // начнём "до" первого

        @Override
        public boolean hasNext() {
            return files.length > 0;
        }

        @Override
        public Image next() {
            if (files.length == 0) return null;
            currentIndex = (currentIndex + 1) % files.length;
            return loader.loadFromFile(files[currentIndex]);
        }

        @Override
        public Image preview() {
            if (files.length == 0) return null;
            currentIndex = (currentIndex - 1 + files.length) % files.length;
            return loader.loadFromFile(files[currentIndex]);
        }

        @Override
        public boolean hasPreview() {
            return files.length > 0;
        }

        public void reset() {
            currentIndex = -1;
        }
    }
}
