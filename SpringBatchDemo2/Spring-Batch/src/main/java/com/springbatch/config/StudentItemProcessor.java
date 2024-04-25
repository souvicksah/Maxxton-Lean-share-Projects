package com.springbatch.config;

import org.springframework.batch.item.ItemProcessor;

import com.springbatch.domain.Student;

public class StudentItemProcessor implements ItemProcessor<Student, Student> {
  @Override
  public Student process(Student item) throws Exception {

    return item;
  }
}
