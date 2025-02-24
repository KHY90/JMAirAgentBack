package com.jmair.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jmair.auth.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	boolean existsByUserLogin(String userLogin);
}
