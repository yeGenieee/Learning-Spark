import org.apache.spark.sql.SparkSession

object PurchaseEvent {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Purchase Event")
      .master("local[*]")
      .getOrCreate()

    val sc = spark.sparkContext

    val tranFile = sc.textFile("/Users/yegenieee/Desktop/Learning-Spark/spark-in-action/app/src/main/resources/ch04/ch04_data_transactions.txt")
    val tranData = tranFile.map(_.split("#"))
    var transByCustomer = tranData.map(transaction => (transaction(2).toInt, transaction))

    val (customerId, purchase) = transByCustomer.countByKey().toSeq.sortBy(_._2).last // 구매 횟수가 가장 많았던 고객 찾기
    var complimentTransactions = Array(Array("2015-03-30", "11:59 PM", "53", "4", "1", "0.00")) // 사은품 추가 구매 기록 저장

    transByCustomer.lookup(customerId).foreach(tx => println(tx.mkString(", ")))
  }

}
