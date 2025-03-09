package com.jmair.installation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jmair.installation.entity.InstallRequest;

public interface InstallRepository extends JpaRepository<InstallRequest, Integer> {


}
