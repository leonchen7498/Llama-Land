package domain

import scala.util.Try
import scala.util.Failure
import scala.util.Success

final case class GameState(boards: List[Board], placeablePieces: List[Piece]):
    def placePiece(pieceType: PieceType, x: Int, y: Int): Try[GameState] =
        val piece = placeablePieces.filter(piece => piece.pieceType == pieceType).head;
        boards.head.placePiece(piece, x, y) match
            case Success(board) =>
                Success(this.copy(boards = boards.updated(0, board)))
            case Failure(exception) =>
                Failure(exception)

    def buyAndPlaceLlama(cropType: CropType, x: Int, y: Int): Try[GameState] = 
        boards.head.buyAndPlaceLlama(cropType, x, y) match
            case Success(board) => 
                Success(this.copy(boards = board :: boards.tail))
            case Failure(exception) =>
                Failure(exception)


    def replacePiece(newPiece: Piece): Try[GameState] = 
        val index = placeablePieces.indexWhere(piece => piece.pieceType == newPiece.pieceType)

        if (index < 0) {
            return Failure(new IllegalArgumentException)
        }

        return Success(this.copy(placeablePieces = placeablePieces.updated(index, newPiece)))

    def rotatePlaceablePieces(): GameState = 
        this.copy(placeablePieces = rotatePieces(placeablePieces))

    private def rotatePieces(pieces: List[Piece]): List[Piece] =
        pieces match
            case Nil => Nil
            case head :: next => 
                head.rotatePiece() :: rotatePieces(next)

    def flipPlaceablePieces(): GameState = 
        this.copy(placeablePieces = flipPieces(placeablePieces))

    private def flipPieces(pieces: List[Piece]): List[Piece] =
        pieces match
            case Nil => Nil
            case head :: next => 
                head.flipPiece() :: flipPieces(next)

    def nextBoard(): GameState = 
        val boardsChangedOrder = boards.tail :+ boards.head;
        val boardsNew = boardsChangedOrder.head.copy(hasPlacedPiece = false) :: boardsChangedOrder.tail;
        return GameState(boardsNew, placeablePieces)

object GameState {
    def apply(names: List[String], startingPieces: List[Piece], placeablePieces: List[Piece]): Try[GameState] = 
        if (names.length <= 1 || names.length > 4 || names.length != startingPieces.length) {
            return Failure(new IllegalArgumentException)
        }

        val boards = createBoards(startingPieces, names)
        return Success(GameState(boards, placeablePieces))

    private def createBoards(startingPieces: List[Piece], names: List[String]): List[Board] =
        startingPieces match
            case Nil => Nil
            case head :: next =>
                Board(name = names.head).setStartingPiece(head) :: createBoards(next, names.tail)
}