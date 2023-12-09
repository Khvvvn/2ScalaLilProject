package model


case class User(
                 id: Int,
                 login: String,
                 password: String,
                 userType: String,
                 name: String,
                 birthDate: String,
                 gender: String,
                 nationality: String,
                 address: String
               )

case class Message(
                    title: String,
                    studentIds1: List[Int],
                    studentIds2: List[Int],
                    text: String,
                    checks: Boolean
                  )

case class E_Service(
                    service: String,
                    title: String,
                    text: String,
                    price: Double
                  )

case class Student(
                    id: Int,
                    course: Int,
                    gradesId: Int,
                    teacherIds: List[Int]
                  )

case class Teacher(
                    id: Int,
                    education: String,
                    experience: Int,
                    studentsID: List[Int],
                  )
