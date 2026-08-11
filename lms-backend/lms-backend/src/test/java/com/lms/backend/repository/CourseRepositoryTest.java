package com.lms.backend.repository;

import com.lms.backend.entity.Course;
import com.lms.backend.entity.Role;
import com.lms.backend.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("findByInstructor: Should return courses associated with a specific instructor")
    void findByInstructor_Success() {
        // 1. Instructor සාදා Persist කිරීම
        User instructor = User.builder()
                .fullName("Dr. Perera")
                .email("perera@lms.com")
                .password("password123")
                .role(Role.ROLE_INSTRUCTOR)
                .build();
        entityManager.persist(instructor);

        // 2. වෙනත් Instructor කෙනෙක් සාදා Persist කිරීම
        User otherInstructor = User.builder()
                .fullName("Dr. Silva")
                .email("silva@lms.com")
                .password("password123")
                .role(Role.ROLE_INSTRUCTOR)
                .build();
        entityManager.persist(otherInstructor);

        // 3. Courses සාදා Persist කිරීම
        Course course1 = Course.builder()
                .courseCode("CS101")
                .title("Java Programming")
                .instructor(instructor)
                .build();

        Course course2 = Course.builder()
                .courseCode("CS102")
                .title("Spring Boot Microservices")
                .instructor(instructor)
                .build();

        Course course3 = Course.builder()
                .courseCode("CS201")
                .title("Data Structures")
                .instructor(otherInstructor)
                .build();

        entityManager.persist(course1);
        entityManager.persist(course2);
        entityManager.persist(course3);
        entityManager.flush();

        // 4. Test Assertion
        List<Course> instructorCourses = courseRepository.findByInstructor(instructor);

        assertThat(instructorCourses).hasSize(2);
        
        assertThat(instructorCourses)
        .map(course -> course.getCourseCode())
        .containsExactlyInAnyOrder("CS101", "CS102");
    }

    @Test
    @DisplayName("findByCourseCode: Should return course when valid course code is provided")
    void findByCourseCode_Success() {
        User instructor = User.builder()
                .fullName("Prof. Kamal")
                .email("kamal@lms.com")
                .password("password123")
                .role(Role.ROLE_INSTRUCTOR)
                .build();
        entityManager.persist(instructor);

        Course course = Course.builder()
                .courseCode("SE301")
                .title("Software Architecture")
                .instructor(instructor)
                .build();
        entityManager.persistAndFlush(course);

        Optional<Course> foundCourse = courseRepository.findByCourseCode("SE301");

        assertThat(foundCourse).isPresent();
        assertThat(foundCourse.get().getTitle()).isEqualTo("Software Architecture");
    }

    @Test
    @DisplayName("findByCourseCode: Should return empty optional when course code does not exist")
    void findByCourseCode_NotFound() {
        Optional<Course> foundCourse = courseRepository.findByCourseCode("INVALID_CODE");

        assertThat(foundCourse).isEmpty();
    }
}