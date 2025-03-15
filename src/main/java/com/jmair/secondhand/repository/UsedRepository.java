package com.jmair.secondhand.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jmair.secondhand.entity.UsedEntity;

public interface UsedRepository extends JpaRepository<UsedEntity, Integer> {

}
