package com.fieldops.fieldops_api.user.repos;

import com.fieldops.fieldops_api.user.domain.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, UUID> {
}
