package repository

import model._

object TeacherRepository {

  def getAllTeachers(): Future[List[Teacher]] = {
    val futureTeachers = MongoDBConnection.teacherCollection.find().toFuture()
    futureTeachers.map(_.map { doc =>
      Teacher(
        id = doc.getInteger("id"),
        education = doc.getString("Education"),
        experience = doc.getInteger("Experience"),
        studentsID = doc.getList("StudentsID", classOf[Integer]).asScala.map(_.toInt).toList
      )
    }.toList)
  }

  def getTeacherById(teacherId: String): Future[Option[Teacher]] = {
    val teacherDocument = Document("_id" -> new ObjectId(teacherId))
    MongoDBConnection.teacherCollection.find(teacherDocument).headOption().map {
      case Some(doc) =>
        Some(
          Teacher(
            id = doc.getInteger("id"),
            education = doc.getString("Education"),
            experience = doc.getInteger("Experience"),
            studentsID = doc.getList("StudentsID", classOf[Integer]).asScala.map(_.toInt).toList
          )
        )
      case None => None
    }
  }

  def addTeacher(teacher: Teacher): Future[String] = {
    val teacherDocument = BsonDocument(
      "id" -> BsonInt32(teacher.id),
      "Education" -> BsonString(teacher.education),
      "Experience" -> BsonInt32(teacher.experience),
      "StudentsID" -> BsonArray(teacher.studentsID.map(BsonInt32(_)))
    )
    MongoDBConnection.teacherCollection.insertOne(teacherDocument).toFuture().map(_ => "Учитель успешно добавлен")
  }


  def deleteTeacher(teacherId: String): Future[String] = {
    val teacherDocument = Document("_id" -> new ObjectId(teacherId))
    MongoDBConnection.teacherCollection.deleteOne(teacherDocument).toFuture().map {
      case _ => "Учитель успешно удален"
    }
  }


  def updateTeacher(teacherId: String, updatedTeacher: Teacher): Future[String] = {
    val filter = Document("_id" -> new ObjectId(teacherId))
    val update = combine(
      set("id", updatedTeacher.id)
      set("Education", updatedTeacher.education),
      set("Experience", updatedTeacher.experience),
      set("StudentsID", updatedTeacher.studentsID)
    )

    MongoDBConnection.teacherCollection
      .updateOne(filter, update)
      .toFuture()
      .map { updateResult =>
        if (updateResult.wasAcknowledged() && updateResult.getModifiedCount > 0) {
          "Преподаватель успешно обновлен"
        } else {
          "Обновление преподавателя не выполнено"
        }
      }
  }
}
