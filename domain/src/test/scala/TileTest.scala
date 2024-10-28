import domain.Tile
import domain.TileType
import domain.GrassTile
import domain.CropTile
import domain.EmptyTile
import domain.CropType

class TileTest extends munit.FunSuite{
    test("Height should be increased by 1") {
        val tile = GrassTile(5);

        val newTile = tile.getNewTile(TileType.Cocoa);

        assertEquals(newTile, CropTile(6, TileType.Cocoa))
    }

    test("An empty tile can't be placed above another tile") {
        val tile = GrassTile(5);

        intercept[IllegalArgumentException](tile.getNewTile(TileType.None)) ;
    }

    test("A CropTile should be able to be created by TileType") {
        val tileType = CropTile(0, TileType.Potato)

        assertEquals(tileType, CropTile(0, CropType.Potato))
    }

    test("A CropTile can't be created if there's no matching croptype") {
        intercept[IllegalArgumentException](CropTile(0, TileType.None))
    }

    test("Getting the tiletype should correspond to their matching croptype") {
        val tileType = CropTile(0, CropType.Potato).getTileType()

        assertEquals(tileType, TileType.Potato)
    }
}
