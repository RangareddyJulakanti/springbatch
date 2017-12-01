package com.cvs.example.springbatch.config;

import com.cvs.example.springbatch.mapper.ExamResultRowMapper;
import com.cvs.example.springbatch.model.ExamResult;
import com.cvs.example.springbatch.service.SendMailService;
import com.cvs.example.springbatch.service.SendMailTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

@Configuration
public class BatchConfig {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    @Autowired
    private DataSource dataSource;


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ExamResultRowMapper resultRowMapper() {
        return new ExamResultRowMapper();
    }

    @Bean
    public JdbcCursorItemReader databaseItemReader() {
        JdbcCursorItemReader reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("SELECT STUDENT_NAME, DOB, PERCENTAGE FROM public.\"EXAM_RESULT\"");
        reader.setRowMapper(resultRowMapper());
        return reader;
    }

    @Bean
    public BeanWrapperFieldExtractor<ExamResult> fieldExtractor() {
        BeanWrapperFieldExtractor<ExamResult> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"studentName", "percentage", "dob"});
        return fieldExtractor;
    }

    @Bean
    public DelimitedLineAggregator<ExamResult> delimitedLineAggregator() {
        DelimitedLineAggregator<ExamResult> delimitedLineAggregator = new DelimitedLineAggregator<>();
        delimitedLineAggregator.setDelimiter("|");
        delimitedLineAggregator.setFieldExtractor(fieldExtractor());
        return delimitedLineAggregator;
    }

    @Bean
    //  @Scope("step1")
    public FlatFileItemWriter<ExamResult> flatFileItemWriter() {
        FlatFileItemWriter<ExamResult> flatFileItemWriter = new FlatFileItemWriter<ExamResult>();
        flatFileItemWriter.setResource(new FileSystemResource("/csv/examResult.csv"));
        flatFileItemWriter.setLineAggregator(delimitedLineAggregator());
        return flatFileItemWriter;
    }

    @Bean
    public ItemWriter<ExamResult> writer() {
        FlatFileItemWriter<ExamResult> writer = new FlatFileItemWriter<ExamResult>();
        writer.setResource(new FileSystemResource("csv/examResult.csv"));
        DelimitedLineAggregator<ExamResult> delLineAgg = new DelimitedLineAggregator<>();
        delLineAgg.setDelimiter(",");
        BeanWrapperFieldExtractor<ExamResult> fieldExtractor = new BeanWrapperFieldExtractor<ExamResult>();
        fieldExtractor.setNames(new String[]{"studentName", "percentage", "dob"});
        delLineAgg.setFieldExtractor(fieldExtractor);
        writer.setHeaderCallback(header -> {
            header
                    .write("STUDENT_NAME");
            header
                    .append(",")
                    .write("PERCENTAGE");
            header
                    .append(",")
                    .write("DATE_OF_BIRTH");
        });
        writer.setLineAggregator(delLineAgg);
        return writer;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .transactionManager(transactionManager())
                .<String, String>chunk(1)
                .reader(databaseItemReader())
                .writer(writer())
                .build();
    }

    @Bean
    SendMailService sendMailService() {
        return new SendMailService();
    }

    @Bean
    SendMailTasklet sendMailTasklet() {
        SendMailTasklet sendMailTasklet = new SendMailTasklet();
        sendMailTasklet.setMailSender(mailSender());
        sendMailTasklet.setSendMailService(sendMailService());
        sendMailTasklet.setAttachmentFilePath(new FileSystemResource("\\csv\\examResult.csv").getFile().getAbsolutePath());
        sendMailTasklet.setRecipient("rangareddyjava9@gmail.com");
        sendMailTasklet.setSenderAddress("rangareddy35@gmail.com");
        return sendMailTasklet;
    }

    @Bean
    public Step sendMailStep() {
        return stepBuilderFactory.get("sendMailStep")
                .tasklet(sendMailTasklet())
                .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("examResultJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1())
                .next(sendMailStep())
                .end().build();


    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new org.springframework.batch.support.transaction.ResourcelessTransactionManager();
    }

    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        //Set gmail email id
        mailSender.setUsername("rangareddy35@gmail.com");
        //Set gmail email password
        mailSender.setPassword("XXXX");
        Properties prop = mailSender.getJavaMailProperties();
        prop.put("mail.transport.protocol", "smtp");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.debug", "true");
        prop.put("mail.smtp.timeout","600000");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.connectiontimeout","600000");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("rangareddy35@gmail.com", "XXXX");
                    }
                });
        mailSender.setSession(session);
        return mailSender;
    }

}
