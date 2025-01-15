import scalafx.application.JFXApp3
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent => JFXParent}
import scalafx.scene.Scene
import scalafx.Includes._
import scalafx.application.JFXApp3.PrimaryStage



object MainApp extends JFXApp3 {

  override def start(): Unit = {

    val loader = new FXMLLoader(getClass.getResource("/view/homepage.fxml"))
    val root: JFXParent = loader.load()


    stage = new PrimaryStage {
      title = "Memory Card Game"
      scene = new Scene(root)
    }
  }
}
