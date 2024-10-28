package domain;

final case class EmptyTile(val height: Int = 0) extends Tile:
    override def getTileType(): TileType =
        TileType.None
    
