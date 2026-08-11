package com.lms.backend.repository;

import com.lms.backend.entity.Role;
import com.lms.backend.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByEmail: Should return user when email exists")
    void findByEmail_Success() {
        User user = User.builder()
                .fullName("Manoj Baba")
                .email("baba@lms.com")
                .password("hashed_password_123")
                .role(Role.ROLE_STUDENT)
                .build();
        entityManager.persistAndFlush(user);

        Optional<User> foundUser = userRepository.findByEmail("baba@lms.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFullName()).isEqualTo("Manoj Baba");
        assertThat(foundUser.get().getRole()).isEqualTo(Role.ROLE_STUDENT);
    }

    @Test
    @DisplayName("findByEmail: Should return empty optional when email does not exist")
    void findByEmail_NotFound() {
        Optional<User> foundUser = userRepository.findByEmail("notfound@lms.com");

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail: Should return true if email exists in database")
    void existsByEmail_True() {
        User user = User.builder()
                .fullName("Test User")
                .email("exists@lms.com")
                .password("password123")
                .role(Role.ROLE_INSTRUCTOR)
                .build();
        entityManager.persistAndFlush(user);

        boolean exists = userRepository.existsByEmail("exists@lms.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail: Should return false if email does not exist")
    void existsByEmail_False() {
        boolean exists = userRepository.existsByEmail("nonexistent@lms.com");

        assertThat(exists).isFalse();
    }
}