import org.apache.spark.sql.SparkSession

import scala.io.Source.fromFile

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

    val grouped = pushes.groupBy("actor.login").count()
    grouped.show(5)

    val ordered = grouped.orderBy(grouped("count").desc)
    ordered.show(10)

//    val notBot = ordered.filter("login =!= '%bot%'")
//    notBot.show(20)

    val empPath = homeDir + "/Desktop/Learning-Spark/spark-in-action/app/src/main/resources/ghEmployees.txt" // 직원 파일 로드
    // Scala에서는 Set의 랜덤 룩업 성능이 Seq 컬렉션보다 빠르므로 Set을 사용하는 것이 좋다

    // scala의 for comprehensions
    val employees = Set() ++ { // Set() 메서드는 요소가 없는 불변 Set 객체를 생성한다. // ++ 메서드는 이 Set에 복수 요소를 추가한다
      for {
        line <- fromFile(empPath).getLines
      } yield line.trim
      // yield는 for 루프의 각 사이클 별로 값 하나를 임시 컬렉션에 추가한다. (이 경우에는 line.trim) 임시 컬렉션은 for 루프가 종료될 때 전체 for 표현식의 결과로 반환된 후 삭제된다.
    }

    // 공유 변수를 등록한다. 공유 변수는 클러스터의 각 노드에 정확히 한 번만 전송하고, 클러스터 노드의 메모리에 자동으로 캐시되므로 프로그램 실행 중에 바로 접근할 수 있다는 장점이 있다.
    // 공유 변수를 설정하지 않으면, 스파크는 employees Set을 대략 200회 (필터링 작업을 수행할 태스크 개수) 정도 반복적으로 네트워크에 전송하게 된다.
    // 공유 변수를 교환하는 프로토콜 = 가십 프로토콜 (워커 노드들이 소문을 퍼뜨리는 것 처럼 서로 공유 변수를 교환하는 행위)
    val bcEmployees = sc.broadcast(employees)

    // Spark의 User Defined Function 정의
//    val isEmp: (String => Boolean) = (arg: String) => employees.contains(arg)
//    val isEmp = user => employees.contains(user) // Scala의 type inference (타입 추론)

    import spark.implicits._
    // 공유 변수에 접근하기 위해 value 메소드를 써야 하므로, 변수 참조 부분을 변경한다
    val isEmp = user => bcEmployees.value.contains(user) // Scala의 type inference (타입 추론)

    // UDF를 SparkSession 클래스의 udf 메서드로 등록 -> 이렇게 등록하면 스파크 클러스터에서 UDF를 실행할 수 있다.
    // 스파크는 UDF에 필요한 객체를 모두 가져와서 클러스터에서 실행하는 모든 태스크에 전송한다.
    val isEmployee = spark.udf.register("isEmpUdf", isEmp)

    val filtered = ordered.filter(isEmployee($"login"))
    filtered.show()
  }

}
