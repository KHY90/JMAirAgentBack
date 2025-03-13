package com.jmair.cleaning.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jmair.cleaning.entity.CleanEntity;

@Repository
public interface CleanRepository extends JpaRepository<CleanEntity, Integer> {

}

