package com.pickleball.security;

import com.pickleball.entity.Account;
import com.pickleball.entity.MemberRole;
import com.pickleball.repository.AccountRepository;
import com.pickleball.repository.MemberRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

        private final AccountRepository accountRepository;
        private final MemberRoleRepository memberRoleRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                Account account = accountRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "계정을 찾을 수 없습니다: " + username));

                List<MemberRole> roles = memberRoleRepository.findByUsername(username);

                Set<String> grantedRoles = new LinkedHashSet<>();

                for (MemberRole role : roles) {
                    if (role != null && role.getRoles() != null) {
                        String roleName = role.getRoles().trim();
                        if (!roleName.isEmpty()) {
                            String normalizedRole = roleName.startsWith("ROLE_")
                                            ? roleName.toUpperCase(Locale.ROOT)
                                            : "ROLE_" + roleName.toUpperCase(Locale.ROOT);
                            grantedRoles.add(normalizedRole);
                        }
                    }
                }

                if (account.getAccountType() != null) {
                    String fallbackRole = account.getAccountType().trim();
                    if (!fallbackRole.isEmpty()) {
                        grantedRoles.add("ROLE_" + fallbackRole.toUpperCase(Locale.ROOT));
                    }
                }

                List<SimpleGrantedAuthority> authorities = grantedRoles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                return User.builder()
                                .username(account.getUsername())
                                .password(account.getPassword())
                                .authorities(authorities)
                                .build();
        }
}
