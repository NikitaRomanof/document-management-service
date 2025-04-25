package wsulu.document.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import wsulu.document.enums.Roles;
import wsulu.document.model.UserEntity;
import wsulu.document.repo.UserRepo;

/**
 * A service class that implements the library interface of the user details service.
 */
@Service
public class ApplicationUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepo userRepo;

    @Autowired
    public ApplicationUserDetailsServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findOneByUserName(username);
    }

    public String create(String username, String password, String role) {
        Roles r;
        try {
            r = Roles.valueOf(role);
        } catch (Exception ignored) {
            r = Roles.USER;
        }
        UserEntity user = UserEntity.builder()
                .username(username)
                .password(new BCryptPasswordEncoder().encode(password))
                .role(r)
                .build();
        userRepo.save(user);
        return "Create Successfully !";
    }
}
