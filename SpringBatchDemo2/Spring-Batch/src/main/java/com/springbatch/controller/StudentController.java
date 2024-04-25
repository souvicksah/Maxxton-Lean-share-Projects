package com.springbatch.controller;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.springbatch.domain.Student;
import com.springbatch.service.StudentService;

@RestController
public class StudentController {

  @Autowired
  private JobLauncher jobLauncher;
  @Autowired
  private Job job;
  @Autowired
  private StudentService studentService;

  @Autowired
  private JobOperator jobOperator;
  @Autowired
  private JobExplorer jobExplorer;

  @GetMapping("/maxxton/v2/students")
  public List<Student> getAllStudents() {
    return studentService.getStudents();
  }

  @PostMapping("/maxxton/v2/students")
  public void createStudent(@RequestBody Student student) {
    studentService.createStudent(student);
  }

  //creating one job
  @PostMapping("/maxxton/v2/students/import")
  public JobExecution importAllStudents() {
    JobParameters jobParameter = new JobParametersBuilder().addLong("startAt", System.currentTimeMillis()).toJobParameters();

    try {
      return jobLauncher.run(job, jobParameter);
    }
    catch (JobExecutionAlreadyRunningException | JobParametersInvalidException | JobInstanceAlreadyCompleteException | JobRestartException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  //stopping one job
  @PutMapping("maxxton/v1/students/import/stop/{executionId}")
  public boolean stopJob(@PathVariable Long executionId) throws NoSuchJobExecutionException, JobExecutionNotRunningException {
    return jobOperator.stop(executionId);
  }

  //getting job Execution details

  @GetMapping("/maxxton/v2/students/executions/{executionId}")
  public JobExecution getJobExecutionById(@PathVariable Long executionId) {
    return jobExplorer.getJobExecution(executionId);
  }
}
