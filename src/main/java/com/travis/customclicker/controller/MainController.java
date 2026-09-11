package com.travis.customclicker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MainController {

    @FXML
    private Button startButton;

    @FXML
    private void handleStart() {
        System.out.println("Start button clicked");
    }
}
