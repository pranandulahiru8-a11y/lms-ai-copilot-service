package com.lms.backend.repository;

import com.lms.backend.entity.AssessmentResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AssessmentResultRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AssessmentResultRepository assessmentResultRepository;

    @Test
    @DisplayName("save: Should successfully persist evaluation result details")
    void save_Success() {
        AssessmentResult result = AssessmentResult.builder()
                .question("Explain Dependency Injection")
                .studentAnswer("DI is a design pattern used to achieve Inversion of Control.")
                .referenceAnswer("IoC pattern that injects object dependencies.")
                .scoreOutOf10(9)
                .feedbackForStudent("Clear and accurate response.")
                .build();

        AssessmentResult savedResult = assessmentResultRepository.save(result);

        assertThat(savedResult.getId()).isNotNull();
        assertThat(savedResult.getQuestion()).isEqualTo("Explain Dependency Injection");
        assertThat(savedResult.getScoreOutOf10()).isEqualTo(9);
    }

    @Test
    @DisplayName("findAll: Should retrieve all saved evaluation results")
    void findAll_Success() {
        AssessmentResult result1 = AssessmentResult.builder()
                .question("Question 1")
                .studentAnswer("Answer 1")
                .scoreOutOf10(8)
                .build();

        AssessmentResult result2 = AssessmentResult.builder()
                .question("Question 2")
                .studentAnswer("Answer 2")
                .scoreOutOf10(10)
                .build();

        entityManager.persist(result1);
        entityManager.persist(result2);
        entityManager.flush();

        List<AssessmentResult> results = assessmentResultRepository.findAll();

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("findById: Should return result when existing ID is provided")
    void findById_Success() {
        AssessmentResult result = AssessmentResult.builder()
                .question("Question 3")
                .studentAnswer("Answer 3")
                .scoreOutOf10(7)
                .build();

        AssessmentResult persisted = entityManager.persistAndFlush(result);

        Optional<AssessmentResult> foundResult = assessmentResultRepository.findById(persisted.getId());

        assertThat(foundResult).isPresent();
        assertThat(foundResult.get().getQuestion()).isEqualTo("Question 3");
    }
}