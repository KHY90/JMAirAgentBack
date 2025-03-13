package com.jmair.as.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jmair.as.entity.ASEntity;

public interface ASRepository extends JpaRepository<ASEntity, Integer> {
}
