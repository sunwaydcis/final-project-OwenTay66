package controller

import scalafx.Includes._
import javafx.fxml.FXML
import javafx.scene.layout.GridPane
import javafx.scene.control.{Button, ProgressBar}
import javafx.scene.image.{Image, ImageView}
import model.{Card, GameLogic}
import scala.collection.mutable.ArrayBuffer
import javafx.stage.Stage
import javafx.scene.Scene
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.application.Platform
import javafx.animation.{KeyFrame, Timeline}
import javafx.util.Duration

class GameController {
  @FXML private var gameGrid: GridPane = _
  @FXML private var backButton: Button = _
  @FXML private var player1HealthBar: ProgressBar = _
  @FXML private var player2HealthBar: ProgressBar = _

  private var flippedCards = ArrayBuffer[(Card, Button)]()
  private val matchedCards = ArrayBuffer[Card]()
  private var cards: Array[Card] = Array.empty

  private val cardWidth = 60.0
  private val cardHeight = 90.0

  private var currentPlayer = 1
  private var player1TimeLeft = 45
  private var player2TimeLeft = 45

  private var player1Timer: Timeline = _
  private var player2Timer: Timeline = _

  @FXML
  def initialize(): Unit = {
    loadAllCards()
    backButton.setOnAction(_ => onBackToHomepage())

    player1HealthBar.setProgress(1.0)
    player2HealthBar.setProgress(1.0)

    startTimers()
  }

  private def loadAllCards(): Unit = {
    gameGrid.getChildren.clear()

    cards = GameLogic.initializeCards(28)

    val cols = 7
    for (i <- cards.indices) {
      val cardButton = new Button()
      val cardBackImage = new ImageView(new Image(getClass.getResourceAsStream("/images/back.jpg")))
      cardBackImage.setFitWidth(cardWidth)
      cardBackImage.setFitHeight(cardHeight)
      cardButton.graphic = cardBackImage
      cardButton.setStyle("-fx-background-color: transparent;")

      cardButton.setOnAction(_ => flipCard(i, cardButton))
      gameGrid.add(cardButton, i % cols, i / cols)
    }
  }

  private def flipCard(index: Int, button: Button): Unit = {
    val card = cards(index)

    if (matchedCards.contains(card)) {
      println("This card is already matched. Choose another.")
      return
    }

    if (flippedCards.exists(_._1 == card)) {
      println("This card is already flipped. Choose another.")
      return
    }

    card.flip()
    val cardFrontImage = new ImageView(card.image)
    cardFrontImage.setFitWidth(cardWidth)
    cardFrontImage.setFitHeight(cardHeight)
    button.graphic = cardFrontImage

    flippedCards += ((card, button))

    if (flippedCards.size == 2) {
      val (firstCard, _) = flippedCards.head

      if (areCardsMatching(flippedCards)) {
        println("Match!")
        matchedCards ++= flippedCards.map(_._1)
        flippedCards.clear()
      } else {
        println("No match.")
        val flipBackTask = new Runnable {
          override def run(): Unit = {
            Thread.sleep(1000)
            Platform.runLater(() => {
              flippedCards.foreach { case (c, btn) =>
                c.flip()
                val backImage = new ImageView(new Image(getClass.getResourceAsStream("/images/back.jpg")))
                backImage.setFitWidth(cardWidth)
                backImage.setFitHeight(cardHeight)
                btn.graphic = backImage
              }
              flippedCards.clear()
            })
          }
        }
        new Thread(flipBackTask).start()
      }

      switchPlayer()
    }
  }

  private def areCardsMatching(flippedCards: ArrayBuffer[(Card, Button)]): Boolean = {
    if (flippedCards.size == 2) {
      val Array((card1, _), (card2, _)) = flippedCards.toArray
      card1.value.split("(?<=\\D)(?=\\d)").head == card2.value.split("(?<=\\D)(?=\\d)").head
    } else false
  }

  private def switchPlayer(): Unit = {
    if (currentPlayer == 1) {
      currentPlayer = 2
      stopTimer(player1Timer)
      startTimer(player2Timer)
    } else {
      currentPlayer = 1
      stopTimer(player2Timer)
      startTimer(player1Timer)
    }
  }

  private def startTimers(): Unit = {
    player1Timer = createTimer(player1HealthBar, player1TimeLeft, 1)
    player2Timer = createTimer(player2HealthBar, player2TimeLeft, 2)

    startTimer(player1Timer)
  }

  private def createTimer(progressBar: ProgressBar, timeLeft: Int, playerId: Int): Timeline = {
    val timer = new Timeline(new KeyFrame(Duration.seconds(1), _ => {
      if (playerId == 1) {
        player1TimeLeft -= 1
        progressBar.setProgress(player1TimeLeft / 45.0)
        if (player1TimeLeft <= 0) {
          stopTimer(player1Timer)
          declareWinner(2)
        }
      } else {
        player2TimeLeft -= 1
        progressBar.setProgress(player2TimeLeft / 45.0)
        if (player2TimeLeft <= 0) {
          stopTimer(player2Timer)
          declareWinner(1)
        }
      }
    }))
    timer.setCycleCount(-1)
    timer
  }

  private def declareWinner(winningPlayerId: Int): Unit = {
    val winner = if (winningPlayerId == 1) "Player 1" else "Player 2"
    println(s"$winner wins!")
    onBackToHomepage()
  }

  private def startTimer(timer: Timeline): Unit = {
    timer.play()
  }

  private def stopTimer(timer: Timeline): Unit = {
    timer.stop()
  }

  private def checkLevelCompletion(): Unit = {
    if (cards.forall(c => matchedCards.contains(c))) {
      println("Game complete! Congratulations!")
      onBackToHomepage()
    }
  }

  def onBackToHomepage(): Unit = {
    val loader = new FXMLLoader(getClass.getResource("/view/homepage.fxml"))
    val root: Parent = loader.load()

    val stage = backButton.getScene.getWindow.asInstanceOf[Stage]
    stage.scene = new Scene(root)
  }
}
