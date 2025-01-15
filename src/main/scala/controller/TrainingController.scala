
package controller

import scalafx.Includes._
import javafx.fxml.FXML
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.GridPane
import javafx.scene.control.{Button}
import javafx.scene.text.Text
import model.{Card, GameLogic}
import scala.collection.mutable.ArrayBuffer
import javafx.stage.Stage
import javafx.scene.{Scene, Parent}
import javafx.application.Platform
import javafx.animation.{KeyFrame, Timeline}


class TrainingController extends BaseGameController:

  @FXML private var trainingGrid: GridPane = _
  @FXML private var backButton: Button = _
  @FXML private var winnerMessage: Text = _

  private val cardWidth = 60.0
  private val cardHeight = 90.0
  private var cards: Array[Card] = Array.empty

  private var flippedCards = ArrayBuffer[(Card, Button)]()
  private val matchedCards = ArrayBuffer[Card]()

  @FXML
  def initialize(): Unit =
    loadTrainingImages()
    backButton.setOnAction(_ => backToHome())

  private def loadTrainingImages(): Unit =
    trainingGrid.getChildren.clear()


    cards = GameLogic.initializeCards(28)


    val cols =
      for (i <- cards.indices)
        val cardButton = new Button()
        val cardBackImage = new ImageView(new Image(getClass.getResourceAsStream("/images/back.jpg")))
        cardBackImage.setFitWidth(cardWidth)
        cardBackImage.setFitHeight(cardHeight)
        cardButton.graphic = cardBackImage
        cardButton.setStyle("-fx-background-color: transparent;")

        // Add event listener to flip cards
        cardButton.setOnAction(_ => flipCard(i, cardButton))
        trainingGrid.add(cardButton, i % cols, i / cols

  private def flipCard(index: Int, button: Button): Unit =
    val card = cards(index)


    if matchedCards.contains(card) then
      println("This card is already matched. Choose another.")
      return


    if flippedCards.exists(_._1 == card) then
      println("This card is already flipped. Choose another.")
      return

    card.flip()  // Call the flip method from the Card class
    val cardFrontImage = new ImageView(card.image)
    cardFrontImage.setFitWidth(cardWidth)
    cardFrontImage.setFitHeight(cardHeight)
    button.graphic = cardFrontImage

    flippedCards += ((card, button))

    if flippedCards.size == 2 then
      val (firstCard, _) = flippedCards.head

      if areCardsMatching(flippedCards) then
        println("Match!")
        matchedCards ++= flippedCards.map(_._1)
        flippedCards.clear()


        checkLevelCompletion()
      else
        println("No match."

        val flipBackTask = new Runnable:
          override def run(): Unit =
            Thread.sleep(1000)
            Platform.runLater(() =>
              flippedCards.foreach { case (c, btn) =>
                c.flip()
                val backImage = new ImageView(new Image(getClass.getResourceAsStream("/images/back.jpg")))
                backImage.setFitWidth(cardWidth)
                backImage.setFitHeight(cardHeight)
                btn.graphic = backImage
              }
              flippedCards.clear()
            )
        new Thread(flipBackTask).start()

  private def areCardsMatching(flippedCards: ArrayBuffer[(Card, Button)]): Boolean =
    if flippedCards.size == 2 then
      val Array((card1, _), (card2, _)) = flippedCards.toArray
      card1.value.split("(?<=\\D)(?=\\d)").head == card2.value.split("(?<=\\D)(?=\\d)").head
    else false

  private def checkLevelCompletion(): Unit =
    if cards.forall(c => matchedCards.contains(c)) then  // Check if all cards are matched
      println("All cards are matched!")
      winnerMessage.setText("You win! All cards matched!")
      winnerMessage.setVisible(true)  // Display winner message

  @FXML
  def backToHome(): Unit =

    loadFXMLAndSetScene("/view/homepage.fxml", backButton.getScene.getWindow.asInstanceOf[Stage])