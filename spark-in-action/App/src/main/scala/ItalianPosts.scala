import org.apache.spark.sql.SparkSession

object ItalianPosts {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Italian Posts")
      .master("local[*]")
      .getOrCreate()

    val sc = spark.sparkContext


    val italianPosts = sc.textFile("/Users/yegenieee/Desktop/Learning-Spark/spark-in-action/app/src/main/resources/ch05/italianPosts.csv")
    val italianPostsSplit = italianPosts.map(x => x.split("~")) // 문자열 배열로 구성된 RDD 반환

    val italianPostsRDD = italianPostsSplit.map(x => (x(0), x(1), x(2), x(3), x(4), x(5), x(6)
    ,x(7), x(8), x(9), x(10), x(11), x(12)))

    val italianPostsDataFrame = italianPostsRDD.toDF()

    italianPostsDataFrame.show(10)

    val itPostsDF = italianPostsRDD.toDF("commentCount", "lastActivityDate", "ownerUserId", "body", "score",
    "creationDate", "viewCount", "title", "tags", "answerCount", "acceptedAnswerId", "postTypeId", "id")



  }

}
