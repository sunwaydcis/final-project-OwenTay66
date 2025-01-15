package model

import scalafx.scene.image.Image

class StandardCard(value: String, imagePath: String) extends Card(value) {

  override def play(): Unit =
    println(s"Playing card: $value")

  override def image: Image = {
    val cardImagePath = if (isFlippedState) imagePath else "/images/back.jpg"
    val resourceStream = getClass.getResourceAsStream(cardImagePath)
    if (resourceStream == null) throw new RuntimeException(s"Image not found at path: $cardImagePath")
    new Image(resourceStream)
  }
}
