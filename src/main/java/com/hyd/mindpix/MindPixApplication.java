package com.hyd.mindpix;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.util.Assert;

import java.net.URL;

public class MindPixApplication extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    URL mainFxml = MindPixApplication.class.getResource("/fxml/main.fxml");
    Assert.notNull(mainFxml, () -> "main.fxml not found");

    var fxmlLoader = new FXMLLoader();
    fxmlLoader.setControllerFactory(MindPixMain.applicationContext::getBean);
    fxmlLoader.setLocation(mainFxml);

    stage.setScene(new Scene(fxmlLoader.load()));
    stage.show();
  }
}
