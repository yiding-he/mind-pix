package com.hyd.mindpix;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.net.URL;

public class MindPixApplication extends Application {

  public static final SimpleStringProperty CURRENT_IMAGE = new SimpleStringProperty();

  public static final SimpleStringProperty CURRENT_FOLDER = new SimpleStringProperty();

  public static final String TITLE_PREFIX = "Mind Pix - 图库浏览整理工具";

  @Override
  public void start(Stage stage) throws Exception {
    URL mainFxml = MindPixApplication.class.getResource("/fxml/main.fxml");
    Assert.notNull(mainFxml, () -> "main.fxml not found");

    var fxmlLoader = new FXMLLoader();
    fxmlLoader.setControllerFactory(MindPixMain.applicationContext::getBean);
    fxmlLoader.setLocation(mainFxml);

    stage.setTitle(TITLE_PREFIX);
    CURRENT_FOLDER.addListener((_, _, folder) -> {
      Platform.runLater(() -> {
        if (StringUtils.hasText(folder)) {
          stage.setTitle(TITLE_PREFIX + " [" + folder + "]");
        } else {
          stage.setTitle(TITLE_PREFIX);
        }
      });
    });

    stage.setScene(new Scene(fxmlLoader.load(), 1200, 700));
    stage.show();
  }
}
