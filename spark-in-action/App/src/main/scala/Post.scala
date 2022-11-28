
case class Post(
               commentCount:Option[Int],
               lastActivityDate:Option[java.sql.Timestamp],
               ownerUserId:Option[Long],
               body:String,
               score:Option[Int],
               creationDate:Option[java.sql.Timestamp],
               viewCount:Option[Int],
               title:String,
               tags:String,
               answerCount:Option[Int],
               acceptedAnswerId:Option[Long],
               postTypeId:Option[Long],
               id:Long
               )

import java.sql.Timestamp

object StringImplicits {
  implicit class StringImprovements(val s: String) {
    import scala.util.control.Exception.catching
    def toIntSafe = catching(classOf[NumberFormatException]) opt s.toInt
    def toLongSafe = catching(classOf[NumberFormatException]) opt s.toLong
    def toTimestampeSafe = catching(classOf[IllegalArgumentException]) opt Timestamp.valueOf(s)
  }
}
