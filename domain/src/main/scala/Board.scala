package domain;

import scala.util.Try
import scala.util.Success
import scala.util.Failure

final case class Board(tiles: List[List[Tile]] = List.fill(16)(List.fill(16)(EmptyTile())),
        inventory: List[(CropType, Int)] = List((CropType.Cocoa, 0), (CropType.Corn, 0), (CropType.Potato, 0)),
        hasPlacedPiece: Boolean = false, name: String):
    private[domain] def setStartingPiece(startingPiece: Piece): Board =
        placeTiles(startingPiece.pieceTiles, 6, 6)

    private def placeTiles(pieceTilesToPlace: List[PieceTile], x: Int, y: Int): Board =
        pieceTilesToPlace match
            case Nil => this
            case head :: next => 
                val finalX = x+head.x
                val finalY = y+head.y
                val tile: Tile = tiles.apply(finalY).apply(finalX).getNewTile(head.tileType)
                val newInventory = getCropFromTileAndAddToInventory(finalX, finalY)

                this.copy(tiles = tiles.updated(finalY, tiles.apply(finalY).updated(finalX, tile)),
                    inventory = newInventory).placeTiles(next, x, y)

    private def getCropFromTileAndAddToInventory(x: Int, y: Int): List[(CropType, Int)] =
        val tile = tiles.apply(y).apply(x);

        if (tile.isInstanceOf[CropTile]) {
            val cropType = tile.asInstanceOf[CropTile].cropType;
            val index = inventory.indexWhere(crop => crop._1 == cropType)
            val newInventoryCrop = (cropType, inventory.apply(index)._2 + 1)
            return inventory.updated(index, newInventoryCrop)
        }
        else {
            return inventory;
        }
    
    private[domain] def placePiece(piece: Piece, x: Int, y: Int): Try[Board] =
        if (hasPlacedPiece) {
            return Failure(new IllegalArgumentException)
        }

        if (tiles.apply(y).apply(x).isInstanceOf[EmptyTile]) {
            placeTileNextToOther(piece.pieceTiles, x, y)
        }
        else {
            placeTileOnTop(piece.pieceTiles, x, y)
        }

    private def placeTileNextToOther(tilesToPlace: List[PieceTile], x: Int, y: Int): Try[Board] =
        if (checkIfAllowedToPlaceNextToAnother(tilesToPlace, x, y)) {
            return Success(placeTiles(tilesToPlace, x, y).copy(hasPlacedPiece = true))
        }
        return Failure(new IllegalArgumentException);


    private def placeTileOnTop(tilesToPlace: List[PieceTile], x: Int, y: Int): Try[Board] =
        if (checkIfAllowedToPlace(tilesToPlace, x, y)) {
            return Success(placeTiles(tilesToPlace, x, y).copy(hasPlacedPiece = true))
        }
        return Failure(new IllegalArgumentException)
        

    private def checkIfAllowedToPlace(tilesToPlace: List[PieceTile], x: Int, y: Int): Boolean =
        val height = tiles.apply(y).apply(x).height
        checkIfInsideBounds(tilesToPlace, x, y) && checkIfTileIsValid(tilesToPlace, x, y, height)

    private def checkIfAllowedToPlaceNextToAnother(tilesToPlace: List[PieceTile], x: Int, y: Int): Boolean =
        checkIfAllowedToPlace(tilesToPlace, x, y) && nextToAnotherTile(tilesToPlace, x, y)
        
    private def checkIfInsideBounds(tilesToPlace: List[PieceTile], x: Int, y: Int): Boolean =
        tilesToPlace match
            case Nil => true
            case head :: next =>
                val checkY = y+head.y;
                val checkX = x+head.x;

                if (checkX < 0 || checkX >= tiles.length || checkY < 0 || checkY >= tiles.length) {
                    return false;
                }
                return checkIfInsideBounds(next, x, y)

    private def checkIfTileIsValid(tilesToPlace: List[PieceTile], x: Int, y: Int, height: Int): Boolean =
        tilesToPlace match
            case Nil => true
            case head :: next =>
                if (tiles.apply(y+head.y).apply(x+head.x).allowedToPlaceNewTile(height)) {
                    return checkIfTileIsValid(next, x, y, height)
                } else {
                    return false
                }

    private def nextToAnotherTile(tilesToPlace: List[PieceTile], x: Int, y: Int): Boolean =
        tilesToPlace match
            case Nil => false
            case head :: next =>
                val checkX = x + head.x
                val checkY = y + head.y

                if (checkY-1 >= 0 && !tiles.apply(checkY - 1).apply(checkX).isInstanceOf[EmptyTile]) {
                    return true
                }
                else if(checkX+1 < tiles.length && !tiles.apply(checkY).apply(checkX + 1).isInstanceOf[EmptyTile]){
                    return true
                }
                else if(checkY+1 < tiles.length && !tiles.apply(checkY + 1).apply(checkX).isInstanceOf[EmptyTile]){
                    return true
                }
                else if(checkX-1 >= 0 && !tiles.apply(checkY).apply(checkX - 1).isInstanceOf[EmptyTile]){
                    return true
                }
                else {
                    return nextToAnotherTile(next, x, y)
                }

    private[domain] def buyAndPlaceLlama(cropType: CropType, x: Int, y: Int): Try[Board] =
        val tryBuyingLlama = buyLlama(cropType)

        tryBuyingLlama match
            case Success(newInventory) => 
                val tryPlacingLlama = placeLlama(x, y)
                tryPlacingLlama match
                    case Success(newTile) =>
                        return Success(
                            this.copy(tiles = tiles.updated(y, tiles.apply(y).updated(x, newTile)), 
                            inventory = newInventory)
                            )
                    case Failure(exception) => 
                        return Failure(exception)
            case Failure(exception) =>
                return Failure(exception)

    private def buyLlama(cropType: CropType): Try[List[(CropType, Int)]] =
        val index = inventory.indexWhere(crop => crop._1 == cropType && crop._2 >= 4)
        if (index >= 0) {
            val newInventoryCrop = (cropType, inventory.apply(index)._2 - 4)
            return Success(inventory.updated(index, newInventoryCrop))
        }
        return Failure(new IllegalArgumentException("Not enough crops!"))
        

    private def placeLlama(x: Int, y: Int): Try[Tile] =
        val tile = tiles.apply(y).apply(x);
        if (tile.isInstanceOf[GrassTile]) {
            val grassTile = tile.asInstanceOf[GrassTile]
            return grassTile.placeLlama()
        }
        return Failure(new IllegalArgumentException("Not a grass tile!"));
