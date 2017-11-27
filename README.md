
Reference :

1.https://spring.io/guides/gs/batch-processing/

2.https://spring.io/guides/gs/scheduling-tasks/



# SpringBootWithBatchScheduling

# Add oracle database credentials in application.properties like the below

spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE

spring.datasource.username=system

spring.datasource.password=system

spring.datasource.driver-class-name=oracle.jdbc.driver.OracleDriver


server.port=1234


#creates tables automatically
spring.jpa.generate-ddl=true
