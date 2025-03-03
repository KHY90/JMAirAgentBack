package com.jmair.notice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jmair.notice.entity.Notice;

@Repository
public interface NotiveRepository extends JpaRepository<Notice, Integer> {

	// 전체 조회용
	List<Notice> findByStatusTrueOrderByNoticePostTimeDesc();

}
