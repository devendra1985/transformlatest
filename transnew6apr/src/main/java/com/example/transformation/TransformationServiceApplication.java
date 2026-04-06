package com.example.transformation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = {"org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration"})
public class TransformationServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(TransformationServiceApplication.class, args);
  }
}

