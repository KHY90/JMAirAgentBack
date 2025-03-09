package com.jmair.installation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jmair.installation.dto.Install;
import com.jmair.installation.dto.InstallDTO;
import com.jmair.installation.entity.InstallRequest;
import com.jmair.installation.repository.InstallRepository;

@Service
public class InstallService {

	private final InstallRepository installRepository;

	public InstallService(InstallRepository installRepository) {
		this.installRepository = installRepository;
	}

	// 설치 신청 등록
	@Transactional
	public InstallDTO createInstallRequest(InstallDTO installDTO) {
		InstallRequest request = new InstallRequest();
		request.setInstallName(installDTO.getInstallName());
		request.setInstallAddress(installDTO.getInstallAddress());
		request.setInstallPhone(installDTO.getInstallPhone());
		request.setInstallNumber(installDTO.getInstallNumber());
		request.setInstallEmail(installDTO.getInstallEmail());
		request.setInstallPassword(installDTO.getInstallPassword());
		request.setInstallDescription(installDTO.getInstallDescription());
		request.setRequestDate(LocalDate.now());
		request.setReservationFirstDate(installDTO.getReservationFirstDate());
		request.setReservationSecondDate(installDTO.getReservationSecondDate());
		request.setInstallStatus(Install.REQUEST);

		InstallRequest saved = installRepository.save(request);

		InstallDTO result = new InstallDTO();
		result.setInstallId(saved.getInstallId());
		result.setInstallName(saved.getInstallName());
		result.setInstallAddress(saved.getInstallAddress());
		result.setInstallPhone(saved.getInstallPhone());
		result.setInstallNumber(saved.getInstallNumber());
		result.setInstallEmail(saved.getInstallEmail());
		result.setInstallPassword(saved.getInstallPassword());
		result.setInstallDescription(saved.getInstallDescription());
		result.setRequestDate(saved.getRequestDate());
		result.setReservationFirstDate(saved.getReservationFirstDate());
		result.setReservationSecondDate(saved.getReservationSecondDate());
		result.setInstallStatus(saved.getInstallStatus());
		return result;
	}

	// 전체 조회
	public List<InstallDTO> getAllInstallRequests() {
		return installRepository.findAll().stream().map(req -> {
			InstallDTO dto = new InstallDTO();
			dto.setInstallId(req.getInstallId());
			dto.setInstallName(req.getInstallName());
			dto.setInstallAddress(req.getInstallAddress());
			dto.setInstallPhone(req.getInstallPhone());
			dto.setInstallNumber(req.getInstallNumber());
			dto.setInstallEmail(req.getInstallEmail());
			dto.setInstallDescription(req.getInstallDescription());
			dto.setRequestDate(req.getRequestDate());
			dto.setReservationFirstDate(req.getReservationFirstDate());
			dto.setReservationSecondDate(req.getReservationSecondDate());
			dto.setInstallStatus(req.getInstallStatus());
			return dto;
		}).collect(Collectors.toList());
	}
}
