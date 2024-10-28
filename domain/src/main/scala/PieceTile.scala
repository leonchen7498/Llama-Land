package domain

final case class PieceTile(tileType: TileType, x: Int, y: Int):
    def rotatePieceTile(): PieceTile =
        val newX = y * -1;
        val newY = x;
        return this.copy(x = newX, y = newY)

    def flipPieceTile(): PieceTile =
        val newX = x * -1;
        return this.copy(x = newX)
