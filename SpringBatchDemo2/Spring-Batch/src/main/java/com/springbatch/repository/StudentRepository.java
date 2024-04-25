package com.springbatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springbatch.domain.Student;

public interface StudentRepository extends JpaRepository<Student,Long> {
}
