package model

import scala.util.Random
import scala.collection.mutable.ArrayBuffer

class AIPlayer {

  def selectCard(cards: Array[Card], matchedCards: Array[Card], aiMemory: ArrayBuffer[Card]): Int = {

    val availableCards = cards.zipWithIndex.filterNot { case (card, _) => matchedCards.contains(card) }

    val rememberedPairs = aiMemory.groupBy(identity).collect {
      case (card, occurrences) if occurrences.size > 1 => card
    }

    val rememberedCardOption = rememberedPairs.flatMap { card =>
      availableCards.find(_._1 == card)
    }.headOption

    val (selectedCard, index) = rememberedCardOption.getOrElse {
      availableCards(Random.nextInt(availableCards.length))
    }
    index
  }
}
