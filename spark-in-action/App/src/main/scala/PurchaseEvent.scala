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

    transByCustomer = transByCustomer.mapValues(tx => {
      if (tx(3).toInt == 25 && tx(4).toDouble > 1)
        tx(5) = (tx(5).toDouble * 0.95).toString
      tx
    })

    transByCustomer = transByCustomer.flatMapValues(tx => {
      if (tx(3).toInt == 81 && tx(4).toDouble >= 5) {
        val cloned = tx.clone()
        cloned(5) = "0.00"; cloned(3) = "70"; cloned(4) = "1";
        List(tx, cloned)
      } else {
        List(tx)
      }
    })

    val amounts = transByCustomer.mapValues(tx => tx(5).toDouble)
    val totals = amounts.foldByKey(0)((p1, p2) => p1 + p2).collect()
    println(totals.toSeq.sortBy(_._2).last)

    complimentTransactions = complimentTransactions :+ Array("2015-03-30", "11:59 PM", "76", "63", "1", "0.00")

    transByCustomer = transByCustomer.union(sc.parallelize(complimentTransactions.map(t => (t(2).toInt, t))))

    transByCustomer.map(t => t._2.mkString("#"))
      .saveAsTextFile("ch04_output_transByCustomer")

  }

}
