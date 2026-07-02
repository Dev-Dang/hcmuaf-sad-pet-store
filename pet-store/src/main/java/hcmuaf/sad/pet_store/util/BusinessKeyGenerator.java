package hcmuaf.sad.pet_store.util;

import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import hcmuaf.sad.pet_store.model.enums.EntityType;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

public class BusinessKeyGenerator {
    private static TransactionTemplate txNew;

    public BusinessKeyGenerator(PlatformTransactionManager transactionManager) {
        TransactionTemplate t = new TransactionTemplate(transactionManager);
        t.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        BusinessKeyGenerator.txNew = t;
    }

    public static String next(EntityType entityType) {
        try {
            return txNew.execute(status -> {
                Map<String, Object> row = DBUtils.jdbc().queryForMap(
                        "SELECT prefix, last_val, pad_length FROM bk_generator WHERE entity_type = ? FOR UPDATE",
                        entityType.name());
                long nextVal = ((Number) row.get("last_val")).longValue() + 1;
                DBUtils.jdbc().update(
                        "UPDATE bk_generator SET last_val = ? WHERE entity_type = ?",
                        nextVal, entityType.name());
                int pad = ((Number) row.get("pad_length")).intValue();
                return row.get("prefix") + "-" + String.format("%0" + pad + "d", nextVal);
            });
        } catch (DataAccessException | TransactionException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }
}
