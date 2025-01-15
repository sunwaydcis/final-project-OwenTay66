package controller

import scalafx.Includes._
import javafx.fxml.FXML
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.GridPane
import javafx.scene.control.{Button, ProgressBar}
import javafx.scene.text.Text
import model.{Card, GameLogic, AIPlayer}
import scala.collection.mutable.ArrayBuffer
import javafx.stage.Stage
import javafx.application.Platform
import javafx.animation.{KeyFrame, Timeline}
import javafx.util.Duration
import javafx.scene.Scene
import javafx.fxml.FXMLLoader
import model.JokerCard

class TrainingController extends BaseGameController :

  @FXML private var trainingGrid: GridPane = _
  @FXML private var backButton: Button = _
  @FXML private var winnerMessage: Text = _
  @FXML private var player1HealthBar: ProgressBar = _
  @FXML private var player2HealthBar: ProgressBar = _
  @FXML private var player1TimeText: Text = _
  @FXML private var player2TimeText: Text = _
  @FXML private var player1Score: Text = _
  @FXML private var player2Score: Text = _

  private val cardWidth = 60.0
  private val cardHeight = 90.0
  private var cards: Array[Card] = Array.empty
  private var flippedCards = ArrayBuffer[(Card, Button)]()
  private val matchedCards = ArrayBuffer[Card]()
  private val aiPlayer = new AIPlayer()
  private var playerTurn = true
  private var currentPlayer = 1


  private var player1Points: Int = 0
  private var player2Points: Int = 0

  private var player1Timer: Timeline = _
  private var player2Timer: Timeline = _
  private var player1TimeLeft = 80
  private var player2TimeLeft = 80
  private var gameEnded = false

  @FXML
  def initialize(): Unit =
    loadTrainingImages()
    backButton.setOnAction(_ => backToHome())
    player1Points = 0
    player2Points = 0
    player1Score.setText("Score: 0")
    player2Score.setText("Score: 0")
    player1HealthBar.setProgress(1.0)
    player2HealthBar.setProgress(1.0)

    println(s"It's Player $currentPlayer's turn.")

    startTimers()

  private def loadTrainingImages(): Unit =
    trainingGrid.getChildren.clear()

    cards = GameLogic.initializeCards(28)

    val cols = 7
    for i <- cards.indices do
      val cardButton = new Button()
      val cardBackImage = new ImageView(new Image(getClass.getResourceAsStream("/images/back.jpg")))
      cardBackImage.setFitWidth(cardWidth)
      cardBackImage.setFitHeight(cardHeight)
      cardButton.graphic = cardBackImage
      cardButton.setStyle("-fx-background-color: transparent;")

      cardButton.setOnAction(_ => flipCard(i, cardButton))
      trainingGrid.add(cardButton, i % cols, i / cols)

  private def areCardsMatching(flippedCards: ArrayBuffer[(Card, Button)]): Boolean =
    if flippedCards.size == 2 then
      val Array((card1, _), (card2, _)) = flippedCards.toArray
      card1.value.split("(?<=\\D)(?=\\d)").head == card2.value.split("(?<=\\D)(?=\\d)").head
    else false

  private var isPlayerSelecting = true

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


        if flippedCards.forall(_._1.isInstanceOf[JokerCard]) then
          println("Matched Joker cards! Awarding 2 points.")
          if currentPlayer == 1 then
            player1Points += 2
            player1Score.setText(s"Score: $player1Points")
          else
            player2Points += 2
            player2Score.setText(s"Score: $player2Points")
        else

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

                if !c.isInstanceOf[JokerCard] then
                  c.flip()
                  val backImage = new ImageView(new Image(getClass.getResourceAsStream("/images/back.jpg")))
                  backImage.setFitWidth(cardWidth)
                  backImage.setFitHeight(cardHeight)
                  btn.graphic = backImage
              }
              flippedCards.clear()
              switchPlayer()
            )
        new Thread(flipBackTask).start()

  private var aiMemory: Map[Card, Int] = Map()

  private def rememberCard(card: Card, index: Int): Unit =
    aiMemory += (card -> index)

  private def aiTurn(): Unit =
    println("AI Player's Turn")


    println(s"flippedCards size: ${flippedCards.size}, matchedCards size: ${matchedCards.size}")


    val unflippedCards = cards.zipWithIndex.filter:
      case (card, _) => !matchedCards.contains(card)
    .map(_._1)

    println(s"Unflipped cards: ${unflippedCards.size}")

    if unflippedCards.size >= 2 then
      val firstCard = unflippedCards(scala.util.Random.nextInt(unflippedCards.size))
      val secondCard = unflippedCards.filterNot(_ == firstCard)(scala.util.Random.nextInt(unflippedCards.size - 1))


      val firstIndex = cards.indexOf(firstCard)
      val secondIndex = cards.indexOf(secondCard)


      val firstCardButton = trainingGrid.getChildren.get(firstIndex).asInstanceOf[Button]
      flipCard(firstIndex, firstCardButton)


      new Thread(() =>
        Thread.sleep(1000)
        Platform.runLater(() =>
          val secondCardButton = trainingGrid.getChildren.get(secondIndex).asInstanceOf[Button]
          flipCard(secondIndex, secondCardButton)
        )
      ).start()


  private def switchPlayer(): Unit =
    if !gameEnded then
      if currentPlayer == 1 then
        currentPlayer = 2
        println("It's Player 2's turn (AI).")
        stopTimer(player1Timer)
        startTimer(player2Timer)
        Platform.runLater(() => aiTurn())
      else
        currentPlayer = 1
        println("It's Player 1's turn (Human).")
        stopTimer(player2Timer)
        startTimer(player1Timer)


  private def startTimers(): Unit =
    player1Timer = createTimer(player1HealthBar, player1TimeLeft, 1)
    player2Timer = createTimer(player2HealthBar, player2TimeLeft, 2)

    startTimer(player1Timer)

  private def createTimer(progressBar: ProgressBar, timeLeft: Int, playerId: Int): Timeline =
    val timer = new Timeline(new KeyFrame(Duration.seconds(1), _ =>
      if playerId == 1 then
        player1TimeLeft -= 1
        progressBar.setProgress(player1TimeLeft / 80.0)
        player1TimeText.setText(player1TimeLeft.toString)
        if player1TimeLeft <= 0 then
          stopTimers()
          declareWinner(2)
      else
        player2TimeLeft -= 1
        progressBar.setProgress(player2TimeLeft / 80.0)
        player2TimeText.setText(player2TimeLeft.toString)
        if player2TimeLeft <= 0 then
          stopTimers()
          declareWinner(1)
    ))
    timer.setCycleCount(-1)
    timer

  private def stopTimer(timer: Timeline): Unit =
    if timer != null then timer.stop()

  private def stopTimers(): Unit =
    stopTimer(player1Timer)
    stopTimer(player2Timer)

  private def startTimer(timer: Timeline): Unit =
    if timer != null then timer.play()

  @FXML
  private def backToHome(): Unit =
    val stage = backButton.getScene.getWindow.asInstanceOf[Stage]

    stage.setScene(new Scene(FXMLLoader.load(getClass.getResource("/view/homepage.fxml"))))

  private def checkLevelCompletion(): Unit =
    if cards.forall(c => matchedCards.contains(c)) then
      println("All cards are matched!")
      stopTimers()
      declareWinnerByScore()
      gameEnded = true

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
    if player1Points > player2Points then
      println(s"Player 1 wins with $player1Points points!")
    else if player2Points > player1Points then
      println(s"Player 2 wins with $player2Points points!")
    else
      println("It's a tie!")

  def onBackToHomepage(): Unit =
    loadFXMLAndSetScene("/view/homepage.fxml", backButton.getScene.getWindow.asInstanceOf[Stage])


