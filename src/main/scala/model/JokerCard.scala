package model

import scalafx.scene.image.Image

class JokerCard(value: String, imagePath: String) extends Card(value, isJoker = true) {

  override def play(): Unit =
    println(s"Playing the Joker card: $value")

  override def flip(): Unit = {
    super.flip()
    println(s"Flipping the Joker card: $value")
  }

  override def image: Image = {
    val cardImagePath = if (isFlippedState) imagePath else "/images/jokercard.png"
    val resourceStream = getClass.getResourceAsStream(cardImagePath)
    if (resourceStream == null) throw new RuntimeException(s"Image not found at path: $cardImagePath")
    new Image(resourceStream)
  }
}
