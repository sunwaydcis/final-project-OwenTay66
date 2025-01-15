package model

import scalafx.scene.image.Image

abstract class Card(var value: String, val isJoker: Boolean = false) {
  private var isFlipped = false

  def flip(): Unit =
    isFlipped = !isFlipped

  def isFlippedState: Boolean = isFlipped

  def image: Image = {
    val cardImagePath = if (isFlippedState) s"/cards/$value.png" else "/images/back.jpg"
    val resourceStream = getClass.getResourceAsStream(cardImagePath)
    if (resourceStream == null) throw new RuntimeException(s"Image not found at path: $cardImagePath")
    new Image(resourceStream)
  }

  def play(): Unit
}
