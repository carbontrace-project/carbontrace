package com.carbontrace.modules.user.serviceimpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carbontrace.exception.ResourceNotFoundException;
import com.carbontrace.modules.auth.entity.User;
import com.carbontrace.modules.auth.repository.UserRepository;
import com.carbontrace.modules.user.dto.UserResponseDto;
import com.carbontrace.modules.user.dto.UserUpdateRequestDto;
import com.carbontrace.modules.user.mapper.UserMapper;
import com.carbontrace.modules.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reads and updates the caller's own profile (COMMANDO.md Section 8.2).
 *
 * <p>Both methods look the user up by the email carried in the validated JWT, so
 * a caller can only ever reach their own row.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND = "User not found with email: ";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(String email) {
        return userMapper.toResponseDto(findUser(email));
    }

    /**
     * {@inheritDoc}
     *
     * <p>The mapper writes onto the entity loaded inside this transaction, so
     * Hibernate's dirty checking flushes the change at commit and
     * {@code @UpdateTimestamp} refreshes {@code updated_at}. The explicit
     * {@code save} is kept because the rest of the codebase states its writes
     * that way (see {@code AuthServiceImpl}) rather than relying on the reader
     * spotting that an entity is managed.
     */
    @Override
    @Transactional
    public UserResponseDto updateCurrentUser(String email, UserUpdateRequestDto request) {
        User user = findUser(email);

        // Only firstName, lastName and companyName can arrive in the DTO, and the
        // mapper ignores every other target — email, role and password are
        // unreachable from here (Section 8.2).
        userMapper.updateUserFromDto(request, user);
        userRepository.save(user);

        log.info("Profile updated for {}", user.getEmail());
        return userMapper.toResponseDto(user);
    }

    /**
     * A 404 here means the token is valid but its subject no longer exists — the
     * account was removed between issuing the token and this request. The
     * alternative, a 401, would tell the client to refresh, which cannot help.
     */
    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Profile request for an email with no user row: {}", email);
                    return new ResourceNotFoundException(USER_NOT_FOUND + email);
                });
    }
}
