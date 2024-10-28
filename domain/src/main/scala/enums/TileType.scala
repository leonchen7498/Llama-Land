package domain;

enum TileType {
  case Grass, Potato, Cocoa, Corn, None
}

object TileType {
    implicit def apply(value: String): Option[TileType] =
      TileType.values.find(tileType => tileType.toString().toLowerCase() == value.toLowerCase())
}