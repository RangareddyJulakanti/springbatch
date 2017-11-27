package com.cvs.example.springbatch.config;

import com.cvs.example.springbatch.mapper.ExamResultRowMapper;
import com.cvs.example.springbatch.model.ExamResult;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

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
        fieldExtractor.setNames(new String[] {"studentName", "percentage", "dob"});
        delLineAgg.setFieldExtractor(fieldExtractor);
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
    public Job job() {
        return jobBuilderFactory.get("examResultJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1())
                .end().build();


    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new org.springframework.batch.support.transaction.ResourcelessTransactionManager();
    }

}
