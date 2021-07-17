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



