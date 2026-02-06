package com.anonLove.config;

import com.anonLove.domain.post.Category;
import com.anonLove.domain.post.Post;
import com.anonLove.domain.user.Gender;
import com.anonLove.domain.user.User;
import com.anonLove.repository.CategoryRepository;
import com.anonLove.repository.PostRepository;
import com.anonLove.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. 유저 초기화
        initializeUsers();

        // 2. 카테고리 초기화
        initializeCategories();
    }

    private void initializeUsers() {
        log.info("Checking users initialization...");

        List<TestUser> testUsers = Arrays.asList(
            new TestUser("example1@gmail.com", "password123", "테스트유저1", "테스트대학교", Gender.MALE),
            new TestUser("example2@gmail.com", "password123", "테스트유저2", "테스트대학교", Gender.FEMALE),
            new TestUser("example3@gmail.com", "password123", "테스트유저3", "테스트대학교", Gender.MALE)
        );

        int createdCount = 0;

        for (TestUser testUser : testUsers) {
            if (!userRepository.existsByEmail(testUser.email)) {
                log.info("User with email '{}' not found. Creating...", testUser.email);
                User user = User.builder()
                        .email(testUser.email)
                        .password(passwordEncoder.encode(testUser.password))
                        .nickname(testUser.nickname)
                        .university(testUser.university)
                        .gender(testUser.gender)
                        .build();
                userRepository.save(user);
                log.info("Created user: {} (ID: {})", testUser.email, user.getId());
                createdCount++;
            } else {
                log.info("User with email '{}' already exists. Skipping.", testUser.email);
            }
        }

        if (createdCount > 0) {
            log.info("User initialization completed. Created {} users. Total: {}", createdCount, userRepository.count());
        } else {
            log.info("All test users already exist. No new users created. Total: {}", userRepository.count());
        }
    }

    private static class TestUser {
        String email;
        String password;
        String nickname;
        String university;
        Gender gender;

        TestUser(String email, String password, String nickname, String university, Gender gender) {
            this.email = email;
            this.password = password;
            this.nickname = nickname;
            this.university = university;
            this.gender = gender;
        }
    }

    private void initializeCategories() {
        log.info("Checking categories initialization...");

        List<String> categoryNames = Arrays.asList("연애", "이별", "썸", "짝사랑", "고민", "자유");
        int createdCount = 0;

        for (String categoryName : categoryNames) {
            // 카테고리별로 존재 여부 확인
            if (!categoryRepository.findByName(categoryName).isPresent()) {
                log.info("Category '{}' not found. Creating...", categoryName);
                Category category = new Category(categoryName);
                categoryRepository.save(category);
                log.info("Created category: {} (ID: {})", categoryName, category.getId());
                createdCount++;
            } else {
                log.info("Category '{}' already exists. Skipping.", categoryName);
            }
        }

        if (createdCount > 0) {
            log.info("Category initialization completed. Created {} categories. Total: {}", createdCount, categoryRepository.count());
        } else {
            log.info("All categories already exist. No new categories created. Total: {}", categoryRepository.count());
        }
    }
}
