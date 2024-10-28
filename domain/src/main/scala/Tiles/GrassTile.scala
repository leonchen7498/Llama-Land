package domain;

import scala.util.Try
import scala.util.Failure
import scala.util.Success

final case class GrassTile(val height: Int, hasLlama: Boolean = false) extends Tile {

  override def getTileType(): TileType =
    TileType.Grass

  override def allowedToPlaceNewTile(height: Int): Boolean = 
    return this.height == height && hasLlama == false

  def placeLlama(): Try[Tile] =
    if (hasLlama) {
      return Failure(new IllegalArgumentException("There's already a llama here!"))
    }
    else {
      return Success(GrassTile(height, true))
    }
}
