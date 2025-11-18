package com.hyd.mindpix;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class MindPixMain {

  // 默认集合名称常量
  public static final String DEFAULT_COLLECTION_NAME = "[默认集合]";

  public static ApplicationContext applicationContext;

  public static void publish(Object event) {
    applicationContext.publishEvent(event);
  }

  public static void main(String[] args) {
    MindPixMain.applicationContext = SpringApplication.run(MindPixMain.class, args);
    Application.launch(MindPixApplication.class);
  }
}
