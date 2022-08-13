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







-- 진행
fc 2-4 04:15
