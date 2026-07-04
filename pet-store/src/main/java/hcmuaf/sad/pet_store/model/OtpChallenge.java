package hcmuaf.sad.pet_store.model;

import hcmuaf.sad.pet_store.model.enums.OtpChallengeStatus;
import hcmuaf.sad.pet_store.model.enums.OtpPurpose;
import hcmuaf.sad.pet_store.model.enums.OtpTargetType;
import hcmuaf.sad.pet_store.model.policy.OtpPolicy;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import hcmuaf.sad.pet_store.util.DBUtils;
import hcmuaf.sad.pet_store.util.OtpUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OtpChallenge {
    private Long id;
    private String challengeId;
    private OtpPurpose purpose;
    private OtpTargetType targetType;
    private String targetValue;
    private String userCode;
    private OtpChallengeStatus status;
    private int resendCount;
    private LocalDateTime lastSentAt;
    private LocalDateTime expiresAt;
    private Long verifiedOtpRecordId;
    private LocalDateTime verifiedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    /**
     * Factory method — tạo OtpChallenge mới với các field mặc định.
     * Chưa lưu DB — gọi {@link #insertWithFirstOtp()} để lưu kèm OTP đầu tiên.
     */
    public static OtpChallenge createNewChallenge(String userCode, OtpPurpose purpose,
                                                   OtpTargetType targetType, String targetValue) {
        LocalDateTime now = LocalDateTime.now();
        OtpChallenge challenge = new OtpChallenge();
        challenge.setChallengeId(UUID.randomUUID().toString());
        challenge.setPurpose(purpose);
        challenge.setTargetType(targetType);
        challenge.setTargetValue(targetValue);
        challenge.setUserCode(userCode);
        challenge.setStatus(OtpChallengeStatus.PENDING);
        challenge.setResendCount(0);
        challenge.setLastSentAt(now);
        challenge.setExpiresAt(now.plusMinutes(OtpPolicy.CHALLENGE_TTL_MINUTES));
        challenge.setCreatedAt(now);
        return challenge;
    }

    /**
     * [4.1.8 + 4.1.9] Lưu challenge mới + tạo OTP record đầu tiên trong 1 transaction.
     * @return OTP plain-text (để gửi cho user qua email/SMS)
     */
    public String insertWithFirstOtp() {
        String otp = OtpUtils.generate();
        OtpRecord record = OtpRecord.createActiveForChallenge(this.challengeId, otp);

        DBUtils.tx().executeWithoutResult(status -> {
            // [4.1.8] Tạo phiên xác thực OTP
            this.insert();
            // [4.1.9] Lưu mã OTP cho phiên
            record.insert();
        });
        return otp;
    }

    public static OtpChallenge findActiveByChallengeId(String challengeId) {
        try {
            List<OtpChallenge> results = DBUtils.jdbc().query("""
                    SELECT * FROM otp_challenges
                    WHERE challenge_id = ? AND status IN ('PENDING', 'VERIFIED')
                    LIMIT 1
                    """, ROW_MAPPER, challengeId);
            return results.isEmpty() ? null : results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static OtpChallenge findVerifiedByChallengeId(String challengeId) {
        if (challengeId == null || challengeId.isBlank()) {
            return null;
        }

        try {
            List<OtpChallenge> results = DBUtils.jdbc().query("""
                    SELECT * FROM otp_challenges
                    WHERE challenge_id = ? AND status = 'VERIFIED' AND expires_at > ?
                    LIMIT 1
                    """, ROW_MAPPER, challengeId, LocalDateTime.now());
            return results.isEmpty() ? null : results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static OtpChallenge findActiveByUserAndTarget(String userCode, OtpPurpose purpose,
                                                          OtpTargetType targetType, String targetValue) {
        try {
            List<OtpChallenge> results = DBUtils.jdbc().query("""
                    SELECT * FROM otp_challenges
                    WHERE user_code = ? AND purpose = ? AND target_type = ? AND target_value = ?
                      AND status = 'PENDING'
                    ORDER BY created_at DESC
                    LIMIT 1
                    """, ROW_MAPPER, userCode, purpose.name(), targetType.name(), targetValue);
            return results.isEmpty() ? null : results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void insert() {
        try {
            DBUtils.jdbc().update("""
                    INSERT INTO otp_challenges
                    (challenge_id, purpose, target_type, target_value, user_code, status,
                     resend_count, last_sent_at, expires_at, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    challengeId, purpose.name(), targetType.name(), targetValue, userCode, status.name(),
                    resendCount, lastSentAt, expiresAt, createdAt);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void incrementResendCount() {
        try {
            LocalDateTime now = LocalDateTime.now();
            DBUtils.jdbc().update("""
                    UPDATE otp_challenges
                    SET resend_count = resend_count + 1, last_sent_at = ?
                    WHERE id = ?
                    """, now, id);
            this.resendCount++;
            this.lastSentAt = now;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void markVerified(Long otpRecordId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            DBUtils.jdbc().update("""
                    UPDATE otp_challenges
                    SET status = 'VERIFIED', verified_otp_record_id = ?, verified_at = ?
                    WHERE id = ?
                    """, otpRecordId, now, id);
            this.status = OtpChallengeStatus.VERIFIED;
            this.verifiedOtpRecordId = otpRecordId;
            this.verifiedAt = now;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void markCompleted() {
        try {
            LocalDateTime now = LocalDateTime.now();
            DBUtils.jdbc().update("""
                    UPDATE otp_challenges
                    SET status = 'COMPLETED', completed_at = ?
                    WHERE id = ?
                    """, now, id);
            this.status = OtpChallengeStatus.COMPLETED;
            this.completedAt = now;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void markExpired() {
        try {
            DBUtils.jdbc().update("""
                    UPDATE otp_challenges
                    SET status = 'EXPIRED'
                    WHERE id = ? AND status = 'PENDING'
                    """, id);
            if (status == OtpChallengeStatus.PENDING) {
                this.status = OtpChallengeStatus.EXPIRED;
            }
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public boolean isExpired() {
        return expiresAt == null || !expiresAt.isAfter(LocalDateTime.now());
    }

    public boolean isCooldownActive() {
        return lastSentAt != null
                && lastSentAt.plusSeconds(OtpPolicy.RESEND_COOLDOWN_SECONDS).isAfter(LocalDateTime.now());
    }

    public boolean isMaxResendReached() {
        return resendCount >= OtpPolicy.MAX_RESEND;
    }

    public LocalDateTime nextResendAt() {
        return lastSentAt == null ? LocalDateTime.now() : lastSentAt.plusSeconds(OtpPolicy.RESEND_COOLDOWN_SECONDS);
    }

    public static final RowMapper<OtpChallenge> ROW_MAPPER = (rs, rowNum) -> {
        OtpChallenge challenge = new OtpChallenge();
        challenge.setId(rs.getLong("id"));
        challenge.setChallengeId(rs.getString("challenge_id"));
        challenge.setPurpose(OtpPurpose.valueOf(rs.getString("purpose")));
        challenge.setTargetType(OtpTargetType.valueOf(rs.getString("target_type")));
        challenge.setTargetValue(rs.getString("target_value"));
        challenge.setUserCode(rs.getString("user_code"));
        challenge.setStatus(OtpChallengeStatus.valueOf(rs.getString("status")));
        challenge.setResendCount(rs.getInt("resend_count"));
        challenge.setLastSentAt(rs.getObject("last_sent_at", LocalDateTime.class));
        challenge.setExpiresAt(rs.getObject("expires_at", LocalDateTime.class));
        Long verifiedRecordId = rs.getObject("verified_otp_record_id", Long.class);
        challenge.setVerifiedOtpRecordId(verifiedRecordId);
        challenge.setVerifiedAt(rs.getObject("verified_at", LocalDateTime.class));
        challenge.setCompletedAt(rs.getObject("completed_at", LocalDateTime.class));
        challenge.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return challenge;
    };
}
