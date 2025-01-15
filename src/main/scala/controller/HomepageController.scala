package controller

import scalafx.scene.control.Button
import scalafx.scene.Scene
import scalafx.stage.{Modality, Stage, StageStyle}
import javafx.fxml.{FXML, FXMLLoader}
import javafx.event.ActionEvent
import scalafx.scene.layout.VBox
import javafx.scene.Parent
import scalafx.application.Platform
import scalafx.Includes._

class HomepageController {


  @FXML private var startButton: javafx.scene.control.Button = _
  @FXML private var trainingButton: javafx.scene.control.Button = _

  @FXML
  def onStartGame(): Unit = {
    println("Start Game button clicked")
    Platform.runLater {
      try {
        val loader = new FXMLLoader(getClass.getResource("/view/RootLayout.fxml"))
        val root = loader.load().asInstanceOf[Parent] // Load FXML as JavaFX Parent
        val stage = startButton.getScene.getWindow.asInstanceOf[javafx.stage.Stage] // JavaFX Stage
        stage.setScene(new Scene(new VBoxWrapper(root))) // Convert JavaFX Parent to ScalaFX Scene
        stage.show()
      } catch {
        case e: Exception =>
          println(s"Error loading RootLayout.fxml: ${e.getMessage}")
          e.printStackTrace()
      }
    }
  }

  @FXML
  def onInfo(): Unit = {
    println("Info button clicked")
    Platform.runLater {
      try {
        val loader = new FXMLLoader(getClass.getResource("/view/GameInfo.fxml"))
        val root = loader.load().asInstanceOf[Parent]
        val stage = new Stage() {
          initModality(Modality.ApplicationModal)
          initStyle(StageStyle.Utility)
          scene = new Scene(new VBoxWrapper(root))
        }
        stage.showAndWait()
      } catch {
        case e: Exception =>
          println(s"Error loading GameInfo.fxml: ${e.getMessage}")
          e.printStackTrace()
      }
    }
  }

  @FXML
  def startTraining(event: ActionEvent): Unit = {
    println("Training button clicked")
    Platform.runLater {
      try {
        val loader = new FXMLLoader(getClass.getResource("/view/Training.fxml"))
        val root = loader.load().asInstanceOf[Parent]
        val stage = trainingButton.getScene.getWindow.asInstanceOf[javafx.stage.Stage]
        stage.setScene(new Scene(new VBoxWrapper(root)))
        stage.show()
      } catch {
        case e: Exception =>
          println(s"Error loading Training.fxml: ${e.getMessage}")
          e.printStackTrace()
      }
    }
  }

  @FXML
  def onExit(event: ActionEvent): Unit = {
    println("Exit button clicked")
    val stage = startButton.getScene.getWindow.asInstanceOf[javafx.stage.Stage]
    stage.close()
  }


  class VBoxWrapper(parent: Parent) extends VBox {
    children.add(parent)
  }
}
