package repository

import model._

object StudentRepository {
  def getAllStudents(): Future[List[Student]] = {
    val futureStudents = MongoDBConnection.studentCollection.find().toFuture()
    futureStudents.map { docs =>
      Option(docs)
        .map(_.map { doc =>
          Student(
            id = doc.getInteger("id"),
            course = doc.getInteger("Course"),
            gradesId = doc.getInteger("GradesId"),
            teacherIds = Option(doc.getList("TeacherIds", classOf[Integer])).map(_.asScala.map(_.toInt).toList).getOrElse(List.empty)
          )
        }.toList)
        .getOrElse(List.empty)
    }
  }

  def getStudentById(studentId: String): Future[Option[Student]] = {
    val studentDocument = Document("_id" -> new ObjectId(studentId))
    MongoDBConnection.studentCollection.find(studentDocument).headOption().map {
      case Some(doc) =>
        Some(
          Student(
            id = doc.getInteger("id"),
            course = doc.getInteger("Course"),
            gradesId = doc.getInteger("GradesId"),
            teacherIds = Option(doc.getList("TeacherIds", classOf[Integer])).map(_.asScala.map(_.toInt).toList).getOrElse(List.empty)
          )
        )
      case None => None
    }
  }

  def addStudent(student: Student): Future[String] = {
    val studentDocument = BsonDocument(
      "id" -> BsonInt32(student.id),
      "course" -> BsonInt32(student.course),
      "gradesId" -> BsonInt32(student.gradesId),
      "teacherIds" -> BsonArray(student.teacherIds.map(BsonInt32(_)))
    )
    MongoDBConnection.studentCollection.insertOne(studentDocument).toFuture().map(_ => "Студент успешно добавлен")
  }

  def deleteStudent(studentId: String): Future[String] = {
    val studentDocument = Document("_id" -> new ObjectId(studentId))
    MongoDBConnection.studentCollection.deleteOne(studentDocument).toFuture().map {
      case _ => "Студент успешно удален"
    }
  }

  def updateStudent(studentId: String, updatedStudent: Student): Future[String] = {
    val filter = Document("_id" -> new ObjectId(studentId))
    val update = combine(
      set("Course", updatedStudent.course),
      set("GradesID", updatedStudent.gradesId),
      set("TeacherIds", updatedStudent.teacherIds)
    )

    MongoDBConnection.studentCollection
      .updateOne(filter, update)
      .toFuture()
      .map { updateResult =>
        if (updateResult.wasAcknowledged() && updateResult.getModifiedCount > 0) {
          "Студент успешно обновлен"
        } else {
          "Обновление студента не выполнено"
        }
      }
  }
}
