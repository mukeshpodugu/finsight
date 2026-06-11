package com.finsight.config;

import com.finsight.entity.Category;
import com.finsight.entity.User;
import com.finsight.repository.CategoryRepository;
import com.finsight.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public DataInitializer(UserRepository userRepository, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        seedCategories();
        seedAdminUser();
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }

        List<Category> defaultCategories = List.of(
            // Income Categories
            Category.builder().name("Salary").type("INCOME").colorCode("#10B981").iconName("briefcase").build(),
            Category.builder().name("Freelancing").type("INCOME").colorCode("#3B82F6").iconName("laptop").build(),
            Category.builder().name("Business").type("INCOME").colorCode("#8B5CF6").iconName("trending-up").build(),
            Category.builder().name("Investments").type("INCOME").colorCode("#F59E0B").iconName("dollar-sign").build(),
            Category.builder().name("Scholarship").type("INCOME").colorCode("#EC4899").iconName("award").build(),
            Category.builder().name("Other").type("INCOME").colorCode("#6B7280").iconName("plus").build(),

            // Expense Categories
            Category.builder().name("Food").type("EXPENSE").colorCode("#EF4444").iconName("coffee").build(),
            Category.builder().name("Travel").type("EXPENSE").colorCode("#3B82F6").iconName("map-pin").build(),
            Category.builder().name("Shopping").type("EXPENSE").colorCode("#EC4899").iconName("shopping-bag").build(),
            Category.builder().name("Education").type("EXPENSE").colorCode("#10B981").iconName("book-open").build(),
            Category.builder().name("Healthcare").type("EXPENSE").colorCode("#06B6D4").iconName("activity").build(),
            Category.builder().name("Entertainment").type("EXPENSE").colorCode("#F59E0B").iconName("film").build(),
            Category.builder().name("Utilities").type("EXPENSE").colorCode("#8B5CF6").iconName("zap").build(),
            Category.builder().name("Rent").type("EXPENSE").colorCode("#6366F1").iconName("home").build(),
            Category.builder().name("Fuel").type("EXPENSE").colorCode("#F97316").iconName("droplet").build(),
            Category.builder().name("Investments").type("EXPENSE").colorCode("#10B981").iconName("trending-up").build(),
            Category.builder().name("Others").type("EXPENSE").colorCode("#6B7280").iconName("more-horizontal").build()
        );

        categoryRepository.saveAll(defaultCategories);
        System.out.println("Default global financial categories seeded successfully.");
    }

    private void seedAdminUser() {
        if (userRepository.findByEmail("admin@finsight.com").isPresent()) {
            return;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User admin = User.builder()
                .email("admin@finsight.com")
                .passwordHash(encoder.encode("admin123"))
                .firstName("PODUGU")
                .lastName("MUKESH")
                .role("ADMIN")
                .isVerified(true)
                .build();

        userRepository.save(admin);
        System.out.println("Default admin user seeded: admin@finsight.com / admin123");
    }
}
