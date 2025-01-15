package controller

import scalafx.Includes._
import javafx.fxml.FXML
import javafx.scene.layout.GridPane
import javafx.scene.control.{Button, ProgressBar}
import javafx.scene.image.{Image, ImageView}
import model.{Card, GameLogic, JokerCard}
import scala.collection.mutable.ArrayBuffer
import javafx.stage.Stage
import javafx.application.Platform
import javafx.animation.KeyFrame
import javafx.util.Duration
import javafx.scene.text.Text
import javafx.animation.Timeline

class GameController extends BaseGameController:

  @FXML private var gameGrid: GridPane = _
  @FXML private var backButton: Button = _
  @FXML private var player1HealthBar: ProgressBar = _
  @FXML private var player2HealthBar: ProgressBar = _
  @FXML private var player1TimeText: Text = _
  @FXML private var player2TimeText: Text = _
  @FXML private var player1Score: Text = _
  @FXML private var player2Score: Text = _
  @FXML private var winnerMessage: Text = _

  private var player1Points: Int = 0
  private var player2Points: Int = 0

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
  private var gameEnded = false

  @FXML
  def initialize(): Unit =
    loadAllCards()
    backButton.setOnAction(_ => onBackToHomepage())
    player1Points = 0
    player2Points = 0
    player1Score.setText("Score: 0")
    player2Score.setText("Score: 0")
    player1HealthBar.setProgress(1.0)
    player2HealthBar.setProgress(1.0)

    println(s"It's Player $currentPlayer's turn.")

    startTimers()

  private def loadAllCards(): Unit =
    gameGrid.getChildren.clear()

    cards = GameLogic.initializeCards(28)

    val cols = 7
    for (i <- cards.indices)
      val cardButton = new Button()
      val cardBackImage = new ImageView(new Image(getClass.getResourceAsStream("/images/back.jpg")))
      cardBackImage.setFitWidth(cardWidth)
      cardBackImage.setFitHeight(cardHeight)
      cardButton.graphic = cardBackImage
      cardButton.setStyle("-fx-background-color: transparent;")
      cardButton.setOnAction(_ => flipCard(i, cardButton))
      gameGrid.add(cardButton, i % cols, i / cols)

  private def flipCard(index: Int, button: Button): Unit =
    val card = cards(index)

    if matchedCards.contains(card) then
      println("This card is already matched. Choose another.")
      return

    if flippedCards.exists(_._1 == card) then
      println("This card is already flipped. Choose another.")
      return

    card.flip()
    val cardFrontImage = new ImageView(card.image)
    cardFrontImage.setFitWidth(cardWidth)
    cardFrontImage.setFitHeight(cardHeight)
    button.graphic = cardFrontImage

    flippedCards += ((card, button))


    if card.isInstanceOf[JokerCard] then
      println("Flipped a Joker card! Awarding 1 point.")
      if currentPlayer == 1 then
        player1Points += 1
        player1Score.setText(s"Score: $player1Points")
      else
        player2Points += 1
        player2Score.setText(s"Score: $player2Points")

    if flippedCards.size == 2 then
      val (firstCard, _) = flippedCards.head

      if areCardsMatching(flippedCards) then
        println("Match!")
        matchedCards ++= flippedCards.map(_._1)

        // Check if both matched cards are Joker cards
        if flippedCards.forall(_._1.isInstanceOf[JokerCard]) then
          println("Matched Joker cards! Awarding 2 points.")
          if currentPlayer == 1 then
            player1Points += 2
            player1Score.setText(s"Score: $player1Points")
          else
            player2Points += 2
            player2Score.setText(s"Score: $player2Points")
        else
          // Award 1 point for a normal match
          if currentPlayer == 1 then
            player1Points += 1
            player1Score.setText(s"Score: $player1Points")
          else
            player2Points += 1
            player2Score.setText(s"Score: $player2Points")

        flippedCards.clear()
        checkLevelCompletion()
      else
        println("No match.")
        val flipBackTask = new Runnable:
          override def run(): Unit =
            Thread.sleep(1000)
            Platform.runLater(() =>
              flippedCards.foreach { case (c, btn) =>
                // Flip back only non-Joker cards
                if !c.isInstanceOf[JokerCard] then
                  c.flip()
                  val backImage = new ImageView(new Image(getClass.getResourceAsStream("/images/back.jpg")))
                  backImage.setFitWidth(cardWidth)
                  backImage.setFitHeight(cardHeight)
                  btn.graphic = backImage
              }
              flippedCards.clear()
            )
        new Thread(flipBackTask).start()

      switchPlayer()


  private def areCardsMatching(flippedCards: ArrayBuffer[(Card, Button)]): Boolean =
    if flippedCards.size == 2 then
      val Array((card1, _), (card2, _)) = flippedCards.toArray
      card1.value.split("(?<=\\D)(?=\\d)").head == card2.value.split("(?<=\\D)(?=\\d)").head
    else false

  private def switchPlayer(): Unit =
    if !gameEnded then
      if currentPlayer == 1 then
        currentPlayer = 2
        println("It's Player 2's turn.")
        stopTimers()
        startTimer(player2Timer)
      else
        currentPlayer = 1
        println("It's Player 1's turn.")
        stopTimers()
        startTimer(player1Timer)

  private def startTimers(): Unit =
    player1Timer = createTimer(player1HealthBar, player1TimeLeft, 1)
    player2Timer = createTimer(player2HealthBar, player2TimeLeft, 2)

    startTimer(player1Timer)

  private def createTimer(progressBar: ProgressBar, timeLeft: Int, playerId: Int): Timeline =
    val timer = new Timeline(new KeyFrame(Duration.seconds(1), _ =>
      if playerId == 1 then
        player1TimeLeft -= 1
        progressBar.setProgress(player1TimeLeft / 45.0)
        player1TimeText.setText(player1TimeLeft.toString)
        if player1TimeLeft <= 0 then
          stopTimers()
          declareWinner(2)
      else
        player2TimeLeft -= 1
        progressBar.setProgress(player2TimeLeft / 45.0)
        player2TimeText.setText(player2TimeLeft.toString)
        if player2TimeLeft <= 0 then
          stopTimers()
          declareWinner(1)
    ))
    timer.setCycleCount(-1)
    timer

  private def declareWinner(winningPlayerId: Int): Unit =
    val winner = if winningPlayerId == 1 then "Player 1" else "Player 2"
    println(s"$winner wins!")

    Platform.runLater(() =>
      winnerMessage.setText(s"$winner Wins!")
      winnerMessage.setVisible(true)
    )

    new Thread(new Runnable:
      override def run(): Unit =
        Thread.sleep(2000)
        Platform.runLater(() => onBackToHomepage())
    ).start()

    gameEnded = true

  private def declareWinnerByScore(): Unit =
    val winnerMessageText: String =
      if player1Points > player2Points then
        s"Player 1 wins with $player1Points points!"
      else if player2Points > player1Points then
        s"Player 2 wins with $player2Points points!"
      else
        val winner = if player1HealthBar.getProgress > player2HealthBar.getProgress then
          "Player 1"
        else if player2HealthBar.getProgress > player1HealthBar.getProgress then
          "Player 2"
        else
          val winnerByTime = if player1TimeLeft > player2TimeLeft then
            "Player 1"
          else if player2TimeLeft > player1TimeLeft then
            "Player 2"
          else
            "It's a tie!"
          winnerByTime
        winner

    println(winnerMessageText)

    Platform.runLater(() =>
      winnerMessage.setText(winnerMessageText)
      winnerMessage.setVisible(true)
    )

    new Thread(new Runnable:
      override def run(): Unit =
        Thread.sleep(2000)
        Platform.runLater(() => onBackToHomepage())
    ).start()

    gameEnded = true

  private def startTimer(timer: Timeline): Unit =
    timer.play()

  private def stopTimers(): Unit =
    if player1Timer != null then player1Timer.stop()
    if player2Timer != null then player2Timer.stop()

  private def checkLevelCompletion(): Unit =
    if cards.forall(c => matchedCards.contains(c)) then
      println("All cards are matched!")
      stopTimers()
      declareWinnerByScore()
      gameEnded = true

  private def checkWinnerByScore(): Unit =
    if player1Points > player2Points then
      println(s"Player 1 wins with $player1Points points!")
    else if player2Points > player1Points then
      println(s"Player 2 wins with $player2Points points!")
    else
      println("It's a tie!")

    onBackToHomepage()

  def onBackToHomepage(): Unit =
    loadFXMLAndSetScene("/view/homepage.fxml", backButton.getScene.getWindow.asInstanceOf[Stage])


