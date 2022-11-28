package org.spark.example

import org.apache.spark.sql.SparkSession

import scala.io.Source.fromFile

object SparkExample {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().getOrCreate()

    val sc = spark.sparkContext

    val ghLog = spark.read.json(args(0))

    val pushes = ghLog.filter("type = 'PushEvent'")
    val grouped = pushes.groupBy("actor.login").count()
    val ordered = grouped.orderBy(grouped("count").desc)

    val employeeSet = Set() ++ (
      for {
        line <- fromFile(args(1)).getLines
      } yield line.trim
      )

    val bcEmployees = sc.broadcast(employeeSet)

    import spark.implicits._

    val isEmp = user => bcEmployees.value.contains(user)
    val udf = spark.udf.register("isEmpUdf", isEmp)

    val filtered = ordered.filter(udf($"login"))

    filtered.write.format(args(3)).save(args(2))
  }

}
