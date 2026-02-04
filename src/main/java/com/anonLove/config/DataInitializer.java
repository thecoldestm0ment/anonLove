package com.anonLove.config;

import com.anonLove.domain.post.Category;
import com.anonLove.domain.post.Post;
import com.anonLove.repository.CategoryRepository;
import com.anonLove.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
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

    @Override
    @Transactional
    public void run(String... args) {
        // 2. 카테고리 초기화
        initializeCategories();
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
