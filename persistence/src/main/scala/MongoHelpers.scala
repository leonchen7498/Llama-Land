package persistence

import domain.Board
import domain.Tile
import domain.TileType
import org.mongodb.scala.bson.BsonDocument
import org.bson.conversions.Bson
import domain.GrassTile
import org.mongodb.scala.bson.BsonArray
import scala.jdk.CollectionConverters._
import domain.EmptyTile
import domain.CropTile
import domain.Piece
import domain.PieceType
import domain.PieceTile
import domain.CropType
import org.mongodb.scala.bson.collection.immutable.Document
import domain.GameState

object MongoHelpers {
    def gamestateToBson(key: String, gamestate: GameState): BsonDocument =
        BsonDocument(
            "key" -> key,
            "boards" -> boardsToBson(gamestate.boards),
            "placeablePieces" -> BsonArray(gamestate.placeablePieces.map(pieceToBson))
        )

    def boardsToBson(boards: List[Board]): BsonArray =
        BsonArray(boards.map(boardToBson))

    def boardToBson(board: Board): BsonDocument =
        BsonDocument(
            "tiles" -> tileListToBson(board.tiles),
            "inventory" -> inventoryToBson(board.inventory),
            "hasPlacedPiece" -> board.hasPlacedPiece,
            "name" -> board.name
        )

    def tileListToBson(tiles: List[List[Tile]]): BsonArray =
        BsonArray(tiles.map(row => BsonArray(row.map(tile => tileBson(tile)))))

    def tileBson(tile: Tile): BsonDocument = 
        if (tile.isInstanceOf[GrassTile]){
            BsonDocument(
            "tileType" -> tile.getTileType().toString(),
            "height" -> tile.height,
            "hasLlama" -> tile.asInstanceOf[GrassTile].hasLlama
            )
        }
        else{
            BsonDocument(
            "tileType" ->  tile.getTileType().toString(),
            "height" -> tile.height
            )
        }

    def inventoryToBson(inventory: List[(CropType, Int)]): BsonArray =
        BsonArray(inventory.map(inventoryCrop =>
            BsonDocument(
                    "cropType" -> inventoryCrop._1.toString(),
                    "amount" -> inventoryCrop._2
        )))

    def pieceToBson(piece: Piece): BsonDocument =
        BsonDocument(
            "pieceType" -> piece.pieceType.toString(),
            "pieceTiles" -> pieceTileListToBson(piece.pieceTiles)
        )
    
    def pieceTileListToBson(pieceTiles: List[PieceTile]): BsonArray =
        BsonArray(pieceTiles.map(pieceTileToBson))

    def pieceTileToBson(pieceTile: PieceTile): BsonDocument =
        BsonDocument(
            "tileType" -> pieceTile.tileType.toString(),
            "x" -> pieceTile.x,
            "y" -> pieceTile.y
        )

    def documentToPieceList(documents: List[Document]): List[Piece] =
        documents.map(document =>
            val bson = document.toBsonDocument()
            bsonToPiece(bson)
        )

    def bsonToGamestate(bson: BsonDocument): GameState =
        GameState(
            bsonToBoardList(bson.getArray("boards")),
            bsonToPieceList(bson.getArray("placeablePieces"))
        )

    def bsonToBoardList(bsonArray: BsonArray): List[Board] =
        bsonArray.asScala.map { board => bsonToBoard(board.asDocument()) }.toList

    def bsonToBoard(bson: BsonDocument): Board =
        Board(bsonToTileList(bson.getArray("tiles")), 
        bsonToInventory(bson.getArray("inventory")), 
        bson.getBoolean("hasPlacedPiece").getValue(),
        bson.getString("name").getValue())

    def bsonToTileList(bsonArray: BsonArray): List[List[Tile]] = 
        bsonArray.asScala.map { row =>
            row.asArray().asScala.map(tileBson => bsonToTile(tileBson.asDocument())).toList
        }.toList

    def bsonToTile(bson: BsonDocument): Tile =
        if (bson.getString("tileType").getValue().toLowerCase.equals("grass")){
            getTile(bson.getString("tileType").getValue(), 
            bson.getInt32("height").getValue(),
            bson.getBoolean("hasLlama").getValue())
        }
        else {
            getTile(bson.getString("tileType").getValue(), 
            bson.getInt32("height").getValue())
        }

    def getTile(tileTypeString: String, height: Int, hasLlama: Boolean = false): Tile =
        val tileType = TileType.apply(tileTypeString)

        tileType match
            case Some(TileType.Grass) => GrassTile(height = height, hasLlama = hasLlama)
            case Some(TileType.None) => EmptyTile()
            case _ => CropTile(height, tileType.get)

    def bsonToInventory(bsonArray: BsonArray): List[(CropType, Int)] =
        bsonArray.asScala.map(bson =>
            val cropType = CropType.apply(bson.asDocument().getString("cropType").getValue())
            val amount = bson.asDocument().getInt32("amount").getValue()

            (cropType.get, amount)
        ).toList

    def bsonToPieceList(bson: BsonArray, index: Int = 0): List[Piece] =
        if (index >= bson.size()) {
            return Nil;
        }
        
        val bsonPiece = bson.get(index).asDocument();
        return bsonToPiece(bsonPiece) :: bsonToPieceList(bson, index+1);

    def bsonToPiece(bson: BsonDocument): Piece =
        Piece(
            PieceType.apply(bson.getString("pieceType").getValue()).get,
            bsonToPieceTileList(bson.getArray("pieceTiles"))
        )
    
    def bsonToPieceTileList(bson: BsonArray, index: Int = 0): List[PieceTile] =
        if (index >= bson.size()) {
            return Nil;
        }
        
        val bsonPieceTile = bson.get(index).asDocument();
        return PieceTile(
            TileType.apply(bsonPieceTile.getString("tileType").getValue()).get,
            bsonPieceTile.getInt32("x").getValue(), 
            bsonPieceTile.getInt32("y").getValue()) ::
            bsonToPieceTileList(bson, index+1);
}

