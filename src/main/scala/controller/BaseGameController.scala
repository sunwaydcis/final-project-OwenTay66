package controller

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.Stage
import scalafx.scene.control.Control
import javafx.fxml.FXMLLoader
import javafx.scene.Parent


trait BaseGameController {


  def loadFXMLAndSetScene(fxmlPath: String, stage: Stage): Unit = {
    try {
      val loader = new FXMLLoader(getClass.getResource(fxmlPath))
      val root = loader.load[Parent]()
      stage.scene = new Scene(root)
      stage.show()
    } catch {
      case e: Exception =>
        println(s"Error loading FXML file: $fxmlPath")
        e.printStackTrace()
    }
  }


  def getCurrentStage(control: Control): Stage = {
    val window = control.getScene.getWindow.asInstanceOf[javafx.stage.Window]
    new Stage(window.asInstanceOf[Stage])
  }
}
