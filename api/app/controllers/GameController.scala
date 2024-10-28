package controllers

import javax.inject.*
import play.api.*
import play.api.mvc.*
import play.api.libs.json.Json
import play.api.libs.json.OFormat
import domain.*;
import persistence.*;
import java.util.UUID;
import models.BoardJsonObjectWriters.*;
import play.api.libs.json.JsObject
import org.mongodb.scala.bson.collection.immutable.Document
import scala.util.Try
import scala.util.Success
import scala.util.Failure

@Singleton
class GameController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  val repo = MongoDbRepository();

  def startGame() = Action { implicit request: Request[AnyContent] =>
    val names = request.body.asJson.get.apply("names").as[List[String]]; //
    if (names.length < 2 || names.length > 4) {
      Unauthorized("Start the game with at least 2 players and at most 4 players.")
    }

    val placeablePieces = getNewListOfPlaceablePieces(PieceType.values.toList)
    val startingPieces = repo.getRandomPieces(PieceType.Start.toString(), names.length);
    val tryGamestate = GameState(names, startingPieces, placeablePieces)

    tryGamestate match
      case Success(gamestate) =>
        val key = UUID.randomUUID.toString;
        repo.saveGame(key, gamestate);
        val gamestateJson = Json.toJson(gamestate).as[JsObject];
        val keyJson = ("gameKey" -> Json.toJson(key))

        Ok(Json.stringify(gamestateJson + keyJson)).withSession("gameKey" -> key);
      case Failure(_) =>
        Unauthorized("Start the game with at least 2 players and at most 4 players.")
  }

  private def getNewListOfPlaceablePieces(pieceTypes: List[PieceType]): List[Piece] =
    pieceTypes match
      case Nil => Nil
      case head :: next =>
        if (head == PieceType.Start) {
          getNewListOfPlaceablePieces(next)
        }
        else {
          repo.getRandomPieces(head.toString()).head :: getNewListOfPlaceablePieces(next)
        }

  def getGame(gameKey: String) = Action { implicit request: Request[AnyContent] =>
    val gamestate = repo.getGame(gameKey)
    Ok(Json.toJson(gamestate)).withSession("gameKey" -> gameKey);
  }

  def endTurn() = Action { implicit request: Request[AnyContent] =>
    request.session.get("gameKey")
      .map { key => 
        val gamestate = repo.getGame(key);
        val newGamestate = gamestate.nextBoard();

        repo.saveGame(key, newGamestate);

        Ok(Json.toJson(newGamestate))
      }.getOrElse {
        Unauthorized("You have not started a game yet!")
      }
  }

  def placeTile(pieceTypeString: String, x: Int, y: Int) = Action { implicit request: Request[AnyContent] =>
    request.session.get("gameKey") match
      case Some(key) => 
        PieceType.apply(pieceTypeString) match
          case Some(pieceType) => 
            val gamestate = repo.getGame(key);

            gamestate.placePiece(pieceType, x, y) match
              case Success(newGamestate) => 
                val newPiece = repo.getRandomPieces(pieceType.toString()).head
                val finalGamestate = newGamestate.replacePiece(newPiece).get

                repo.saveGame(key, finalGamestate);

                Ok(Json.toJson(finalGamestate)).withSession("gameKey" -> key);
              case Failure(_) =>
                Unauthorized("You can't place a tile here!")
          case None => 
            Unauthorized("That is not a valid piece!")
      case None =>
        Unauthorized("You have not started a game yet!")
  }

  def placeAndBuyLlama(cropTypeString: String, x: Int, y: Int) = Action { implicit request: Request[AnyContent] =>
    val keyOption = request.session.get("gameKey")
    keyOption match
      case Some(key) => 
        CropType.apply(cropTypeString) match
          case Some(cropType) => 
            val gamestate = repo.getGame(key);
            gamestate.buyAndPlaceLlama(cropType, x, y) match
              case Success(newGamestate) => 
                repo.saveGame(key, newGamestate);
                val gamestateJson = Json.toJson(newGamestate);
                Ok(gamestateJson).withSession("gameKey" -> key);
              case Failure(exception) => 
                Unauthorized(exception.getMessage())
          case None => 
            Unauthorized("That's not a valid crop!")
      case None => 
        Unauthorized("That key doesn't exist")
  }

  def rotatePieces() = Action { implicit request: Request[AnyContent] =>
    request.session.get("gameKey")
      .map { key => 
        val gamestate = repo.getGame(key);
        val newGamestate = gamestate.rotatePlaceablePieces();

        repo.saveGame(key, newGamestate);

        Ok(Json.toJson(newGamestate))
      }.getOrElse {
        Unauthorized("You have not started a game yet!")
      }
  }

  def flipPieces() = Action { implicit request: Request[AnyContent] =>
    request.session.get("gameKey")
      .map { key => 
        val gamestate = repo.getGame(key);
        val newGamestate = gamestate.flipPlaceablePieces();

        repo.saveGame(key, newGamestate);

        Ok(Json.toJson(newGamestate))
      }.getOrElse {
        Unauthorized("You have not started a game yet!")
      }
  }
}
