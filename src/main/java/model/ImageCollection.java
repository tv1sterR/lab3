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
        if (this.files == null) this.files = new File[0];
    }

    @Override
    public Iterator getIterator() {
        return new SimpleIterator();
    }

    public File getFile(int index) {
        if (index < 0 || index >= files.length) return null;
        return files[index];
    }

    public int size() {
        return files.length;
    }

    private class SimpleIterator implements Iterator {

        @Override
        public Image next(int index) {
            if (files.length == 0) return null;
            return loader.loadFromFile(files[index]);
        }

        @Override
        public Image preview(int index) {
            if (files.length == 0) return null;
            return loader.loadFromFile(files[index]);
        }
    }
}
