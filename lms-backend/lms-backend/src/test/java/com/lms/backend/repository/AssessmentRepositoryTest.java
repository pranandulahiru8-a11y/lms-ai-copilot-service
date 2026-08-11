package com.lms.backend.repository;

import com.lms.backend.entity.Assessment;
import com.lms.backend.entity.Course;
import com.lms.backend.entity.Role;
import com.lms.backend.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AssessmentRepositoryTest {

        @Autowired
        private TestEntityManager entityManager;

        @Autowired
        private AssessmentRepository assessmentRepository;

        @Test
        @DisplayName("findByStudent: Should return assessments belonging to a specific student")
        void findByStudent_Success() {
                User student1 = User.builder()
                                .fullName("Student One")
                                .email("student1@lms.com")
                                .password("password123")
                                .role(Role.ROLE_STUDENT)
                                .build();

                User student2 = User.builder()
                                .fullName("Student Two")
                                .email("student2@lms.com")
                                .password("password123")
                                .role(Role.ROLE_STUDENT)
                                .build();

                entityManager.persist(student1);
                entityManager.persist(student2);

                Assessment assessment1 = Assessment.builder()
                                .title("Java Spring Assessment")
                                .quizDataJson("{}")
                                .student(student1)
                                .build();

                Assessment assessment2 = Assessment.builder()
                                .title("Angular Assessment")
                                .quizDataJson("{}")
                                .student(student2)
                                .build();

                entityManager.persist(assessment1);
                entityManager.persist(assessment2);
                entityManager.flush();

                List<Assessment> student1Assessments = assessmentRepository.findByStudent(student1);

                assertThat(student1Assessments).hasSize(1);
                assertThat(student1Assessments.get(0).getTitle()).isEqualTo("Java Spring Assessment");
        }

        @Test
        @DisplayName("findByCourse: Should return assessments linked to a specific course")
        void findByCourse_Success() {

                User instructor = User.builder()
                        .fullName("Prof. Perera")
                        .email("instructor@lms.com")
                        .password("password123")
                        .role(Role.ROLE_INSTRUCTOR)
                        .build();
                entityManager.persist(instructor);

                Course course = Course.builder()
                                .courseCode("CS101")
                                .title("Full Stack Web Development")
                                .instructor(instructor)
                                .build();

                entityManager.persist(course);

                Assessment assessment = Assessment.builder()
                                .title("Course Final Quiz")
                                .quizDataJson("{}")
                                .course(course)
                                .build();

                entityManager.persist(assessment);
                entityManager.flush();

                List<Assessment> courseAssessments = assessmentRepository.findByCourse(course);

                assertThat(courseAssessments).hasSize(1);
                assertThat(courseAssessments.get(0).getTitle()).isEqualTo("Course Final Quiz");
        }
}