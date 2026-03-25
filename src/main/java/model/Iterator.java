package model;

import javafx.scene.image.Image;

public interface Iterator {
    boolean hasNext();      // проверка на наличие следующего элемента
    Image next();           // следующий элемент
    Image preview();        // предыдущий элемент
    boolean hasPreview();   // проверка на наличие предыдущего элемента
}
