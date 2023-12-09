package repository

import MongoDBConnection._
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonDocument, BsonInt32, BsonString}
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContextExecutor, Future}
import org.mongodb.scala.model.Updates.{combine, set}
import org.bson.types.ObjectId

case class Message(
                    id: Int,
                    sender: String,
                    recipient: String,
                    content: String,
                    timestamp: String
                  )

object MessageRepository {

  def getAllMessages(): Future[List[Message]] = {
    val futureMessages = MongoDBConnection.messageCollection.find().toFuture()
    futureMessages.map { docs =>
      Option(docs)
        .map(_.map { doc =>
          Message(
            id = doc.getInteger("id"),
            sender = doc.getString("Sender"),
            recipient = doc.getString("Recipient"),
            content = doc.getString("Content"),
            timestamp = doc.getString("Timestamp")
          )
        }.toList)
        .getOrElse(List.empty)
    }
  }

  def getMessageById(messageId: String): Future[Option[Message]] = {
    val messageDocument = Document("_id" -> new ObjectId(messageId))
    MongoDBConnection.messageCollection.find(messageDocument).headOption().map {
      case Some(doc) =>
        Some(
          Message(
            id = doc.getInteger("id"),
            sender = doc.getString("Sender"),
            recipient = doc.getString("Recipient"),
            content = doc.getString("Content"),
            timestamp = doc.getString("Timestamp")
          )
        )
      case None => None
    }
  }

  def addMessage(message: Message): Future[String] = {
    val messageDocument = BsonDocument(
      "id" -> BsonInt32(message.id),
      "Sender" -> BsonString(message.sender),
      "Recipient" -> BsonString(message.recipient),
      "Content" -> BsonString(message.content),
      "Timestamp" -> BsonString(message.timestamp)
    )
    MongoDBConnection.messageCollection.insertOne(messageDocument).toFuture().map(_ => "Message added successfully")
  }

  def deleteMessage(messageId: String): Future[String] = {
    val messageDocument = Document("_id" -> new ObjectId(messageId))
    MongoDBConnection.messageCollection.deleteOne(messageDocument).toFuture().map {
      case _ => "Message deleted successfully"
    }
  }

  def updateMessage(messageId: String, updatedMessage: Message): Future[String] = {
    val filter = Document("_id" -> new ObjectId(messageId))
    val update = combine(
      set("id", updatedMessage.id),
      set("Sender", updatedMessage.sender),
      set("Recipient", updatedMessage.recipient),
      set("Content", updatedMessage.content),
      set("Timestamp", updatedMessage.timestamp)
    )

    MongoDBConnection.messageCollection
      .updateOne(filter, update)
      .toFuture()
      .map { updateResult =>
        if (updateResult.wasAcknowledged() && updateResult.getModifiedCount > 0) {
          "Message updated successfully"
        } else {
          "Message update not performed"
        }
      }
  }
}
