package controller

import javafx.fxml.FXML
import javafx.scene.control.Label

class GameInfoController:

  @FXML private var infoLabel: Label = _

  def initialize(): Unit =
    infoLabel.setText("Here is some more detailed information about the game...")
    infoLabel.setStyle("-fx-font-size: 24px;")
