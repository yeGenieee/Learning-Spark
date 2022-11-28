# Data Join & Grouping

## Data Join
- RDD를 다른 RDD와 결합

### RDBMS와 유사한 조인 연산자
1. Join
2. LeftOuterJoin
3. RightOuterJoin
4. FullOuterJoin
- 스파크에서 제공하는 네 가지 조인 연산자는 RDBMS 조인연산자와 동일하게 동작한다
- 단, 스파크의 조인 연산자는 PairRDD에서만 사용할 수 있다
- 조인 연산자에도 Partitioner 객체나 파티션 개수를 전달할 수 있는데, 파티션 개수만 지정하면 파티셔너는 `HashPartitioner`를 사용한다
- Partitioner를 지정하지 않으면, 조인할 두 RDD 중 첫번째 RDD의 Partitioner를 사용한다
  - 두 RDD에 Partitioner를 명시적으로 지정하지 않으면, 스파크는 새로운 `HashPartitioner`를 사용한다.
  - 이 때는, 파티션 개수로 `spark.default.partitions` 매개변수 값을 사용하거나, 두 RDD의 파티션 개수 중 더 큰 것을 사용한다


```Scala
package org.example

import org.apache.spark.sql.SparkSession

object Sales {
  def main(args: Array[String]): Unit ={
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("Sales")
      .getOrCreate()

    val sc = spark.sparkContext

    val txFile = sc.textFile("/Users/yejchoi-mn/Desktop/spark-in-action/book/spark/src/main/resources/ch04/ch04_data_transactions.txt")
    val txData = txFile.map(_.split("#"))

    // 1. 어제 판매한 상품 이름과 각 상품별 매출액 합계 (각 상품명을 알파벳 오름차순으로 정렬)
    val txProduct = txData.map(tx => (tx(3).toInt, tx)) // 상품 ID를 key로 설정한 Pair RDD를 생성
    val totalsByProduct = txProduct.mapValues(t => t(5).toDouble) // reduceByKey를 이용해서 각 상품의 매출액 합계를 구하기
      .reduceByKey {
        case(total1, total2) => total1 + total2
      }

    println(totalsByProduct.first())

    val productFile = sc.textFile("/Users/yejchoi-mn/Desktop/spark-in-action/book/spark/src/main/resources/ch04/ch04_data_products.txt")
    val products = productFile.map(_.split("#"))
      .map(p => (p(0).toInt, p))

    println(products.first())

    val totalAndProducts = totalsByProduct.join(products)
    println(totalAndProducts.first())

    // 2. 어제 판매하지 않은 상품 목록
    val totalsWithUnorderedProducts = products.leftOuterJoin(totalsByProduct)
    println(totalsWithUnorderedProducts.first())

    val missingProducts = totalsWithUnorderedProducts.filter(item => item._2._2.isEmpty)
      .map(x => x._2._1)

    totalsWithUnorderedProducts.foreach(
      p => println(p)
    )

//    val totalsWithUnorderedProducts = totalsByProduct.rightOuterJoin(products)
//
//    val unorderedProducts = totalsWithUnorderedProducts
//      .filter(item => item._2._1 == None)
//      .map(x => x._2._2)

    missingProducts
      .foreach(
      p => println(p.mkString(", "))
    )

    // 3. 전일 판매 실적 통계 : 각 고객이 궁비한 상품의 평균 가격, 최저 가격 및 최고 가격, 구매 금액 합계
  }

}

```

### subtract & subtractByKey
- `subtract`
  - 첫번째 RDD에서 두번째 RDD의 요소를 제거한 여집합을 반환
  - 일반 RDD에서도 사용할 수 있고, 각 요소의 키가 값만 비교하는 것이 아닌 요소 전체를 비교해서 제거 여부를 판단한다
- `subtractByKey`
  - Pair RDD에서만 사용 가능한 메서드
  - 첫번째 RDD의 키-값 쌍 중 두번째 RDD에 포함되지 않은 키의 요소들로 RDD를 구성해서 반환한다
  - 첫번째 RDD와 두번째 RDD가 반드시 같은 타입일 필요는 없음
