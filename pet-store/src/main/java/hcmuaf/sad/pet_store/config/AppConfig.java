package hcmuaf.sad.pet_store.config;

import hcmuaf.sad.pet_store.util.BusinessKeyGenerator;
import hcmuaf.sad.pet_store.util.DBUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }

    @Bean
    public DBUtils dbUtils(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        return new DBUtils(jdbcTemplate, transactionManager);
    }

    @Bean
    public BusinessKeyGenerator businessKeyGenerator(PlatformTransactionManager transactionManager) {
        return new BusinessKeyGenerator(transactionManager);
    }
}
