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


-- 진행
fc 2-4 04:15
