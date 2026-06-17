package hcmuaf.sad.pet_store.uc2;

import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.model.enums.EntityType;
import hcmuaf.sad.pet_store.model.enums.UserRole;
import hcmuaf.sad.pet_store.util.BusinessKeyGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"test", "uc2"})
@Transactional
class UserTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // [2.1.5] Kiểm tra email với tài khoản trong hệ thống
    @Test
    void findActiveByEmail_nonExistent_shouldReturnNull() {
        User user = User.findActiveByEmail("nonexistent@test.com");
        assertThat(user).isNull();
    }

    @Test
    void findActiveByEmail_existingUser_shouldReturnUser() {
        // [2.1.5] Tìm user active theo email
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "found@test.com", "Found User", UserRole.CUSTOMER).insert();

        User user = User.findActiveByEmail("found@test.com");
        assertThat(user).isNotNull();
        assertThat(user.getUserCode()).isEqualTo(userCode);
        assertThat(user.getEmail()).isEqualTo("found@test.com");
        assertThat(user.getDisplayName()).isEqualTo("Found User");
        assertThat(user.getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void findActiveByEmail_deletedUser_shouldReturnNull() {
        // [2.1.5] User bị xóa mềm không thể tra cứu — softDelete() tạo tombstone
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        User user = new User(userCode, "deleted@test.com", "Deleted User", UserRole.CUSTOMER);
        user.insert();
        user.softDelete();

        User found = User.findActiveByEmail("deleted@test.com");
        assertThat(found).isNull();
    }

    @Test
    void findActiveByEmail_oldVersion_shouldReturnNull() {
        // [2.1.5] Chỉ trả về is_current=true, bỏ qua các version cũ
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);

        // Insert old version
        jdbcTemplate.update(
                "INSERT INTO users (user_code, email, display_name, role, effective_from, effective_to, is_current, is_deleted, created_at) VALUES (?, ?, ?, ?, ?, ?, false, false, ?)",
                userCode, "oldversion@test.com", "Old User", "CUSTOMER", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());

        User user = User.findActiveByEmail("oldversion@test.com");
        assertThat(user).isNull();
    }
}
