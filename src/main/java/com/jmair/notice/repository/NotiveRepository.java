package com.jmair.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jmair.notice.entity.Notice;

@Repository
public interface NotiveRepository extends JpaRepository<Notice, Integer> {

}
