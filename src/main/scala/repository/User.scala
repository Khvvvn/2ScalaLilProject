package repository

import model._

object UserRepository {

  def getAllUsers(): Future[List[User]] = {
    val futureUsers = MongoDBConnection.userCollection.find().toFuture()
    futureUsers.map { docs =>
      Option(docs)
        .map(_.map { doc =>
          User(
            id = doc.getInteger("id"),
            login = doc.getString("Login"),
            password = doc.getString("Password"),
            userType = doc.getString("UserType"),
            name = doc.getString("Name"),
            birthDate = doc.getString("BirthDate"),
            gender = doc.getString("Gender"),
            nationality = doc.getString("Nationality"),
            address = doc.getString("Address")
          )
        }.toList)
        .getOrElse(List.empty)
    }
  }

  def getUserById(userId: String): Future[Option[User]] = {
    val userDocument = Document("_id" -> new ObjectId(userId))
    MongoDBConnection.userCollection.find(userDocument).headOption().map {
      case Some(doc) =>
        Some(
          User(
            id = doc.getInteger("id"),
            login = doc.getString("Login"),
            password = doc.getString("Password"),
            userType = doc.getString("UserType"),
            name = doc.getString("Name"),
            birthDate = doc.getString("BirthDate"),
            gender = doc.getString("Gender"),
            nationality = doc.getString("Nationality"),
            address = doc.getString("Address")
          )
        )
      case None => None
    }
  }

  def addUser(user: User): Future[String] = {
    val userDocument = BsonDocument(
      "id" -> BsonInt32(user.id),
      "Login" -> BsonString(user.login),
      "Password" -> BsonString(user.password),
      "UserType" -> BsonString(user.userType),
      "Name" -> BsonString(user.name),
      "BirthDate" -> BsonString(user.birthDate),
      "Gender" -> BsonString(user.gender),
      "Nationality" -> BsonString(user.nationality),
      "Address" -> BsonString(user.address)
    )
    MongoDBConnection.userCollection.insertOne(userDocument).toFuture().map(_ => "Пользователь успешно добавлен")
  }

  def deleteUser(userId: String): Future[String] = {
    val userDocument = Document("_id" -> new ObjectId(userId))
    MongoDBConnection.userCollection.deleteOne(userDocument).toFuture().map {
      case _ => "Пользователь успешно удален"
    }
  }

  def updateUser(userId: String, updatedUser: User): Future[String] = {
    val filter = Document("_id" -> new ObjectId(userId))
    val update = combine(
      set("id", updatedUser.id),
      set("Login", updatedUser.login),
      set("Password", updatedUser.password),
      set("UserType", updatedUser.userType),
      set("Name", updatedUser.name),
      set("BirthDate", updatedUser.birthDate),
      set("Gender", updatedUser.gender),
      set("Nationality", updatedUser.nationality),
      set("Address", updatedUser.address)
    )

    MongoDBConnection.userCollection
      .updateOne(filter, update)
      .toFuture()
      .map { updateResult =>
        if (updateResult.wasAcknowledged() && updateResult.getModifiedCount > 0) {
          "Пользователь успешно обновлен"
        } else {
          "Обновление пользователя не выполнено"
        }
      }
  }

}
