package domain; 

class GameStateTest extends munit.FunSuite {
  test("When replacing a piece the list should show the new piece and keep the list in the same order") {
    val gamestate = GameState(List(): List[Board], 
        List(Piece(PieceType.S, List()), Piece(PieceType.T, List(PieceTile(TileType.Grass, 5, 0))), Piece(PieceType.+, List())))

    val llamaGamestate = gamestate.replacePiece(Piece(PieceType.T, List())).get

    assertEquals(llamaGamestate.placeablePieces, List(Piece(PieceType.S, List()), Piece(PieceType.T, List()), Piece(PieceType.+, List())))
  }

  test("Can't replace a piece that isn't in the list") {
    val gamestate = GameState(List(): List[Board], 
        List(Piece(PieceType.S, List()), Piece(PieceType.T, List(PieceTile(TileType.Grass, 5, 0))), Piece(PieceType.+, List())))

    val failure = gamestate.replacePiece(Piece(PieceType.Start, List())).failed.get

    intercept[IllegalArgumentException](throw failure)
  }

  test("Should be able to create a list of boards based on the provided amount of names") {
    val startingPieces = List(Piece(PieceType.Start, List()), Piece(PieceType.Start, List()), Piece(PieceType.Start, List()))
    val names = List("", "", "")
    val gamestate = GameState(names, startingPieces, List()).get;

    assertEquals(gamestate.boards.length, 3)
  }

  test("The amount of players should at least be 2") {
    val names = List("")
    val startingPieces = List(Piece(PieceType.Start, List()))
    val failure = GameState(names, startingPieces, List()).failed.get;

    intercept[IllegalArgumentException](throw failure)
  }

  test("The amount of names should be the same as the amount of startingpieces") {
    val names = List("", "", "")
    val startingPieces = List(Piece(PieceType.Start, List()))
    val failure = GameState(names, startingPieces, List()).failed.get;

    intercept[IllegalArgumentException](throw failure)
  }

  test("Changing turn to the next board should reorder the list") {
    val board = Board(name = "henk")
    val board2 = Board(name = "jan")
    val gamestate = GameState(List(board, board2), List())

    val newGamestate = gamestate.nextBoard()

    assertEquals(newGamestate.boards.head, board2)
  }

  test("Changing turn to the next board should set their hasPlacedPiece bool to false") {
    val board = Board(name = "henk")
    val board2 = Board(name = "jan", hasPlacedPiece = true)
    val gamestate = GameState(List(board, board2), List())

    val newGamestate = gamestate.nextBoard()

    assertEquals(newGamestate.boards.head, board2.copy(hasPlacedPiece = false))
  }

  test("Player should be able to place a piece through gamestate") {
    val names = List("", "")
    val startPiece = Piece(PieceType.Start, 
      List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Corn, x, y))).flatMap(_.toList))
    val pieces = List(Piece(PieceType.S, List()), Piece(PieceType.T, List(PieceTile(TileType.Grass, 0, 0))), Piece(PieceType.+, List()))
    val gamestate = GameState(names, List(startPiece, Piece(PieceType.Start, List())), pieces).get;

    val newGamestate = gamestate.placePiece(PieceType.T, 7, 7).get

    assertEquals(newGamestate.boards.head.tiles.apply(7).apply(7).getTileType(), TileType.Grass)
  }

  test("Player should be able to place a llama through gamestate") {
    val board = Board(name = "", inventory = List((CropType.Corn, 5)))
    val gamestate = GameState(List(board), List());
    val startPiece = Piece(PieceType.Start, 
      List.tabulate(4)(y => List.tabulate(4)(x => PieceTile(TileType.Grass, x, y))).flatMap(_.toList))
    val newGamestate = gamestate.copy(boards = List(gamestate.boards.head.setStartingPiece(startPiece)))

    val llamaGamestate = newGamestate.buyAndPlaceLlama(CropType.Corn, 7, 7).get

    assertEquals(llamaGamestate.boards.head.tiles.apply(7).apply(7).asInstanceOf[GrassTile].hasLlama, true)
  }

  test("Can rotate a tile of multiple pieces") {
    val pieces = List(Piece(PieceType.T, List(PieceTile(TileType.Grass, 1, 0))), 
    Piece(PieceType.S, List(PieceTile(TileType.Cocoa, -1, -1))))
    val gamestate = GameState(List(): List[Board], pieces)

    val newGameState = gamestate.rotatePlaceablePieces();

    assertEquals(newGameState.placeablePieces, List(Piece(PieceType.T, List(PieceTile(TileType.Grass, 0, 1))),
                                                Piece(PieceType.S, List(PieceTile(TileType.Cocoa, 1, -1)))))
  }

  test("Can rotate all the tiles of a piece") {
    val piece = Piece(PieceType.T, List(PieceTile(TileType.Grass, 0, 0), PieceTile(TileType.Cocoa, 0, 1),
    PieceTile(TileType.Grass, 0, -1), PieceTile(TileType.Corn, -1, -1), PieceTile(TileType.Potato, 1, -1)))
    val gamestate = GameState(List(): List[Board], 
        List(piece))

    val newGameState = gamestate.rotatePlaceablePieces();

    assertEquals(newGameState.placeablePieces.head, 
      Piece(PieceType.T, List(PieceTile(TileType.Grass, 0, 0), PieceTile(TileType.Cocoa, -1, 0),
        PieceTile(TileType.Grass, 1, 0), PieceTile(TileType.Corn, 1, -1), PieceTile(TileType.Potato, 1, 1))))
  }

  test("Can flip a tile of multiple pieces") {
    val pieces = List(Piece(PieceType.T, List(PieceTile(TileType.Grass, 1, 0))), 
    Piece(PieceType.S, List(PieceTile(TileType.Cocoa, -1, 0))))
    val gamestate = GameState(List(): List[Board], pieces)

    val newGameState = gamestate.flipPlaceablePieces();

    assertEquals(newGameState.placeablePieces, List(Piece(PieceType.T, List(PieceTile(TileType.Grass, -1, 0))),
                                                Piece(PieceType.S, List(PieceTile(TileType.Cocoa, 1, 0)))))
  }

  test("Can flip all the tiles of a piece") {
    val piece = Piece(PieceType.T, List(PieceTile(TileType.Grass, 0, 0), PieceTile(TileType.Cocoa, 0, 1),
    PieceTile(TileType.Grass, 0, -1), PieceTile(TileType.Corn, -1, -1), PieceTile(TileType.Potato, 1, -1)))
    val gamestate = GameState(List(): List[Board], 
        List(piece))

    val newGameState = gamestate.flipPlaceablePieces();

    assertEquals(newGameState.placeablePieces.head, 
      Piece(PieceType.T, List(PieceTile(TileType.Grass, 0, 0), PieceTile(TileType.Cocoa, 0, 1),
        PieceTile(TileType.Grass, 0, -1), PieceTile(TileType.Corn, 1, -1), PieceTile(TileType.Potato, -1, -1))))
  }
}
