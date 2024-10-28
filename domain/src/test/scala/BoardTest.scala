package domain;

class BoardTest extends munit.FunSuite{
    test("Board should start with the given tiles in the center of the board") {
        val board = Board(name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y)))
        val flattened = tiles.flatMap(_.toList)

        val newBoard = board.setStartingPiece(Piece(PieceType.Start, flattened))

         //top left corner of starting tiles
        assertEquals(newBoard.tiles.apply(6).apply(6), CropTile(1, TileType.Corn))

         //bottom right corner of starting tiles
        assertEquals(newBoard.tiles.apply(9).apply(9), CropTile(1, TileType.Corn))
    }

    test("Start tiles placed should have height as 1") {
        val board = Board(name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y)))
        val flattened = tiles.flatMap(_.toList)

        val newBoard = board.setStartingPiece(Piece(PieceType.Start, flattened))

        assertEquals(newBoard.tiles.apply(7).apply(7).height, 1)
    }

    test("Tile can only be placed next to another tile") {
        val board = Board(name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))
        val piece = Piece(PieceType.S, List(PieceTile(TileType.Grass, 0, 0)))

        val newBoard = startBoard.placePiece(piece, 5, 6).get;

        assertEquals(newBoard.tiles.apply(6).apply(5), GrassTile(1))
    }

    test("Tile can not be placed when there are no tiles surrounding it") {
        val board = Board(name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))
        val piece = Piece(PieceType.S, List(PieceTile(TileType.Grass, 0, 0)))

        val failure = startBoard.placePiece(piece, 0, 0).failed.get;

        intercept[IllegalArgumentException](throw failure)
    }

    test("Placing a tile on top of another tile should increase it's height") {
        val board = Board(name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))
        val piece = Piece(PieceType.S, List(PieceTile(TileType.Grass, 0, 0)))

        val newBoard = startBoard.placePiece(piece, 7, 7).get;

        assertEquals(newBoard.tiles.apply(7).apply(7), GrassTile(2))
    }

    test("Can't place an empty tile on top of another tile") {
        val board = Board(name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))
        val piece = Piece(PieceType.S, List(PieceTile(TileType.None, 0, 0)))

        intercept[IllegalArgumentException](startBoard.placePiece(piece, 7, 7))
    }

    test("Can't place a tile on top of a tile with a llama on it") {
        val inventory = List((CropType.Cocoa, 5), (CropType.Corn, 5), (CropType.Potato, 5))
        val board = Board(inventory = inventory, name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Grass, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))
        val newBoard = startBoard.buyAndPlaceLlama(CropType.Cocoa, 7, 7).get
        val piece = Piece(PieceType.S, List(PieceTile(TileType.Grass, 0, 0)))

        val failure = newBoard.placePiece(piece, 7, 7).failed.get

        intercept[IllegalArgumentException](throw failure)
    }


    test("Placing a piece on top of another tile should increase it's height") {
        val board = Board(name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))

        val piece = Piece(PieceType.+, List(PieceTile(TileType.Corn, 0, 0), 
            PieceTile(TileType.Corn, -1, 0), PieceTile(TileType.Corn, 1, 0),
            PieceTile(TileType.Corn, 0, -1), PieceTile(TileType.Corn, 0, 1)))

        val newBoard = startBoard.placePiece(piece, 8, 8).get;

        assertEquals(newBoard.tiles.apply(8).apply(8), CropTile(2, CropType.Corn))
        assertEquals(newBoard.tiles.apply(8).apply(7), CropTile(2, CropType.Corn))
        assertEquals(newBoard.tiles.apply(8).apply(9), CropTile(2, CropType.Corn))
        assertEquals(newBoard.tiles.apply(7).apply(8), CropTile(2, CropType.Corn))
        assertEquals(newBoard.tiles.apply(9).apply(8), CropTile(2, CropType.Corn))
    }

    test("Placing a piece should be allowed as long as one of the tiles are bordering another tile") {
        val board = Board(name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))

        val piece = Piece(PieceType.+, List(PieceTile(TileType.Grass, 0, 0), 
            PieceTile(TileType.Grass, -1, 0), PieceTile(TileType.Grass, 1, 0),
            PieceTile(TileType.Grass, 0, -1), PieceTile(TileType.Grass, 0, 1)))

        //This is 2 tiles to the left of the top left starting tile, aka the right
        // tile of the plus piece borders the left side of the starting piece
        val newBoard = startBoard.placePiece(piece, 4, 6).get;

        assertEquals(newBoard.tiles.apply(6).apply(4), GrassTile(1))
        assertEquals(newBoard.tiles.apply(6).apply(3), GrassTile(1))
        assertEquals(newBoard.tiles.apply(6).apply(5), GrassTile(1))
        assertEquals(newBoard.tiles.apply(5).apply(4), GrassTile(1))
        assertEquals(newBoard.tiles.apply(7).apply(4), GrassTile(1))
    }

    test("Trying to place a piece on an empty tile should not work when one of the tiles to place " +
      "is hitting another tile") {
        val board = Board(name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))

        val piece = Piece(PieceType.+, List(PieceTile(TileType.Grass, 0, 0), 
            PieceTile(TileType.Grass, -1, 0), PieceTile(TileType.Grass, 1, 0),
            PieceTile(TileType.Grass, 0, -1), PieceTile(TileType.Grass, 0, 1)))

        //This is one tile above the starting piece, aka the bottom tile of the piece
        //overlaps with the starting piece while the rest doesn't overlap
        val failure = startBoard.placePiece(piece, 6, 5).failed.get;

        intercept[IllegalArgumentException](throw failure)
    }

    test("Trying to place a piece over another tile should not work when the tiles aren't the same height") {
        val board = Board(name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))

        val piece = Piece(PieceType.+, List(PieceTile(TileType.Grass, 0, 0), 
            PieceTile(TileType.Grass, -1, 0), PieceTile(TileType.Grass, 1, 0),
            PieceTile(TileType.Grass, 0, -1), PieceTile(TileType.Grass, 0, 1)))

        //This is the 2nd tile of the starting piece, aka the top tile of the piece to place
        //would not be on the starting piece while the other 4 would overlap.
        val failure = startBoard.placePiece(piece, 7, 6).failed.get;

        intercept[IllegalArgumentException](throw failure)
    }

    test("Placing a tile on top of a corn tile should add corn to the player's inventory") {
        val board = Board(name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))
        val piece = Piece(PieceType.S, List(PieceTile(TileType.Grass, 0, 0)))
        
        val newBoard = startBoard.placePiece(piece, 7, 7).get;

        assertEquals(newBoard.inventory.contains((CropType.Corn, 1)), true)
    }

    test("Placing a tile on top of a grass tile should yield no crops") {
        val board = Board(name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Grass, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))
        val piece = Piece(PieceType.S, List(PieceTile(TileType.Grass, 0, 0)))
        
        val newBoard = startBoard.placePiece(piece, 7, 7).get;

        assertEquals(newBoard.inventory.contains((CropType.Corn, 0)), true)
        assertEquals(newBoard.inventory.contains((CropType.Potato, 0)), true)
        assertEquals(newBoard.inventory.contains((CropType.Cocoa, 0)), true)
    }

    test("When the player has 4 of one type of crop they can buy a llama and place it on grass") {
        val inventory = List((CropType.Cocoa, 4), (CropType.Corn, 0), (CropType.Potato, 0))
        val board = Board(inventory = inventory, name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Grass, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))
        
        val newBoard = startBoard.buyAndPlaceLlama(CropType.Cocoa, 7, 7).get

        assertEquals(newBoard.tiles.apply(7).apply(7), GrassTile(1, true))
    }

    test("When the player doesn't have enough crops they can't purchase a llama") {
        val inventory = List((CropType.Cocoa, 0), (CropType.Corn, 3), (CropType.Potato, 0))
        val board = Board(inventory = inventory, name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Grass, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))
        
        val failure = startBoard.buyAndPlaceLlama(CropType.Corn, 7, 7).failed.get

        intercept[IllegalArgumentException](throw failure)
    }

    test("Can't buy a llama and place it on a non-grass tile") {
        val inventory = List((CropType.Cocoa, 5), (CropType.Corn, 5), (CropType.Potato, 5))
        val board = Board(inventory = inventory, name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))
        
        val failure = startBoard.buyAndPlaceLlama(CropType.Potato, 7, 7).failed.get

        intercept[IllegalArgumentException](throw failure)
    }

    test("Can't place 2 pieces in succession") {
        val inventory = List((CropType.Cocoa, 5), (CropType.Corn, 5), (CropType.Potato, 5))
        val board = Board(inventory = inventory, name = "")
        val tiles = List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y))).flatMap(_.toList)
        val startBoard = board.setStartingPiece(Piece(PieceType.Start, tiles))
        val piece = Piece(PieceType.S, List(PieceTile(TileType.Grass, 0, 0)))
        
        val newBoard = startBoard.placePiece(piece, 7, 7).get
        val failure = newBoard.placePiece(piece, 7, 8).failed.get

        intercept[IllegalArgumentException](throw failure)
    }
}
