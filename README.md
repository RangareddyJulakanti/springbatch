
Reference :

1.https://spring.io/guides/gs/batch-processing/

2.https://spring.io/guides/gs/scheduling-tasks/



# SpringBootWithBatchScheduling

# Add postgresql database credentials in application.properties like the below

spring.datasource.url=jdbc:postgresql://localhost:5432/postgres

spring.datasource.username=postgres

spring.datasource.password=postgres

spring.datasource.driver-class-name=org.postgresql.Driver

server.port=1234

#creates tables automatically

spring.jpa.generate-ddl=true


server.port=1234


#creates tables automatically
spring.jpa.generate-ddl=true


#create table in database
------------------------
-- Table: public."EXAM_RESULT"

-- DROP TABLE public."EXAM_RESULT";

CREATE TABLE public."EXAM_RESULT"
(
    dob date,
    percentage double precision,
    student_name character varying COLLATE pg_catalog."default"
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public."EXAM_RESULT"
    OWNER to postgres;
