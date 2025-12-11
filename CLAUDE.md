# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Mind Pix is a Java desktop image gallery/organization tool built with:
- **JavaFX 24** for the GUI
- **Spring Boot 3.5.3** for dependency injection and application context
- **Maven** as the build system
- Java 24 (source/target compatibility)

The application allows users to browse image folders, organize images into collections/tabs, and provides keyboard shortcuts for navigation (number keys move thumbnails to tabs, `~` returns to default collection).

## Architecture

### Entry Points
- `MindPixMain.java` – Spring Boot entry point with `@SpringBootApplication`. Initializes Spring context and launches JavaFX via `Application.launch()`.
- `MindPixApplication.java` – JavaFX `Application` subclass. Loads `main.fxml` using a Spring‑aware `FXMLLoader` (`setControllerFactory(MindPixMain.applicationContext::getBean)`).

### MVC & Event System
- **View**: FXML files (`src/main/resources/fxml/main.fxml`) define the UI layout (BorderPane, SplitPane, MenuBar, TabPane, etc.).
- **Controller**: `MainController.java` – a Spring `@Component` that handles UI events and business logic. It uses `@EventListener` methods to react to custom application events.
- **Model**: Java classes (`Thumbnail`, `ImageCollectionTab`, etc.) represent the data; JavaFX properties (`SimpleStringProperty`, `ObjectProperty`) enable reactive UI updates.
- **Event System**: The `Events.java` interface defines a set of record‑based event types (loading, navigation, active‑thumbnail changes, tab transfers). Events are published via `MindPixMain.publish()` and consumed by `@EventListener` methods.

### Custom JavaFX Components
Located in `src/main/java/com/hyd/mindpix/components/`:
- `ImagePreview` – displays the currently selected image with dynamic/fixed scaling modes.
- `Thumbnail` – represents a single image thumbnail.
- `ThumbnailList` – manages the collection of thumbnails.
- `ImageCollectionTab` – a custom Tab that holds a ThumbnailList.

### Key Utilities
- `ImageUtils.java` – image loading and scaling (supports WebP via `webp‑imageio`).
- `FilenameUtils.java` – file‑name sorting and manipulation.

## Development Commands

### Building
- `mvn clean compile` – compile the project.
- `mvn package -P spring‑boot‑fat‑jar` – build a Spring Boot fat JAR (recommended).
- `mvn package -P single‑jar` – build a single JAR using the Maven Shade plugin.

### Running
- After building with the `spring‑boot‑fat‑jar` profile:
  ```bash
  java -jar target/mind-pix-1.0-SNAPSHOT.jar
  ```
- A convenience script `run‑fat‑jar.cmd` is provided for Windows (requires `JAVA25_HOME` environment variable).

### Cleaning
- `mvn clean` – delete the `target` directory.

### Testing
No test suite is currently set up. The project lacks `src/test` directories and test configuration.

## Key File Reference

- **Main entry**: `src/main/java/com/hyd/mindpix/MindPixMain.java`
- **JavaFX application**: `src/main/java/com/hyd/mindpix/MindPixApplication.java`
- **Main controller**: `src/main/java/com/hyd/mindpix/controllers/MainController.java`
- **UI definition**: `src/main/resources/fxml/main.fxml`
- **Event definitions**: `src/main/java/com/hyd/mindpix/Events.java`
- **Build configuration**: `pom.xml`
- **Run script**: `run‑fat‑jar.cmd`

## Permissions & Tooling

The `.claude/settings.local.json` grants permission to:
- Use Bash (including `tree`, `xargs`, `find` patterns)
- Perform WebSearch
- Fetch documentation from `javadoc.webfx.dev` and `openjfx.io`

This enables Claude Code to look up JavaFX/OpenJFX APIs when needed.

## Notes
- The project uses Lombok – ensure annotation processing is enabled in your IDE.
- IntelliJ IDEA project files (`.idea/`) are present; the project can be opened directly in IntelliJ.
- Image scaling modes are controlled by `ImageDisplayMode` (dynamic/fixed) and `ScaleRatio` enums.