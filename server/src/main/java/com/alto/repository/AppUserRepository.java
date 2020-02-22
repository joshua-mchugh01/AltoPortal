package com.alto.repository;

import com.alto.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by fan.jin on 2016-10-15.
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByUsername(String username);
}

