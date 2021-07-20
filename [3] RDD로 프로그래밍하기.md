# [3] RDD로 프로그래밍하기

### 학습 목표

- 스파크의 데이터 작업을 위한 핵심 개념인 RDD (Resilient Distributed Dataset) 에 대해 알 수 있다.
- RDD 생성, 연산을 할 수 있다.
- 트랜스포메이션과 액션에 대해 알 수 있다.



## 1. RDD 기초

  RDD는 분산되어 존재하는 데이터 요소들의 모임이다. 스파크에서 모든 작업은 새로운 RDD를 만들거나, 존재하는 RDD를 변형하거나, 결과 계산을 위해서 RDD에서 연산을 호출하는 것 중의 하나로 표현된다. 내부적으로 스파크는 자동으로 RDD에 있는 데이터들을 클러스터에 분배하며 클러스터 위에서 수행하는 연산들을 병렬화한다.



### RDD

- 분산되어 있는 변경 불가능한 객체 모음

- 각 RDD는 클러스터의 서로 다른 노드들에서 연산 가능하도록 여러 개의 **파티션 (partition)** 으로 나뉨

- RDD는 사용자 정의 클래스를 포함해서 파이썬, 자바, 스칼라의 어떤 타입의 객체든 가질 수 있음

- RDD는 외부 데이터세트를 로드하거나 드라이버 프로그램에서 객체 컬렉션을 분산시키는 두 가지 방법 중의 하나로 만들 수 있음

  - ex) SparkContext, textFile() 을 써서 텍스트 파일을 문자열 RDD로 로딩

    ```scala
    lines = sc.textFile("README.md")
    ```

- 한 번 만들어진 RDD는 두 가지 타입의 연산을 지원
  1. transformation
     - 존재하는 RDD에서 새로운 RDD를 만들어냄
     - ex) filter()
  2. action
     - RDD를 기초로 결과 값을 계산하며 그 값을 드라이버 프로그램에 되돌려주거나 외부 스토리지에 저장
     - ex) first()

  트랜스포메이션과 액션은 위와 같이 스파크가 RDD를 다루는 방식에서 서로 차이가 있다. 스파크는 RDD를 항상 lazy evaluation으로 처음 액션을 사용하는 시점에 처리한다. 즉, `sc.textFile()` 시에 텍스트 파일의 모든 라인을 로드해서 저장해놓는 것이 아닌, `first()` 와 같은 액션을 사용하는 시점에 처음 일치하는 라인이 나올 때 까지만 파일을 읽게 되는 것이다.

- 스파크의 RDD는 기본적으로 액션이 실행될 때 마다 매번 새로운 연산을 한다
  - 여러 액션에서 RDD를 재사용하고 싶다면, 스파크에게 `RDD.persist()` 를 통해 계속 결과를 유지하도록 요청한다
  - RDD를 여러 메모리, 디스크 등에 데이터를 보존해주도록 스파크에 요청할 수도 있다
  - 첫 연산이 이루어진 후, 스파크는 RDD의 내용을 클러스터의 여러 머신들에 나눠서 메모리에 저장하게 되며, 이후의 액션들에서 재사용하게 된다
- RDD를 재사용하지 않는 것은 스파크가 일회성 데이터를 가져와 결과만 계산하고 데이터를 굳이 저장할 필요가 없는 경우에 굉장히 유용하다



### 모든 스파크 프로그램과 셸의 세션 동작 순서

1. 외부 데이터에서 입력 RDD를 만든다
2. `filter()` 와 같은 트랜스포메이션을 사용하여 새로운 RDD를 정의한다
3. 재사용을 위한 중간 단계의 RDD를 보존하기 위해 `persist()` 로 스파크에 요청한다
4. 스파크가 최적화 한 병렬 연산 수행을 위해 `count()` 나 `first()` 같은 액션을 시작한다



## 2. RDD 생성하기

  스파크에서는 RDD를 만드는 두 가지 방법을 아래와 같이 제공한다.

1. 외부 데이터세트 로드
2. 직접 만든 드라이버 프로그램에서 데이터 집합을 병렬화하기

 

- RDD를 만드는 가장 간단한 방법
  - 프로그램에 있는 데이터세트를 가져다가 `SparkContext` 의 `parallelize()` 메소드에 넘겨주기

### parallelize()

- 셸에서 자신만의 RDD 를 만들 수 있고, 연산을 수행할 수 있음
- 그러나, 하나의 머신 메모리에 모든 데이터세트를 담고 있으므로 프로토타이핑이나 테스팅 목적이 아니면 널리 쓰이지 않음

#### 스칼라에서의 parallelize() 메소드

```scala
val lines = sc.parallelize(List("pandas", "i like pandas"));
```



#### 자바의 parallelize() 메소드

```java
JavaRDD<String> lines = sc.parallelize(Arrays.asList("pandas", "i like pandas"))
```



### 외부 스토리지에서 데이터를 불러와서 RDD 생성

#### 스칼라에서의 textFile() 메소드

```scala
val lines = sc.textFile("/path/to/README.md");
```

#### 자바에서의 textFile() 메소드

```java
JavaRDD<String> lines = sc.textFile("/path/to/README.md");
```



## 3. RDD의 연산

  RDD는 두 가지 타입의 연산 작업을 지원한다.

### 1. transformation

- 새로운 RDD를 만들어 내는 연산
- RDD를 리턴
- ex) map(), filter()

### 2. action

- 드라이버 프로그램에 결과를 되돌려주거나 스토리지에 결과를 써 넣는 연산
- RDD 이외의 다른 데이터 타입을 리턴
- ex) count(), first()



### 1. Transformation

- 새로운 RDD를 만들어 리턴해주는 RDD의 연산 방식
- lazy evaluation에 따라 트랜스포메이션된 RDD는 실제로 액션을 사용하는 늦은 시점에 계산됨
- element wise (데이터 요소 위주), 즉, 한 번에 하나의 요소에서만 작업이 이루어짐
  - 모든 트랜스포메이션이 그런 것은 아님

#### 로그 파일에서 에러 메시지만 필터링하는 예제

#### 스칼라에서의 filter() transformation

```scala
val inputRDD = sc.textFile("log.txt")
val errorsRDD = inputRDD.filter(line => line.contains("error"))
```

#### 자바에서의 filter() transformation

```java
JavaRDD<String> inputRDD = sc.textFile("log.txt");
JavaRDD<String> errorsRDD = inputRDD.filter(
	new Function<String, Boolean>() {
		public Boolean call(String x) {
			return x.contains("error");
		}
	}
);
```

- filter() 연산은 이미 존재하는 inputRDD를 변경하지는 않는다 (RDD는 변경 불가능하다)
  - RDD 자체를 변경하는 것이 아니라, 완전히 새로운 RDD에 대한 포인터를 리턴한다
  - 따라서, inputRDD는 프로그램 내에서 재사용 가능하다

##### inputRDD를 다시 사용하여 두 결과를 union() 하는 예제

```scala
val errorsRDD = inputRDD.filter(line => line.contains("error"))
val warningRDD = inputRDD.filter(line => line.contains("warning"))

val badLinesRDD = errorsRDD.union(warningsRDD)
```

- union() 은 두 개의 RDD로 작업한다
- 트랜스포메이션은 입력할 수 있는 RDD 개수에 대한 제한이 없다

  각 트랜스포메이션을 적용해서 새로운 RDD를 얻어내면, 스파크는 각 RDD에 대해 `lineage graph` 라 불리는 관계 그래프를 갖고 있게 된다. 스파크는 이 정보를 활용해서 필요 시, 각 RDD를 재연산하거나 저장된 RDD가 유실될 경우 복구를 하는 등의 경우에 활용한다



### 2. Action

- 드라이버 프로그램에 최종 결과 값을 돌려주거나 외부 저장소에 값을 기록하는 연산 작업
- 실제로 결과 값을 내야 하므로 트랜스포메이션이 계산을 수행하도록 강제함

#### 로그 파일에서 badLinesRDD 에 대한 정보들을 출력하는 예제

#### 액션을 사용하여 스칼라에서 에러 세기

```scala
println("Input had " + badLinesRdd.count() + " concerning lines")
println("Here are 10 examples: ")
badLinesRDD.take(10).foreach(println)
```

#### 액션을 사용하여 자바에서 에러 세기

```java
System.out.println("Input had " + badLinesRDD.count() + "concerning lines");
System.out.println("Here are 10 examples: ");
for (String line : badLinesRDD.take(10)) {
	System.out.println(line);
}
```

- take()
  - RDD의 데이터 일부를 가져오기 위함
- collect()
  - 전체 RDD 데이터를 가져옴
  - RDD를 filter()에 의해 작은 크기의 데이터세트의 RDD로 만든 후 분산이 아닌 로컬에서 데이터를 처리하고 싶을 때 유용함
  - 이 함수를 사용할 때는, 전체 데이터세트가 사용하는 단일 컴퓨터의 메모리에 올라올 수 있을 정도의 크기여야 함
  - 데이터세트가 너무 크면 collect()를 사용할 수 없다
  - RDD가 드라이버 프로그램에 의해 collect()가 불가능한 경우
    - 대부분 데이터가 너무 크기에 collect()를 사용할 수 없다
    - 위의 경우, HDFS나 AWS S3 같은 분산 파일 시스템에 데이터를 써서 해결함
      - RDD의 내용들은 saveAsTextFile() 또는 saveAsSequenceFile() 등의 파일 포맷용 액션을 써서 저장 가능하기 때문

- 새로운 액션을 호출할 때마다 RDD가 **처음부터 (from scratch) 계산**됨
  - 이를 피하려면 중간 결과를 영속화 (persist) 를 이용



### 3. Lazy Evaluation

  RDD의 트랜스포메이션은 **lazy** 방식으로 처리가 된다. 이 의미는 스파크가 액션을 만나기 전까지는 **실제로 트랜스포메이션을 처리하지 않는다**는 말이다. 

- Lazy Evaluation 이란 RDD에 대한 트랜스포메이션을 호출할 때 그 연산이 즉시 수행되는 것이 아니고
- 대신 내부적으로 스파크는 metadata에 이러한 트랜스포메이션 연산이 호출되었다는 것만 기록을 해둔다
- RDD가 실제로는 어떤 특정 데이터를 가지고 있는 것이 아닌, 트랜스포메이션들이 생성한 데이터를 **어떻게 계산할 지에 대한 명령어들을 갖고 있다**고 생각하면 됨
- RDD에 데이터를 로드하는 것도 트랜스포메이션과 마찬가지로 lazy evaluation
  - sc.textFile() 호출 시, 실제로 필요한 시점이 되기 전까지는 로딩되지 않음
- 왜 lazy evaluation 을 이용?
  - 데이터 전달 횟수를 줄이기 위해 이용
  - 하둡 맵리듀스 같은 시스템에서는 맵리듀스의 데이터 전달 횟수를 줄이기 위해 어떤 식으로 연산을 그룹화할지 고민해야 하는 것이 포인트인데, 맵리듀스 에서는 연산 개수가 많다는 것은 즉 네트워크로 데이터를 전송하는 단계가 많아짐을 의미하기 때문이다



## 4. 스파크에 함수 전달하기

  대부분의 트랜스포메이션과 액션 일부는 스파크가 실제로 연산할 때 쓰일 함수들을 전달해야 하는 구조를 가진다.



### 스칼라

- 다른 함수형 API 처럼 인라인으로 정의된 함수나 메소드에 대한 참조, 정적 함수를 전달할 수 있음
- 주의사항
  - 전달하는 함수나 참조하는 데이터들이 직렬화 (serialize) 가능해야 함
  - 객체의 메소드나 필드를 전달하면 전체 객체에 대한 참조 또한 포함됨

#### 스칼라에서의 함수 전달

```scala
class SearchFunctions(val query: String) {
	def isMatch(s: String): Boolean = {
		s.contains(query)
	}
  
  def getMatchesFunctionReference(rdd: RDD[String]): RDD[Boolean] = {
    // 문제: "isMatch"는 "this.isMatch" 이므로 this의 모든 것이 전달된다
    rdd.map(isMatch)
  }
	
	def getMatchesFieldReference(rdd: RDD[String]): RDD[Array[String]] = {
    // 문제: "query"는 "this.query" 이므로 this의 모든 것이 전달된다
    rdd.map(x => x.split(query))
	}
  
  def getMatchesNoReference(rdd: RDD[String]): RDD[Array[String]] = {
    // 언존허미 필요한 필드만 추출하여 지역 변수에 저장해 할당한다
    val query_ = this.query
    rdd.map(x => x.split(query_))
  }
}
```

- 스칼라에서 `NotSerializableException` 이 발생하는 것은
  - 직렬화 불가능한 클래스의 메소드나 필드를 참조하는 문제일 가능성이 높다
  - 최상위 객체의 멤버인 지역 변수나 함수 내에서 전달하는 것은 항상 안전하다



### 자바

- 자바에서 함수들은 `org.apache.spark.api.java.function` 패키지의 스파크 함수 인터페이스들을 구현한 객체가 된다
- 함수의 반환 타입에 따라 여러 개의 인터페이스들이 아래와 같이 있다

#### 기본 자바 함수 인터페이스

| 함수 이름             | 구현할 메소드         | 사용법                                                       |
| --------------------- | --------------------- | ------------------------------------------------------------ |
| Function<T, R>        | R call(T)             | 입력 하나를 받아 출력 하나를 되돌려줌<br />map() 이나 filter() 같은 연산에 씀 |
| Function2<T1, T2, R>  | R call(T1, T2)        | 입력 두 개를 받아 하나의 출력을 되돌려 줌<br />aggregate()나 fold() 같은 연산에 씀 |
| FlatMapFunction<T, R> | Iterable< R > call(T) | 하나의 입력을 받아 0개 이상 여러 개의 출력을 되돌려줌<br />flatMap() 같은 연산에 씀 |

  함수 클래스를 만들기 위해서는 익명 내부 클래스를 인라인으로 만들거나, 이름 있는 클래스를 따로 만들 수 있다.

#### 자바에서 익명 내부 클래스로 함수 전달하기

```java
RDD<String> errors = lines.filter(new Function<String, Boolean>() {
	public Boolean call(String x) {
		return x.contains("error");
	}
});
```

#### 자바에서 이름 있는 클래스로 함수 전달하기

```java
class ContainsError implments Function<String, Boolean>() {
	public Boolean call(String x) {
		return x.contains("error");
	}
}

RDD<String> errors = lines.filter(new ContainsError());
```

- 대규모 프로그램을 작성하는 경우 최상위 레벨의 이름 있는 함수 클래스를 쓰는 것이 코드 가독성이 높음

#### 인자를 가지는 자바 함수 클래스

```java
class ContainsError implements Function<String, Boolean>() {
	private String query;
	
	public Contains(String query) {
		this.query = query;
	}
	
	public Boolean call(String x) {
		return x.contains(query);
	}
}

RDD<String> errors = lines.filter(new Contains("error"));
```



#### 자바 8의 람다 표현식을 사용한 함수 전달

```java
RDD<String> errors = lines.filter(s -> s.contains("error"));
```

 

## 5. 많이 쓰이는 트랜스포메이션과 액션

  스파크에서 가장 흔하게 쓰이는 트랜스포메이션과 액션들에 대해 알아보자. 

특별한 데이터 타입을 취급하는 RDD를 위한 추가적인 연산들도 존재한다.

- 통계 함수들이나 key / value pair를 다루는 RDD에서 key를 기준으로 데이터를 집계하는 key / value 연산 같은 것들이 있다



### 기본 RDD

- 데이터에 상관없이 모든 RDD에 대해 적용할 수 있는 트랜스포메이션과 액션들에 대해 먼저 다뤄보자



#### 데이터 요소 위주 트랜스포메이션

- map()
- filter()

#### map()

- 함수를 받아 RDD의 각 데이터에 적용하고 결과 RDD에 각 데이터의 새 결과 값을 담는다
- 반환 타입이 입력 타입과 같지 않아도 됨
  - ex) RDD 문자열을 갖고 있고, map()이 문자열을 Double 타입으로 리턴한다면, 
    - 입력 RDD : RDD[String]
    - 리턴 RDD : RDD[Double]

#### 스칼라에서 map() 으로 RDD의 값들을 제곱하기

```scala
val input = sc.parallelize(List(1, 2, 3, 4))
val result = input.map(x => x * x)
println(result.collect().mkString(","))
```

#### 자바에서 map() 으로 RDD의 값들을 제곱하기

```java
JavaRDD<Integer> rdd = sc.parallelize(Arrays.asList(1, 2, 3, 4));
JavaRDD<Integer> result = rdd.map(new Function<Integer, Integer>() {
  public Integer call(Integer x) {
    	return x*x;
  }
});

System.out.println(StringUtils.join(result.collect(), ","));
```

#### filter()

- 함수를 받아 filter() 함수를 통과한 데이터만 RDD에 담아 리턴한다



#### flatMap()

- 각 입력 데이터에 대해 여러 개의 아웃풋 데이터를 생성해야 할 때 이용
- map()과 같이 flatMap()에 전달한 함수는 입력 RDD에서 각 데이터마다 호출됨
  - 함수에서 단일 값을 리턴하는 대신, 반복자 (iterator)를 리턴해야 함
    - 반복자가 포함된 RDD를 리턴받는 것은 아니고, 반복자가 생성한 데이터들이 담긴 RDD를 받게 됨

#### 스칼라에서 여러 라인을 단어로 분해하는 flatMap() 의 사용 예제

```scala
val lines = sc.parallelize(List("hello world", "hi"))
val words = lines.flatMap(line => line.split(" "))
words.first() // "hello" 를 반환
```

#### 자바에서 여러 라인을 단어로 분해하는 flatMap()의 사용 예제

```java
JavaRDD<String> lines = sc.parallelize(Arrays.asList("hello word", "hi"));
JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
  public Iterable<String> call(String line) {
    return Arrays.asList(line.split(" "));
  }
});
words.first(); // "hello" 를 반환
```

- flatMap()은 반환 받은 iterator들을 펼쳐놓는다
  - 여러 개의 리스트로 구성된 RDD 대신, 그 각각의 리스트 안에 있는 원소로 구성된 RDD를 반환

### 가상 집합 연산

  RDD는 합집합, 교집합 같은 다양한 수학적 집합 연산을 지원한다. 심지어 RDD가 집합의 형태가 아닌 경우에도 지원한다. 4가지 집합 연산은 연산에 포함된 RDD가 서로 같은 타입이어야 한다.

보통 RDD에서 가장 빈번하게 요구되는 집합의 특성은 중복 데이터가 자주 생기므로 uniqueness가 가장 필요하다. 만약, 중복이 없는 데이터세트를 원한다면, RDD.distinct() 트랜스포메이션을 써서 오직 단일 데이터 요소만 포함한 새로운 RDD를 얻을 수 있다. 그러나, distinct() 는 단일 아이템인지를 비교하기 위해 네트워크로 데이터를 전송해서 비교해야 하므로 연산의 cost가 매우 든다. 이런 shuffling 작업과 이를 피하는 방법은 나중에 알아보자.

#### union()

- 양쪽의 데이터를 합해서 되돌려 줌 (합집합)
- 다양한 경로에서 받은 로그들을 합치는 데 사용
- 원본 데이터들이 중복 되더라도 중복을 유지함

#### intersection() 

- 양쪽 RDD에 동시에 존재하는 요소만 되돌려 줌 (교집합)
- 동작하면서 모든 중복을 제거함
  - 단일 RDD 안에 원래 존재하던 중복은 포함
- 중복을 찾기 위해 distinct() 처럼 셔플링이 수반되므로, union() 보다 성능이 훨씬 떨어짐

#### subtract()

- 첫 번째 RDD의 항목 중 두 번째 RDD에 있는 항목을 제외한 항목들을 가진 RDD를 되돌려줌 (차집합) 
- 셔플링을 수반되므로 성능이 떨어짐

#### cartesian() (cartesian product, 카티시안 곱)

- 첫 번째 RDD에 있는 데이터 a와 두 번째 RDD에 있는 데이터 b에 대해 모든 가능한 쌍 (a, b) 를 리턴
- `select * from a,b`
- 모든 사용자들에 대해 가능한 쌍들에 대한 유사성을 파악하고 싶은 경우에 이용
- 동일 RDD에 대한 카티시안 곱도 가능
  - 사용자 유사성을 위한 작업에 이용 가능
- cost가 매우 크다

#### {1, 2, 3, 3} 을 갖고 있는 RDD에 대한 기본 RDD 트랜스포메이션

| 함수 이름                                 | 용도                                                         | 예시                      | 결과                  |
| ----------------------------------------- | ------------------------------------------------------------ | ------------------------- | --------------------- |
| map()                                     | RDD의 각 요소에 함수를 적용하고 결과 RDD를 되돌려 줌         | rdd.map(x => x + 1)       | {2, 3, 4, 4}          |
| flatMap()                                 | RDD의 각 요소에 함수를 적용하고 반환된 반복자의 내용들로 이루어진 RDD를 되돌려 줌<br />종종 단어 분해를 위해 쓰임 | rdd.flatMap(x => x.to(3)) | {1, 2, 3, 2, 3, 3, 3} |
| filter()                                  | filter()로 전달된 함수의 조건을 통과한 값으로만 이루어진 RDD를 되돌려 줌 | rdd.filter(x => x != 1)   | {2, 3, 3}             |
| distinct()                                | 중복 제거                                                    | rdd.distinct()            | {1, 2, 3}             |
| sample(withReplacement, fraction, [seed]) | 복원 추출(withReplacement=true)이나 비복원 추출로 RDD에서 표본을 뽑아낸다 | rdd.sample(false, 0.5)    | 생략                  |



#### {1, 2, 3}과 {3, 4, 5}를 가진 두 RDD에 대한 트랜스포메이션

| 함수 이름      | 용도                                                         | 예시                    | 결과                               |
| -------------- | ------------------------------------------------------------ | ----------------------- | ---------------------------------- |
| union()        | 두 RDD에 있는 데이터들을 합한 RDD를 생성한다                 | rdd.union(other)        | {1, 2, 3, 3, 4, 5}                 |
| intersection() | 양쪽 RDD에 모두 있는 데이터들만을 가진 RDD를 반환한다        | rdd.intersection(other) | {3}                                |
| subtract()     | 한 RDD가 가진 데이터를 다른쪽에서 삭제한다<br />ex) 교육용으로 썼던 데이터 삭제 | rdd.subtract(other)     | {1, 2}                             |
| cartesian()    | 두 RDD의 카티시안 곱                                         | rdd.cartesian(other)    | {(1,3), (1,4), (1,5), ... , (3,5)} |



### 액션

