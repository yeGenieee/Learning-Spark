# Hadoop

## Hadoop
- Apache 그룹의 프로젝트로 시작되어 발전한 대용량 데이터 처리를 위한 분산처리 프레임워크
- 구성의 핵심 요소
   1. Hadoop Common
       - 하둡의 다른 모듈을 지원하기 위한 공통 컴포넌트 모듈
   2. 하둡 분산 파일 시스템 (HDFS - Hadoop Distributed File System)
       - 분산 저장을 처리하기위한 모듈
       - 여러 개의 서버를 하나의 서버처럼 묶어서 데이터를 저장한다.
   3. Map Reduce
       - 분산되어 저장된 데이터를 병렬 처리할 수 있게 해주는 분산 처리 모듈
   4. Hadoop YARN
       - 병렬 처리를 위한 클러스터 자원관리 및 스케줄링 담당

## 등장배경
- 대용량 데이터를 처리하기 위한 분산 처리 시스템에서 출발하였다.
- Apache그룹의 Douglas Cutting에 의해 Lucene 하부 프로젝트로 시작되었다.

## Hadoop이 빅데이터 생태계의 중심이 되고 있는 이유
1. HDFS가 정형, 비정형 데이터 분석에 효율적이어서
   - 빅데이터 분석은 대용량의 데이터를 기반으로 이루어지는데, 하둡은 대용량 데이터를 저장할 수 있는 분산 파일 시스템인 HDFS를 제공하고 있다.
2. I/O 병목현상이 발생할 가능성이 없어서
   - 전통적인 시스템에서는 서버와 저장장치에서 I/O 병목 현상이 발생하기 쉬운데, 하둡은 클러스터링 기술을 통해 서버를 분산해서 I/O 병목 현상을 줄였다.  
3. 클러스터링 기술을 이용하므로 성능이 선형적으로 향상되어서
   - Scale-out 방식으로 수많은 서버를 단일화한 클러스터를 구성할 수 있다.
4. Hadoop은 오픈소스라서 기존 RDB Data warehouse 인프라에 비해 가격 측면에서 경제적이어서

위의 이유들로 인해 하둡 플랫폼은 대용량 데이터를 분산 처리하기 위한 시스템으로 각광받고 있다. 하둡 시스템에서는 64MB ~ 128MB 단위로 파일을 나누어 분산 저장하는 HDFS가 신뢰도 높은 파일 저장소 역할을 한다. 또한, HDFS는 Namenode와 Datanode로 양분되어 작동하기 때문에 시스템 확장을 위해 데이터 노드를 추가할 때 선형적으로 성능을 끌어올릴 수 있다.

## Hadoop의 특징
- 네트워크의 여러 서버에 존재하는 디스크들을 마치 하나의 디스크처럼 활용한다.
- 파일을 볼륨 기반으로 적재하거나 파일 단위로 저장하는 것이 아니라, 파일을 **Block**단위로 쪼개서 여러 서버에 분산, 저장한다.
- 하나의 클러스터를 구성하는데 수백에서 수 천 대 이상의 서버를 이용하므로 PB 이상의 데이터도 저장, 처리할 수 있다.

### 1. 손쉬운 확장
- 기존 스토리지는 미리 데이터 크기를 예측해서 구축해야 했고, 한 번 구축되고 나면 용량 확장과 이미 저장된 데이터를 분산시키는 추가 작업이 필요했다.
- 그러나, HDFS는 서버를 증설해서 datanode를 설치, 실행하면 스토리지 용량이 자동으로 늘어난다.
- 그리고, data rebalancing 명령을 실행하면, 기존 datanode에 몰려있던 블록 데이터를 적당한 비율로 모든 datanode에 재배치하는 기능도 있다.

### 2. Meta 정보의 중앙 집중적 관리
- 파일의 meta 정보는 namenode에서 중앙 집중적으로 관리하므로, datanode에 문제가 생기더라도 파일이 손실되지 않는다.
- File Redundancy를 피할 수 있다.
> Redundancy
> - 같은 속성을 가진 데이터가 여러 파일에 나타나는 것
> - 데이터 저장 공간 낭비를 초래한다.

### 3. 선형적인 Disk I/O 분산
- Namenode는 충분한 datanode가 있으면, 중복된 datanode 정보를 반환하지 않는다.
- 그래서, 서로 다른 datanode에 파일을 전송하게 된다.

### 4. 선형적인 네트워크 부하 분산
- 선형적인 디스크 입출력 분산에 의해 파일이 분산 저장되어 있기 때문에 datanode의 네트워크 부하가 분산되는 효과를 얻게된다.
- 파일 크기가 크면 블록으로 쪼개서 저장하므로 한 서버에 부하를 주지 않는다.

### 5. 안정적인 데이터 관리
- HDFS는 파일 복제본 개수를 온라인 상태에서도 dynamic하게 지정할 수 있다.
- Network topology 기반으로 복제본을 분산할 수 있어서 데이터의 안정성을 확보할 수 있다.
- ex) replica 수가 3이면, datanode 로컬 디스크에 1개, 동일한 랙에 있는 다른 datanode에 1개, 다른 랙에 있는 datanode에 1개를 저장하여 데이터 노드가 있는 랙 전체에 문제가 생기더라도 파일을 안정적으로 보관할 수 있다.

### 6. 저장하는 파일 수의 제한
- HDFS는 파일의 메타정보를 모두 namenode에서 관리하고, namenode는 이 정보를 모두 메모리에서 처리하기 때문에 namenode 서버의 메모리 크기만큼 파일 메타 정보를 관리한다는 단점이 있다.
- Hadoop v2.0에서는 이 단점을 해결하려고 `Namenode Federation` 구조를 추가했다.
  - 여러 개의 namenode에 namespace 정보를 분산시키는 방식으로, datanode는 공유하고 여러 개의 namenode를 풀로 관리하는 방식이다.

### 7. 파일 저장보다는 읽기가 많은 서비스에 적
- HDFS는 파일을 자주 저장하는 서비스보다 한 번 저장하고 자주 읽는 서비스에 적합하다.
- 왜냐하면, 하둡에 파일을 저장할 때 복제본 전송이 발생하여 네트워크와 디스크 자원을 많이 사용하기 때문이다.
- 그러나, 파일이 블록 단위로 분산되어 있고, 복제본도 존재하므로 파일을 읽을 때는 자원을 분산하여 특정 노드에 병목 현상이 발생하지 않는다.

### 8. Namenode 이중화 문제
- Hadoop v1.0에서는 하둡의 Namenode가 분산된 파일 블록의 위치 정보와 파일 메타 정보를 한 곳에서 관리함에 따라 SPOF 문제를 가지고 있다.
- Namenode에 장애 발생 시, 서비스 자체가 불가능하다는 단점을 가지고 있었는데, Hadoop v2.0에서 이를 해결했다.