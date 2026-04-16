package com.erp.auth.security;

import com.erp.admin.entity.User;
import com.erp.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmailWithRole(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

    if (!user.getIsActive()) {
      throw new UsernameNotFoundException("User account is inactive: " + email);
    }

    return UserPrincipal.create(user);
  }

  @Transactional(readOnly = true)
  public UserDetails loadUserById(Long id) {
    User user = userRepository.findByIdWithRole(id)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

    return UserPrincipal.create(user);
  }
}
