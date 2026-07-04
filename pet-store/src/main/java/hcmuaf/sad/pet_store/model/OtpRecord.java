package hcmuaf.sad.pet_store.model;

import hcmuaf.sad.pet_store.model.enums.OtpRecordStatus;
import hcmuaf.sad.pet_store.model.policy.OtpPolicy;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import hcmuaf.sad.pet_store.util.DBUtils;
import hcmuaf.sad.pet_store.util.PasswordUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OtpRecord {
    private Long id;
    private String challengeId;
    private String otpHash;
    private int attemptCount;
    private OtpRecordStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;

    public static OtpRecord createActiveForChallenge(String challengeId, String plainOtp) {
        LocalDateTime now = LocalDateTime.now();
        OtpRecord record = new OtpRecord();
        record.setChallengeId(challengeId);
        record.setOtpHash(PasswordUtils.hash(plainOtp));
        record.setAttemptCount(0);
        record.setStatus(OtpRecordStatus.ACTIVE);
        record.setSentAt(now);
        record.setExpiresAt(now.plusMinutes(OtpPolicy.OTP_TTL_MINUTES));
        record.setCreatedAt(now);
        return record;
    }

    public static OtpRecord findActiveByChallengeId(String challengeId) {
        try {
            List<OtpRecord> results = DBUtils.jdbc().query("""
                    SELECT * FROM otp_records
                    WHERE challenge_id = ? AND status = 'ACTIVE'
                    ORDER BY created_at DESC
                    LIMIT 1
                    """, ROW_MAPPER, challengeId);
            return results.isEmpty() ? null : results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void insert() {
        try {
            DBUtils.jdbc().update("""
                    INSERT INTO otp_records
                    (challenge_id, otp_hash, attempt_count, status, sent_at, expires_at, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, challengeId, otpHash, attemptCount, status.name(), sentAt, expiresAt, createdAt);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void incrementAttemptCount() {
        try {
            DBUtils.jdbc().update("""
                    UPDATE otp_records
                    SET attempt_count = attempt_count + 1
                    WHERE id = ?
                    """, id);
            this.attemptCount++;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void markUsed() {
        try {
            LocalDateTime now = LocalDateTime.now();
            DBUtils.jdbc().update("""
                    UPDATE otp_records
                    SET status = 'USED', used_at = ?
                    WHERE id = ?
                    """, now, id);
            this.status = OtpRecordStatus.USED;
            this.usedAt = now;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void invalidate() {
        try {
            DBUtils.jdbc().update("""
                    UPDATE otp_records
                    SET status = 'INVALIDATED'
                    WHERE challenge_id = ? AND status = 'ACTIVE'
                    """, challengeId);
            this.status = OtpRecordStatus.INVALIDATED;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public boolean isExpired() {
        return expiresAt == null || !expiresAt.isAfter(LocalDateTime.now());
    }

    public boolean isMaxAttemptReached() {
        return attemptCount >= OtpPolicy.MAX_ATTEMPT;
    }

    public boolean isValid() {
        return status == OtpRecordStatus.ACTIVE && !isExpired() && !isMaxAttemptReached();
    }

    public static final RowMapper<OtpRecord> ROW_MAPPER = (rs, rowNum) -> {
        OtpRecord record = new OtpRecord();
        record.setId(rs.getLong("id"));
        record.setChallengeId(rs.getString("challenge_id"));
        record.setOtpHash(rs.getString("otp_hash"));
        record.setAttemptCount(rs.getInt("attempt_count"));
        record.setStatus(OtpRecordStatus.valueOf(rs.getString("status")));
        record.setSentAt(rs.getObject("sent_at", LocalDateTime.class));
        record.setExpiresAt(rs.getObject("expires_at", LocalDateTime.class));
        record.setUsedAt(rs.getObject("used_at", LocalDateTime.class));
        record.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return record;
    };
}
