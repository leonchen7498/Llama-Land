import domain.*

class EnumTest extends munit.FunSuite{
  test("Searching for a corn crop should return CropType.Corn"){
    val corn = CropType.apply("corn")

    assertEquals(corn.get, CropType.Corn)
  }

  test("Searching for an irrelevant crop should return nothing"){
    val nothing = CropType.apply("spaghetti")

    assertEquals(nothing, None)
  }

  test("Searching for a grass tile should return TileType.Grass"){
    val grass = TileType.apply("grass")

    assertEquals(grass.get, TileType.Grass)
  }

  test("Searching for an irrelevant tile should return nothing"){
    val nothing = TileType.apply("water")

    assertEquals(nothing, None)
  }

  test("Searching for a + piece should return PieceType.+"){
    val plus = PieceType.apply("+")

    assertEquals(plus.get, PieceType.+)
  }

  test("Searching for an irrelevant piece should return nothing"){
    val nothing = PieceType.apply("basketball")

    assertEquals(nothing, None)
  }
}
