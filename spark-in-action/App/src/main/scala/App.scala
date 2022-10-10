import org.apache.spark.sql.SparkSession

object App {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("GitHub push counter")
      .master("local[*]")
      .getOrCreate()

    val sc = spark.sparkContext

    val homeDir = System.getenv("HOME")
    val inputPath = homeDir + "/Desktop/Learning-Spark/spark-in-action/app/src/main/resources/2022-09-01-0.json"
    val ghLog = spark.read.json(inputPath)

    val pushes = ghLog.filter("type = 'PushEvent'")

    pushes.printSchema()
    println("all events : " + ghLog.count())
    println("only pushes : " + pushes.count())
    pushes.show(5)
  }

}
