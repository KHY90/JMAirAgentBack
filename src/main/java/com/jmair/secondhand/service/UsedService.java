package com.jmair.secondhand.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmair.auth.dto.UserGrade;
import com.jmair.auth.entity.User;
import com.jmair.common.exeption.ForbiddenException;
import com.jmair.common.exeption.ResourceNotFoundException;
import com.jmair.common.exeption.UnauthorizedException;
import com.jmair.secondhand.dto.Used;
import com.jmair.secondhand.dto.UsedDTO;
import com.jmair.secondhand.entity.UsedEntity;
import com.jmair.secondhand.repository.UsedRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsedService {

	private final UsedRepository usedRepository;
	private final ObjectMapper objectMapper;

	// 등록
	@Transactional
	public UsedDTO createUsedRequest(UsedDTO dto) {
		// 현재 인증된 사용자 확인
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof User currentUser)) {
			throw new UnauthorizedException("로그인한 사용자 정보가 없습니다.");
		}
		if (!(currentUser.getUserGrade() == UserGrade.SUPERADMIN || currentUser.getUserGrade() == UserGrade.ADMIN)) {
			throw new ForbiddenException("등록 권한이 없습니다.");
		}

		// 이미지 처리: DTO의 이미지 리스트를 JSON 문자열로 변환
		String imagesJson = "[]";
		try {
			if (dto.getUsedImages() != null) {
				imagesJson = objectMapper.writeValueAsString(dto.getUsedImages());
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("이미지 처리 중 오류가 발생했습니다.");
		}

		UsedEntity entity = UsedEntity.builder()
			.usedName(dto.getUsedName())
			.usedCost(dto.getUsedCost())
			.productType(dto.getProductType())
			.usedDescription(dto.getUsedDescription())
			.usedYear(dto.getUsedYear())
			.usedTime(dto.getUsedTime())
			.usedState(Used.SALE)
			.usedNote(dto.getUsedNote())
			.usedImages(imagesJson)
			.build();

		UsedEntity saved = usedRepository.save(entity);

		List<String> imagesList = new ArrayList<>();
		try {
			imagesList = objectMapper.readValue(saved.getUsedImages(), new TypeReference<List<String>>() {});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return UsedDTO.builder()
			.usedId(saved.getUsedId())
			.usedName(saved.getUsedName())
			.usedCost(saved.getUsedCost())
			.productType(saved.getProductType())
			.usedDescription(saved.getUsedDescription())
			.usedYear(saved.getUsedYear())
			.usedTime(saved.getUsedTime())
			.usedPostTime(saved.getUsedPostTime())
			.usedEditTime(saved.getUsedEditTime())
			.usedEndTime(saved.getUsedEndTime())
			.usedState(saved.getUsedState())
			.usedNote(saved.getUsedNote())
			.usedImages(imagesList)
			.build();
	}

	// 전체 조회
	@Transactional(readOnly = true)
	public List<UsedDTO> getAllUsedRequests() {
		return usedRepository.findAll().stream()
			.filter(entity -> !entity.getUsedState().equals(Used.FALLSE))
			.map(entity -> {
				List<String> imagesList = new ArrayList<>();
				try {
					imagesList = objectMapper.readValue(entity.getUsedImages(), new TypeReference<List<String>>() {});
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				return UsedDTO.builder()
					.usedId(entity.getUsedId())
					.usedName(entity.getUsedName())
					.usedCost(entity.getUsedCost())
					.productType(entity.getProductType())
					.usedDescription(entity.getUsedDescription())
					.usedYear(entity.getUsedYear())
					.usedTime(entity.getUsedTime())
					.usedPostTime(entity.getUsedPostTime())
					.usedEditTime(entity.getUsedEditTime())
					.usedEndTime(entity.getUsedEndTime())
					.usedState(entity.getUsedState())
					.usedNote(entity.getUsedNote())
					.usedImages(imagesList)
					.build();
			}).toList();
	}

	// 상세 조회
	@Transactional(readOnly = true)
	public UsedDTO getUsedRequestDetail(Integer usedId) {
		UsedEntity entity = usedRepository.findById(usedId)
			.orElseThrow(() -> new ResourceNotFoundException("중고 에어컨을 찾을 수 없습니다."));
		if (entity.getUsedState().equals(Used.FALLSE)) {
			throw new ResourceNotFoundException("해당 중고 에어컨은 삭제되었습니다.");
		}
		List<String> imagesList = new ArrayList<>();
		try {
			imagesList = objectMapper.readValue(entity.getUsedImages(), new TypeReference<List<String>>() {});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return UsedDTO.builder()
			.usedId(entity.getUsedId())
			.usedName(entity.getUsedName())
			.usedCost(entity.getUsedCost())
			.productType(entity.getProductType())
			.usedDescription(entity.getUsedDescription())
			.usedYear(entity.getUsedYear())
			.usedTime(entity.getUsedTime())
			.usedPostTime(entity.getUsedPostTime())
			.usedEditTime(entity.getUsedEditTime())
			.usedEndTime(entity.getUsedEndTime())
			.usedState(entity.getUsedState())
			.usedNote(entity.getUsedNote())
			.usedImages(imagesList)
			.registeredUserId(entity.getRegisteredUserId())
			.build();
	}

	// 관리자 수정
	@Transactional
	public UsedDTO updateUsedRequest(Integer usedId, UsedDTO dto, Optional<User> currentUser) {
		UsedEntity entity = usedRepository.findById(usedId)
			.orElseThrow(() -> new ResourceNotFoundException("중고 에어컨을 찾을 수 없습니다."));

		// 관리자 권한 확인
		boolean isAdmin = currentUser.isPresent() &&
			(currentUser.get().getUserGrade() == UserGrade.SUPERADMIN ||
				currentUser.get().getUserGrade() == UserGrade.ADMIN);

		UsedEntity updatedEntity;

		updatedEntity = entity.toBuilder()
			.usedName(dto.getUsedName())
			.usedCost(dto.getUsedCost())
			.productType(dto.getProductType())
			.usedDescription(dto.getUsedDescription())
			.usedYear(dto.getUsedYear())
			.usedTime(dto.getUsedTime())
			.usedEditTime(LocalDateTime.now())
			.usedState(dto.getUsedState())
			.usedNote(dto.getUsedNote())
			.build();

		UsedEntity updated = usedRepository.save(entity);

		return UsedDTO.builder()
			.usedId(updated.getUsedId())
			.usedName(updated.getUsedName())
			.usedCost(updated.getUsedCost())
			.productType(updated.getProductType())
			.usedDescription(updated.getUsedDescription())
			.usedYear(updated.getUsedYear())
			.usedTime(updated.getUsedTime())
			.usedPostTime(updated.getUsedPostTime())
			.usedEditTime(updated.getUsedEditTime())
			.usedEndTime(updated.getUsedEndTime())
			.usedState(updated.getUsedState())
			.usedNote(updated.getUsedNote())
			.build();
	}

	// 유저 구매요청
	@Transactional
	public UsedDTO updateUsedSaleRequest(Integer usedId, UsedDTO dto, Optional<User> currentUser) {
		if (currentUser.isEmpty()) {
			throw new UnauthorizedException("로그인한 사용자 정보가 없습니다.");
		}
		User user = currentUser.get();

		UsedEntity entity = usedRepository.findById(usedId)
			.orElseThrow(() -> new ResourceNotFoundException("중고 에어컨을 찾을 수 없습니다."));

		if (!entity.getUsedState().equals(Used.SALE)) {
			throw new ForbiddenException("판매 중인 상품이 아닙니다.");
		}

		UsedEntity updatedEntity = entity.toBuilder()
			.usedName(dto.getUsedName())
			.usedCost(dto.getUsedCost())
			.productType(dto.getProductType())
			.usedDescription(dto.getUsedDescription())
			.usedYear(dto.getUsedYear())
			.usedTime(dto.getUsedTime())
			.usedEditTime(LocalDateTime.now())
			.usedState(Used.RESERVATION)
			.usedNote(dto.getUsedNote())
			.registeredUserId(user.getId())
			.build();

		UsedEntity updated = usedRepository.save(updatedEntity);

		return UsedDTO.builder()
			.usedId(updated.getUsedId())
			.usedName(updated.getUsedName())
			.usedCost(updated.getUsedCost())
			.productType(updated.getProductType())
			.usedDescription(updated.getUsedDescription())
			.usedYear(updated.getUsedYear())
			.usedTime(updated.getUsedTime())
			.usedPostTime(updated.getUsedPostTime())
			.usedEditTime(updated.getUsedEditTime())
			.usedEndTime(updated.getUsedEndTime())
			.usedState(updated.getUsedState())
			.usedNote(updated.getUsedNote())
			.registeredUserId(updated.getRegisteredUserId())
			.build();
	}

	// 삭제
	@Transactional
	public void deleteUsedRequest(Integer usedId, Optional<User> currentUser) {
		if (currentUser.isEmpty() ||
			!(currentUser.get().getUserGrade() == UserGrade.SUPERADMIN ||
				currentUser.get().getUserGrade() == UserGrade.ADMIN)) {
			throw new UnauthorizedException("삭제 권한이 없습니다.");
		}
		UsedEntity entity = usedRepository.findById(usedId)
			.orElseThrow(() -> new ResourceNotFoundException("중고 에어컨을 찾을 수 없습니다."));
		entity.setUsedState(Used.FALLSE);
		entity.setUsedEndTime(LocalDateTime.now());
		usedRepository.save(entity);
	}

}
