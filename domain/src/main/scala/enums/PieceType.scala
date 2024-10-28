package domain

enum PieceType {
  case Start, +, S, T
}

object PieceType {
    implicit def apply(value: String): Option[PieceType] =
      PieceType.values.find(pieceType => pieceType.toString().toLowerCase() == value.toLowerCase())
}