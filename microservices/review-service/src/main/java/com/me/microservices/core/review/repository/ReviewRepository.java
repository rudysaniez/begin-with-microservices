package com.me.microservices.core.review.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.me.microservices.core.review.bo.ReviewEntity;

@Transactional
public interface ReviewRepository extends JpaRepository<ReviewEntity, Integer> {

	/**
	 * @param reviewID
	 * @return optional of {@link ReviewEntity}
	 */
	@Transactional(readOnly=true)
	public Optional<ReviewEntity> findByReviewID(Integer reviewID);
	
	/**
	 * @param productID
	 * @param page
	 * @return page of {@link ReviewEntity}
	 */
	@Transactional(readOnly=true)
	public Page<ReviewEntity> findByProductID(Integer productID, Pageable page);
	
	/**
	 * @param reviewID
	 * @param productID
	 * @return optional of {@link ReviewEntity}
	 */
	@Transactional(readOnly=true)
	public Optional<ReviewEntity> findByReviewIDAndProductID(Integer reviewID, Integer productID);
	
	/**
	 * @param id
	 * @return True or False
	 */
	@Transactional(readOnly=true)
	public boolean existsByReviewID(Integer id);
	
	/**
	 * @param reviewID
	 */
	@Transactional
	public void deleteByReviewID(Integer reviewID);
	
	/**
	 * @param productID
	 */
	@Transactional
	public void deleteByProductID(Integer productID);
}
