package org.example

import org.apache.spark.sql.SparkSession

object CustomerTransaction {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("Customer Transaction")
      .getOrCreate()

    val sc = spark.sparkContext

    val tranFile = sc.textFile("/Users/yejchoi-mn/Desktop/spark-in-action/book/spark/src/main/resources/ch04/ch04_data_transactions.txt")
    val transactionData = tranFile.map(_.split("#"))

    var transactionByCustomer = transactionData.map(tx => (tx(2).toInt, tx))

    val customerCount = transactionByCustomer.keys.distinct().count()
    println("Customer count : " + customerCount)

    transactionByCustomer.countByKey()

    // 구매 횟수가 가장 많은 고객에게 사은품으로 곰돌이 보내기
    val (customerId, purchase) = transactionByCustomer.countByKey().toSeq.sortBy(_._2).last // 구매 횟수가 가장 많은 고객 뽑기
    var complimentaryTx = Array(Array("2022-10-28", "11:59 PM", customerId.toString, "4", "1", "0.00")) // 사은품 추가 구매 기록

    // 바비 쇼핑몰 놀이 세트를 두 개 이상 구매한 고객에게 청구 금액을 5% 할인해주기
    transactionByCustomer = transactionByCustomer.mapValues(tx => {
      if (tx(3).toInt == 25 && tx(4).toDouble > 1)
        tx(5) = (tx(5).toDouble * 0.95).toString
      tx
    })

    // 사전을 다섯 권 이상 구매한 고객에게 사은품으로 칫솔 보내기
    transactionByCustomer = transactionByCustomer.flatMapValues(tx => {
      if (tx(3).toInt == 81 && tx(4).toDouble >= 5) {
        val cloned = tx.clone()
        cloned(3) = "70"; cloned(4) = "1"; cloned(5) = "0.00";
        List(tx, cloned)
      }
      else
        List(tx)
    })

    // 가장 많은 금액을 지출한 고객에게 사은품으로 커플 잠옷 보내기
    val amounts = transactionByCustomer.mapValues(tx => tx(5).toDouble)
    val totals = amounts.foldByKey(0)((p1, p2) => p1 + p2).collect()
    val (mostPayCustomerId, totalAmount) = totals.toSeq.sortBy(_._2).last

    complimentaryTx = complimentaryTx :+ Array("2022-10-28", "11:59 PM", mostPayCustomerId.toString, "63", "1", "0.00")

    transactionByCustomer = transactionByCustomer.union(
      sc.parallelize(complimentaryTx)
        .map(tx => (tx(2).toInt, tx))
    )

    transactionByCustomer.map(t => t._2.mkString(";")).saveAsTextFile("/Users/yejchoi-mn/Desktop/spark-in-action/book/spark/src/main/resources/ch04-output-txByCust")

  }

}
