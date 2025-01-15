package model

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object GameLogic {

  var player1Score = 0
  var player2Score = 0
  var player1Time = 45.0
  var player2Time = 45.0

  def addPoints(player: Int, points: Int): Unit =
    if (player == 1) player1Score += points
    else if (player == 2) player2Score += points

    println(s"Player $player awarded $points points! Scores: Player 1 = $player1Score, Player 2 = $player2Score")

  def addTime(player: Int, seconds: Double): Unit =
    if (player == 1) player1Time += seconds
    else if (player == 2) player2Time += seconds

    println(s"Player $player's time increased by $seconds seconds!")

  def handleCardFlip(cards: List[Card], currentPlayer: Int): Unit =
    cards match {
      case List(joker1: JokerCard, joker2: JokerCard) =>
        println("Two Joker cards flipped together!")
        addPoints(currentPlayer, 2)
      case List(joker: JokerCard) =>
        println("Single Joker card flipped!")
        addTime(currentPlayer, 2)
      case _ =>
        println("No special match detected.")
    }

  def initializeCards(numCards: Int): Array[Card] = {
    require(numCards == 28, "The deck must contain exactly 28 cards.")

    val cardValues = Array(
      "8Clubs", "8Spades", "8Hearts", "8Diamonds",
      "9Clubs", "9Spades", "9Hearts", "9Diamonds",
      "10Clubs", "10Spades", "10Hearts", "10Diamonds",
      "JClubs", "JSpades", "JHearts", "JDiamonds",
      "QClubs", "QSpades", "QHearts", "QDiamonds",
      "KClubs", "KSpades", "KHearts", "KDiamonds",
      "AClubs", "ASpades", "AHearts", "ADiamonds"
    )

    val selectedValues = Random.shuffle(cardValues.toList).take(13)
    val pairedValues = selectedValues ++ selectedValues

    val jokers = Array(
      new JokerCard("Joker1", "/images/jokercard.png"),
      new JokerCard("Joker2", "/images/jokercard.png")
    )

    val allCards = pairedValues.map(value => new StandardCard(value, s"/cards/$value.png")) ++ jokers
    Random.shuffle(allCards).toArray
  }

  def checkMatch(flippedCards: ArrayBuffer[Card]): Boolean = {
    if (flippedCards.size == 2) {
      val Array(card1, card2) = flippedCards.toArray
      if (card1.value == card2.value) {
        println("Match found!")
        flippedCards.clear()
        true
      } else {
        println("No match. Flipping cards back.")
        card1.flip()
        card2.flip()
        flippedCards.clear()
        false
      }
    } else false
  }

  def resetGame(): Unit = {
    player1Score = 0
    player2Score = 0
    player1Time = 45.0
    player2Time = 45.0
    println("Game has been reset!")
  }
}
