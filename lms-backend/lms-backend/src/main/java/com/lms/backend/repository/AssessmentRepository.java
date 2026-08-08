package com.lms.backend.repository;

import com.lms.backend.entity.Assessment;
import com.lms.backend.entity.Course;
import com.lms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByStudent(User student);
    List<Assessment> findByCourse(Course course);
}