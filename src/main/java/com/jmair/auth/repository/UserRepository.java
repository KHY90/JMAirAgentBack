package com.jmair.auth.repository;

import com.jmair.auth.dto.UserGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import com.jmair.auth.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

	boolean existsByUserLogin(String userLogin);

	// 로그인
        Optional<User> findByUserLogin(String userLogin);

        // 등급으로 조회
        java.util.List<User> findByUserGrade(UserGrade userGrade);

        // 엔지니어 신청자 조회
        java.util.List<User> findByEngineerAppliedAtIsNotNullAndUserGrade(UserGrade userGrade);
}
