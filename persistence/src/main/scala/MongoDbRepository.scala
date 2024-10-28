package persistence

import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.MongoCollection
import domain.*
import org.mongodb.scala.bson.collection.immutable.Document
import com.mongodb.client.model.Aggregates
import org.mongodb.scala.bson.BsonArray
import org.bson.BsonValue
import org.bson.conversions.Bson

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.collection.mutable.Map
import persistence.MongoHelpers.*
import com.mongodb.BasicDBObject
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters
import com.mongodb.client.model.UpdateOptions
import org.mongodb.scala.Observable
import scala.concurrent.Promise

//If the mongo server ever dies, just replace the mongoClient string with your own and
//add a database named "lama-land" with the collections "games" and "pieces"
//Leave the "games" collection empty, but fill the "pieces" collection with the 
//provided json file in the resources map "lama-land.pieces.json"
final case class MongoDbRepository():
    //API key in code 🙀🙀🙀🙀 (you can't access it without having the right ip address anyways)
    val mongoClient: MongoClient = MongoClient("mongodb+srv://admin:passw0rd@sogyo.eqzym.mongodb.net/?retryWrites=true&w=majority&appName=sogyo")
    val database: MongoDatabase = mongoClient.getDatabase("lama-land")
    
    def saveGame(key: String, gamestate: GameState) =
        val boardDocument = Document("$set" -> gamestateToBson(key, gamestate));
        val filter = Filters.equal("key", key);
        val options = UpdateOptions().upsert(true);

        val update = database.getCollection("games").updateOne(filter, boardDocument, options).subscribe(obs => {
            println("Insert/update gamestate " + obs.wasAcknowledged())
        })

    def getGame(key: String): GameState =
        val collection: MongoCollection[Document] = database.getCollection("games")
        val filter = Filters.equal("key", key)
        val document = Await.result(collection.find(filter).first().head(), Duration.Inf)
        return bsonToGamestate(document.toBsonDocument());


    def getRandomPieces(pieceType : String, amountOfPieces: Int = 1): List[Piece] =
        val collection: MongoCollection[Document] = database.getCollection("pieces")
        val aggregatePipeline = Seq(Aggregates.`match`(Filters.equal("pieceType", pieceType)),
                                    Aggregates.sample(amountOfPieces))

        val documents = Await.result(collection.aggregate(aggregatePipeline).toFuture(), Duration.Inf)

        return documentToPieceList(documents.toList)

implicit class ObservableToFuture[T](obs: Observable[T]) {
    def toFuture(): Future[Seq[T]] = {
      val promise = Promise[Seq[T]]()
      obs.collect().subscribe(
        (results: Seq[T]) => promise.success(results),
        (e: Throwable) => promise.failure(e)
      )
      promise.future
    }
  }