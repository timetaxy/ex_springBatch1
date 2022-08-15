https://github.com/woniper/fastcampus-spring-batch-example/

run config
    create new config. application
    run application 지정
--spring.batch.job.names=helloJob
    특정 배치만 실행
job > step > tasklet

스프링배치 객체 관계
잡타입 빈이 생성되면 잡런가 잡실행, 잡이 스텝 실행
잡 레포지토리는 잡의 메타데이터 관리 ( 배치 전반적 관리 클래스 )

chunk : n개씩 나눠서 실행
task : 하나 작업기반 실행

chunk 기반 step 은 itemReader-만들거나 읽기, ItemProcessor-가공 필터링, ItemWriter-마지막 실행 데이터 최종 실행

메타테이블
batch job instance
잡이름, 잡키름으로 로우 생성
잡키는 파람 암호 저장
batch job execution
시작 종료 상태
batch job execution params
주입 파람
batch job execution
필요데이터, 실행 결과
batch job execution context - 스텝끼리 공유하지는 않는다
공유 데이터 직렬화 저장
batch step execution
batch step execution context

클래스 ExecutionContext 는 job, step 모두에 매핑 됨

메타테이블 스크립트 위치
    spring-batch-core/org.springframework/batch/core/*

스크립트 가져와서 데이터베이스 생성
yml 자동생성 속성 추가

active profiles 에 매개 변수가
실행할 application-[profile].yml 결정

## 동일 job, param은 재실행 될 수 없음

코드에서
.incrementer(new RunIdIncrementer())
시퀀셜하게 처리되게 하는 부분

job 끼리는 공유 됨
step 끼리는 공유 안 

--- chunk
chunk base 잡에서는 itemreader가 null 리턴할 때까지 반복
chunk size : itemWriter output list size

스프링 기본 스코프 싱글톤
생성 소멸
잡스코프-잡의 실행 종료, step에 선언
스텝스코프- 스템 실행 종료, tasklet, chunk(item reader processor writer) 에 선언

스코프 사용시
하위 itemwriter 등은 모두 @Bean 처리 되어야 함

같은 어노테이션
@Scope("job") = @JobScope
@Scope("step") = @StepScope

왜 스콥을 설정?
예 - 여러스텝에서 하나의 태스크렛 동시 엑세스시 스레드 세이
스프링익스프레션으로 파라메터 접근시, 라이프싸이클 관리되고 있어야 가능

스텝에서
//               <type, type> 은 인풋 아웃풋
.<String, String>chunk(StringUtils.isNotEmpty(chunkSize) ? Integer.parseInt(chunkSize) : 10)
--- itemReader
아이템리더를 구현
스프링 제공 파일, 데이터베이스 리더

ItemStream 내부 보면
ExecutionContext 파람, 아이템 스트림 구현체들이 상태를 관리 한다는 것

---
템플릿 생성(job, step 까지)

--- reader
null return > chunk 반복의 끝

--- flatFileItemReader
스프링 제공 csv 파일 리더

--- jdbc item reader : 커서와 페이징 차이 염두

jdbc resultset : java cursor 유사
    커서 이동하며 데이터 읽어 옴
아이템 스트림 인터페이스 - 오픈 업데이트 클로즈 등 스트림 
jdcc 아이템 리더에서 스트림 인터페이스 활용 - 오픈 업데이트 클로즈
    업데이트 매서드 종료 = null 리턴
커서 기반 
    커넥션 연결 시간이 길어진다는 단점
    하나의 커넥션 스레드 언세이프
    더 많은 메모리 필요
페이지 기반
    커넥션 시간 짧은, 커넥션 연결 리소스 소모
    스레드 세이프
    적은 메모리
아이템 리더 두가지
    쿼리프로바이더 스트링이 아닌 객체로 쿼리를 설정 가능
    jdbcCursorItemReader 
    jdbcPagingItemReader
        fetch-한번에 가져올 커서 사이즈 설정 가, 이 자체가 커서로 동작하는 것은 아니고 jdbcTemplate.fetchSize 가 커서 처럼 동작하는 것임
        page size 설정은 mysql.offset 설정과 유사
작업 : 아이템 리더 라이터 > 스텝 > 잡

--- jpa 아이템 리더
커스텀 아이템 리더는 사용할 일 거의 없음
entityManagerFactory - jpa 실행위해 entity manager가 필요
코드에서 ctrl+n 제네레이트
아이템 프로세서는 리더 라이터 보다 단순, 프로세서는 필수 아님
    정크에서는 아이템 라이터가 필수

---file 아이템 라이터
이미 구현된 구현체 있음
인풋타입은 아이템리더에서 리턴 > 프로세서 > 리스트를 라이터로 전달
    interface ItemWriter 의 writer 인풋이 List 인 이유
100까지 for 100.for

---jdbc 아이템 라이터
이름 jdbcBatchItemWriter
bulk insert 쿼리를 실행
    insert into aa (a,b,c) values (1,2,3), (4,5,6);
mysql 연결 url 에서
    rewriteBatchedStatements=true //bulk 인서트 위한 옵션
    jpa.hibernate.ddl-auto:update //운영에서는 none
active profile 로 properties 파일 선택

                EntityManager.Persist 사용하기 (수정대상인지 체크 select 쿼리 안함)
                persisttance exception 주의 ex) id auto gen 등
id 입력시 이미 있는데이터라 판단, update 하기 위해 select 쿼리 실행 됨
id 인서트시 입력 안하게 하면 insert 쿼리 실행 안 됨

id 직접 할당시 > usePersist(true)
id 직접 할당 안할 시 > usePersist 불필요

--- item processor
프로세스, 필터
리턴 null 은 해당 데이터는 writer에서 처리하지 않겠다는 것

allow_duplicate=true 모든 input 이 리턴 됨 - 중복체크 안 함
allow_duplicate=false 또는 null - 중복체크
    중복체크는 map 사용

chunk에서 item writer에 하나만 가능
2가지 필요시 - db, log 는
compositeItemWriter -- 한개씩 생성 후 순서대로 동작하도록
    순서나 중복 주의 해야 함

--- duplicatiion validate
//키풀에 존재하는 키인지 확, 존재하면 중복이므로 null 리턴
if (keyPool.containsKey(key)) {
return null;
}

---
스텝에 @JobScope // 잡파라메터를 스텝에서 이용하려면 필요

--- test
jobLauncher 는 job 실행
jobLauncherTestU

이전 데이터 삭제하기
@After
public void tearDown() throws Exception {
personRepository.deleteAll();
}

allowDuprication null = false
@SpringBatchTest //jobScope이 테스트코드에서 동작하게

--- 리스너
전후 실행 = 컨트롤러 전후의 인터셉터 유사
구현방법
    방법1. 인터페이스
    방법2. 어노테이션 사용

step 관련 모든 리스너는 stepListener를 상속

--- 스킵
스텝 예외처리
익셉션 허용 횟수 지정 가능 > status exit code complete
    chunk 단위로 트랜잭셔널하게 처리
    배치생성시 중복처리되지 않도록 고려해야 함

익셉션 간단하게는
public class NotFoundNameException extends RuntimeException {

배치상태보기 batch_step_execution

--- retry
재시도시 성공할 여지가 있을 때 사용
retryListener open > true > retryListener callback > error > retryListener onError

--- part4
entity enum 설정
@Enumerated(EnumType.STRING)
private Level level = level.NORMAL;

# 신규 prj 참고 : 4.2

# 테스트 필요 요소
@SpringBatchTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {UserConfiguration.class, TestConfiguration.class}) //테스트 대상과 테스트 config
public class ParallelUserConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

//이슈 : 배치 시간이 다음날 까지 걸치게 될 경우 (시작과 종료)

@Builder 는 NoArgsConstructor, AllArgsConstructor 와 같이

@JoinColumn(클래스 기준 n에 해당하는 클래스 필드) 없을 경우 연관관계 테이블 추가 생성 됨

writer의 파일생성은 chunk당이 아니라 step 당 실행

--- decider

코드설명
job 실행하면서 start 매서드 설정된 step이 차례로 실행 된다
JobParameterDecide 의 decide 매서드 통해 해당 조건에 따라 FlowExecutionStatus 가 리턴
    complete > 끝
    continue > 다음 .to 가 실행 됨

--- 성능 개선

코드 같더라도 고유한 이름의 빈 생성하기 위한 파라메터 화
private final String JOB_NAME = "userJob";

@Bean(JOB_NAME)
public Job userJob() throws Exception {

@Bean(JOB_NAME + "_orderStatisticsStep")

--- async step
아이템 프로세서와 라이터 기준으로 비동기처리

사용하려면 스프링 배치 integration 의존성 추가해야 함
<!-- https://mvnrepository.com/artifact/org.springframework.batch/spring-batch-integration -->
<dependency>
    <groupId>org.springframework.batch</groupId>
    <artifactId>spring-batch-integration</artifactId>
    <version>4.3.6</version>
</dependency>

processor 는 리더에서 받은 인풋을 자바 concurrent feature 로 래핑

ItemReader 은 동일
AsyncItemProcessor, AsyncItemWriter
1. async 는 종료 안되는 경우가 있으므로, main에 System.exit  추가 필요

2. application에 쓰레드풀 익스큐터 생성
내가 커스텀한 빈을 먼저 사용하려면
@Primary

3. User 에서 fetch 전략 eager

--- 멀티쓰레드 step
정크기준으로 멀티쓰레드처리

여러 쓰레드가 정크 단위로 동시 처하기 때문에 쓰레드 세이프한 아이템리더 필수
    asyncStep 은 ItemProcessor, writer 만 비동기 처리
페이징 기반은 쓰레드 세이프, 커서는 세이프 x
jpa 페이지 아이템 리더 > 쓰레드 세이프

스텝에서 추가
.taskExecutor(this.taskExecutor)
.throttleLimit(8)

--- 파티션 스텝
스텝기준으로 멀티쓰레드처리

파티셔너 생성
    max, min 위한 Repo 함수 생성
    @Query(value = "select min(u.id) from User u")
    long findMinId();

성능 개선은 테스트로 실제 해봐야 알 수 있음

1. 파티셔너의 파티션 매서드에서 각 그릴사이즈만큼의 스텝에서 사용될 범위의 민 맥스 구해서 익스큐션 컨텍스트를 맵에 담는다
2. 슬레이브 스텝에서 실행될 때, 아이템리더에서 사용될 건데, 민 맥스로 조회 조건문 아이템 리더에 파람 추가
3. 아이템리더 .queryString("select u from User u where u.id between :minId and :maxId"),  .parameterValues(parameters)
4. 핸들러 생성 taskExecutorPartitionHandler
5. 매니저 생성 userLevelUpManagerStep, 매니저에 핸들러 연결 .partitionHandler(taskExecutorPartitionHandler())
6. 스텝에 매니저 연결, .next(this.userLevelUpManagerStep())
7. async 튜닝, itemWriter itemReader async 로

--- 패럴럴 스텝
하나의 잡에 설정된 n개의 스텝을 병렬로 실행
핵심 splitFlow
스텝 자체를 동시실행 (멀티스레드-정크단위, 파티션 스텝은 스텝단위로 동시 실행)
    스텝단위 인 것은 공통(패러럴,파티션)
    파티션스텝: 하나의 마스터 스텝을 여러개 슬레이브로
    패러럴 : 잡에 설정된 n개 병렬

1. executer 멤버 추가
2. saveUserFlow 구현, saveuserStep 수정해서 구현, flow로 랩핑 리턴
3. userJob flow로 start 수정
4. orderStatisticsStep bean, jobScope 어노테이션 제거 - splitflow가 잡스코프에서 파람을 받기 때문에 order stat~ flow를 생성하기 때문에
5. orderStatisticsFlow 생성
6. splitFlow 생성, // userLevelUpStep 과 orderStatFlow 를 하나의 flow 스텝으로 만듦, // flow로 합치려면 각각의 스텝도 flow 이어야 함
7. job에 추가 .next(this.splitFlow(null))

파티션 스텝 추가하기
8. 파티션 참고 (아이템리더, 마스터스텝, 핸들러) 
9.                 .start(userLevelUpManagerStep()) //                파티션으로 튜닝, 마스터 스텝 입력
10. @Bean(JOB_NAME + "_userItemReader") 빈 이름 지정( 지정 않으면 매서드 명으로 빈 됨으로 )

--- 튜닝 성능 비교
User40000 건 저장, ChunkSize 1000
생능개선 대상 : userLevelUpStep 
성능 순위
1. 멀티쓰레드
2. partition
3. async + partition
4. partition + paraller
5. paraller

--- 배포 실행




