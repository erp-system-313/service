package com.erp.config;

import com.erp.admin.entity.Role;
import com.erp.admin.entity.User;
import com.erp.finance.entity.Account;
import com.erp.finance.entity.AccountType;
import com.erp.inventory.entity.Category;
import com.erp.inventory.entity.Product;
import com.erp.hr.entity.Employee;
import com.erp.purchasing.entity.Supplier;
import com.erp.sales.entity.Customer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;

@TestConfiguration
public class TestConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public TestDataSeeder testDataSeeder(EntityManager em, PasswordEncoder passwordEncoder) {
        return new TestDataSeeder(em, passwordEncoder);
    }

    public static class TestDataSeeder {
        @PersistenceContext
        private EntityManager entityManager;
        private final PasswordEncoder passwordEncoder;

        public TestDataSeeder(EntityManager entityManager, PasswordEncoder passwordEncoder) {
            this.entityManager = entityManager;
            this.passwordEncoder = passwordEncoder;
        }

        public SeededData seed() {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Administrator role");
            adminRole.setIsActive(true);
            entityManager.persist(adminRole);

            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("User role");
            userRole.setIsActive(true);
            entityManager.persist(userRole);

            User admin = new User();
            admin.setEmail("admin@erp.com");
            admin.setPasswordHash(passwordEncoder.encode("test123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(adminRole);
            admin.setIsActive(true);
            entityManager.persist(admin);

            User user = new User();
            user.setEmail("user@test.com");
            user.setPasswordHash(passwordEncoder.encode("test123"));
            user.setFirstName("Test");
            user.setLastName("User");
            user.setRole(userRole);
            user.setIsActive(true);
            entityManager.persist(user);

            Category category = new Category();
            category.setName("Test Category");
            category.setDescription("Test Category Description");
            category.setStatus(Category.Status.ACTIVE);
            entityManager.persist(category);

            Product product = new Product();
            product.setName("Test Product");
            product.setSku("TEST-001");
            product.setDescription("Test Product Description");
            product.setCategory(category);
            product.setPrice(BigDecimal.valueOf(100.00));
            product.setCost(BigDecimal.valueOf(50.00));
            product.setQuantity(100);
            product.setMinStock(10);
            product.setStatus(Product.Status.ACTIVE);
            entityManager.persist(product);

            Supplier supplier = new Supplier();
            supplier.setName("Test Supplier");
            supplier.setEmail("supplier@test.com");
            supplier.setPhone("1234567890");
            supplier.setAddress("Test Address");
            supplier.setStatus(Supplier.Status.ACTIVE);
            entityManager.persist(supplier);

            Customer customer = new Customer();
            customer.setName("Test Customer");
            customer.setEmail("customer@test.com");
            customer.setPhone("1234567890");
            customer.setAddress("Test Address");
            customer.setIsActive(true);
            entityManager.persist(customer);

            Employee employee = new Employee();
            employee.setEmployeeNumber("EMP001");
            employee.setFirstName("Test");
            employee.setLastName("Employee");
            employee.setEmail("employee@test.com");
            employee.setPhone("1234567890");
            employee.setDepartment("IT");
            employee.setPosition("Developer");
            employee.setHireDate(LocalDate.now());
            employee.setStatus(Employee.Status.ACTIVE);
            entityManager.persist(employee);

            Account account = new Account();
            account.setAccountNumber("1000");
            account.setName("Cash");
            account.setAccountType(AccountType.ASSET);
            account.setIsActive(true);
            entityManager.persist(account);

            entityManager.flush();

            return new SeededData(
                category.getId(),
                product.getId(),
                supplier.getId(),
                customer.getId(),
                employee.getId(),
                account.getId()
            );
        }
    }

    public record SeededData(
        Long categoryId,
        Long productId,
        Long supplierId,
        Long customerId,
        Long employeeId,
        Long accountId
    ) {}
}