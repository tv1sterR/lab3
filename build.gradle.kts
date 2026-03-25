plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

javafx {
    version = "17.0.14"   // можно оставить 17.0.14, но 21 стабильнее
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    // EXIF metadata extractor
    implementation("com.drewnoakes:metadata-extractor:2.18.0")
}

application {
    mainClass.set("app.ImageViewerApp")
}
