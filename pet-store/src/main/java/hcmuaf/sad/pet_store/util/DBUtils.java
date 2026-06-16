package hcmuaf.sad.pet_store.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class DBUtils {
    private static JdbcTemplate jdbc;
    private static TransactionTemplate tx;

    public DBUtils(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        DBUtils.jdbc = jdbcTemplate;
        DBUtils.tx = new TransactionTemplate(transactionManager);
    }

    public static JdbcTemplate jdbc() {
        return jdbc;
    }

    public static TransactionTemplate tx() {
        return tx;
    }
}
