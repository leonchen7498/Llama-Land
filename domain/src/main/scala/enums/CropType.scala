package domain

enum CropType {
  case Corn, Cocoa, Potato
}

object CropType {
    implicit def apply(value: String): Option[CropType] =
      CropType.values.find(cropType => cropType.toString().toLowerCase() == value.toLowerCase())
}