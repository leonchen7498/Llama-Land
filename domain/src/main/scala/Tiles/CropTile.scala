package domain;

final case class CropTile(val height: Int, val cropType: CropType) extends Tile {
  override def getTileType(): TileType =
    cropType match
        case CropType.Cocoa => TileType.Cocoa
        case CropType.Corn => TileType.Corn
        case CropType.Potato => TileType.Potato

} 

object CropTile {
    def apply(height: Int, tileType: TileType) =
        tileType match
            case TileType.Cocoa => new CropTile(height, CropType.Cocoa)
            case TileType.Corn => new CropTile(height, CropType.Corn)
            case TileType.Potato => new CropTile(height, CropType.Potato)
            case _ => throw new IllegalArgumentException
}
