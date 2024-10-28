package domain;

abstract class Tile {
    def height: Int

    def getNewTile(tileTypeOfNewTile: TileType): Tile =
        val newHeight = height + 1

        tileTypeOfNewTile match
            case TileType.Grass => GrassTile(newHeight)
            case TileType.None => throw new IllegalArgumentException
            case _ => CropTile(newHeight, tileTypeOfNewTile)

    def allowedToPlaceNewTile(height : Int): Boolean =
        return this.height == height

    def getTileType(): TileType
}