package models

import domain.Board
import play.api.libs.json.Writes
import play.api.libs.json.Json
import domain.Tile
import domain.Piece
import domain.PieceTile
import domain.TileType
import play.api.libs.json.JsObject
import domain.CropType
import domain.GrassTile
import domain.GameState

object BoardJsonObjectWriters{
    implicit val gameStateWrites: Writes[GameState] = new Writes[GameState] {
        def writes(gamestate: GameState) = Json.obj(
            "boards" -> gamestate.boards,
            "placeablePieces" -> gamestate.placeablePieces
        )
    }


    implicit val boardWrites: Writes[Board] = new Writes[Board] {
        def writes(board: Board) = Json.obj(
            "name" -> board.name,
            "tiles" -> board.tiles,
            "inventory" -> inventoryWrites(board.inventory),
            "hasPlacedPiece" -> board.hasPlacedPiece
        )

        def inventoryWrites(inventory: List[(CropType, Int)]): List[JsObject] = 
            inventory.map(inventoryCrop => 
                Json.obj(
                    "cropType" -> inventoryCrop._1.toString(),
                    "amount" -> inventoryCrop._2
                )
            )
    }

    implicit val tileWrites: Writes[Tile] = new Writes[Tile] {
        def writes(tile: Tile) = 
            if (tile.isInstanceOf[GrassTile]) {
                Json.obj(
                    "tileType" -> tile.getTileType().toString(),
                    "height" -> tile.height,
                    "hasLlama" -> tile.asInstanceOf[GrassTile].hasLlama
                )
            } 
            else {
                Json.obj(
                    "tileType" -> tile.getTileType().toString(),
                    "height" -> tile.height
                )
            }
    }

    implicit val pieceWrites: Writes[Piece] = new Writes[Piece] {
        def writes(piece: Piece) = Json.obj(
            "pieceType" -> piece.pieceType.toString(),
            "pieceTiles" -> create2dList(piece.pieceTiles)
        )

        def create2dList(pieceTiles: List[PieceTile]): List[List[JsObject]] =
            val jsonList = List.tabulate(3)(y => 
                List.tabulate(3)(x => Json.obj("tileType" -> TileType.None.toString())));
            
            addTiles(pieceTiles, jsonList)

        def addTiles(pieceTiles: List[PieceTile], resultList: List[List[JsObject]]): List[List[JsObject]] =
            pieceTiles match
                case Nil => resultList
                case head :: next => 
                    val finalX = 1 + head.x
                    val finalY = 1 + head.y
                    val json = Json.obj("tileType" -> head.tileType.toString())

                    val newList = resultList.updated(finalY, resultList.apply(finalY).updated(finalX, json))
                    addTiles(next, newList)
    }

    implicit val pieceTileWrites: Writes[PieceTile] = new Writes[PieceTile] {
        def writes(pieceTile: PieceTile) = Json.obj(
            "tileType" -> pieceTile.tileType.toString(),
            "x" -> pieceTile.x,
            "y" -> pieceTile.y
        )
    }
}

