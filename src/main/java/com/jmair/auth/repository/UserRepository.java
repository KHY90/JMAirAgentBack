package com.jmair.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jmair.auth.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

	boolean existsByUserLogin(String userLogin);

	// 로그인
	Optional<User> findByUserLogin(String userLogin);
}
