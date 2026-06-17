package hcmuaf.sad.pet_store.controller.auth;

import hcmuaf.sad.pet_store.dto.auth.EmailCredential;
import hcmuaf.sad.pet_store.mapper.UserMapper;
import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.model.UserCredential;
import hcmuaf.sad.pet_store.model.enums.ProviderType;
import hcmuaf.sad.pet_store.util.PasswordUtils;
import hcmuaf.sad.pet_store.exception.AppException;
import hcmuaf.sad.pet_store.exception.ErrorCode;

public class EmailAuthProvider implements AuthProvider<EmailCredential> {

    @Override
    public AuthenticatedUser authenticate(EmailCredential credential) {
        // Tìm tài khoản đang hoạt động
        User user = User.findActiveByEmail(credential.getEmail());
        if (user == null)
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);

        // Tìm credential tương ứng của tài khoản trên
        UserCredential stored = UserCredential
                .findByUserCodeAndProvider(user.getUserCode(), ProviderType.EMAIL);
        if (stored == null)
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);

        // Xác thực credential (mật khẩu)
        if (!PasswordUtils.verify(credential.getPassword(), stored.getSecretHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Trả về user đã xác thực
        return UserMapper.toAuthenticatedUser(user);
    }
}
