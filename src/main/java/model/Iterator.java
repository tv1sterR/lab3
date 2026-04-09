package model;

import javafx.scene.image.Image;

public interface Iterator {
    Image next(int index);
    Image preview(int index);
}
