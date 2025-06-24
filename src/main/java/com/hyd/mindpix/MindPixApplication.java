package com.hyd.mindpix;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.util.Assert;

import java.net.URL;

public class MindPixApplication extends Application {

  public static final SimpleStringProperty CURRENT_IMAGE = new SimpleStringProperty();

  @Override
  public void start(Stage stage) throws Exception {
    URL mainFxml = MindPixApplication.class.getResource("/fxml/main.fxml");
    Assert.notNull(mainFxml, () -> "main.fxml not found");

    var fxmlLoader = new FXMLLoader();
    fxmlLoader.setControllerFactory(MindPixMain.applicationContext::getBean);
    fxmlLoader.setLocation(mainFxml);

    stage.setTitle("Mind Pix - 图库浏览整理工具");
    stage.setScene(new Scene(fxmlLoader.load(), 1200, 800));
    stage.show();
  }
}
