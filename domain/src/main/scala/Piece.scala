package domain

final case class Piece(pieceType: PieceType, pieceTiles: List[PieceTile]):
    def rotatePiece(): Piece =
        this.copy(pieceTiles = rotatePieceTiles(pieceTiles))

    private def rotatePieceTiles(tiles: List[PieceTile]): List[PieceTile] =
        tiles match
            case Nil => Nil
            case head :: next => 
                head.rotatePieceTile() :: rotatePieceTiles(next)

    def flipPiece(): Piece =
        this.copy(pieceTiles = flipPieceTiles(pieceTiles))

    private def flipPieceTiles(tiles: List[PieceTile]): List[PieceTile] =
        tiles match
            case Nil => Nil
            case head :: next => 
                head.flipPieceTile() :: flipPieceTiles(next)