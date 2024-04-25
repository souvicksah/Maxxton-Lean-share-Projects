package com.springbatch.config;

import java.io.FileNotFoundException;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import com.springbatch.domain.Student;
import com.springbatch.repository.StudentRepository;

@Configuration
public class BatchConfig {
  @Autowired
  private JobRepository jobRepository;
  @Autowired
  private StudentRepository studentRepository;
  @Autowired
  private PlatformTransactionManager platformTransactionManager;

  @Bean
  public FlatFileItemReader<Student> itemReader() {
    FlatFileItemReader<Student> itemReader = new FlatFileItemReader<>();
    itemReader.setResource(new FileSystemResource("D:/MyProjects/SpringBatchDemo2/Spring-Batch/src/main/resources/student.csv"));
    itemReader.setName("csvReader");
    itemReader.setLinesToSkip(1);
    itemReader.setLineMapper(lineMapper());
    return itemReader;
  }

  @Bean
  public StudentItemProcessor processor() {
    return new StudentItemProcessor();
  }

  @Bean
  public RepositoryItemWriter<Student> write() {
    RepositoryItemWriter<Student> repositoryItemWriter = new RepositoryItemWriter<>();
    repositoryItemWriter.setRepository(studentRepository);
    repositoryItemWriter.setMethodName("save");
    return repositoryItemWriter;
  }

  @Bean
  public Step buildStep() {
    return new StepBuilder("StudentStep", jobRepository)
        .<Student, Student>chunk(10, platformTransactionManager)
        .reader(itemReader())
        .processor(processor())
        .writer(write())
        .faultTolerant()
        .retryLimit(3)
        .retry(DeadlockLoserDataAccessException.class)
        .skipLimit(10)
        .skip(FlatFileParseException.class)
        .noSkip(FileNotFoundException.class)
        .noRollback(ValidationException.class)
        //.startLimit(1) --> to mention this step needs to run only once
        .build();
  }

  @Bean
  public Job runJob() {
    return new JobBuilder("importStudents", jobRepository)
        .start(buildStep())
        .build();
  }

  private LineMapper<Student> lineMapper() {
    DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();
    DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
    lineTokenizer.setDelimiter(",");
    lineTokenizer.setStrict(true);
    lineTokenizer.setNames("studentId", "name", "standard");

    BeanWrapperFieldSetMapper<Student> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
    fieldSetMapper.setTargetType(Student.class);

    lineMapper.setLineTokenizer(lineTokenizer);
    lineMapper.setFieldSetMapper(fieldSetMapper);
    return lineMapper;
  }

  @Bean
  public JobLauncher jobLauncher() throws Exception {
    TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
    jobLauncher.setJobRepository(jobRepository);
    jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
    jobLauncher.afterPropertiesSet();
    return jobLauncher;
  }

}
