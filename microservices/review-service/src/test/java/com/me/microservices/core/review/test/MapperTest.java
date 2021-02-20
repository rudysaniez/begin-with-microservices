package com.me.microservices.core.review.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.junit.Test;
import org.mapstruct.factory.Mappers;

import com.me.microservices.core.review.api.model.Review;
import com.me.microservices.core.review.bo.ReviewEntity;
import com.me.microservices.core.review.mapper.ReviewMapper;

public class MapperTest {

	private ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);
	
	private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
	
	@Test
	public void mapper() {
		
		ReviewEntity entity = new ReviewEntity(UUID.randomUUID().toString(), 1, 1, "rsaniez", 
				"Review product", "Good product!");
		
		Review review = mapper.toModel(entity);
		assertEquals(entity.getContent(), review.getContent());
		assertEquals(entity.getProductID(), review.getProductID());
		assertEquals(entity.getReviewID(), review.getReviewID());
		assertEquals(entity.getSubject(), review.getSubject());
		assertEquals(formatter.format(entity.getCreationDate()), formatter.format(review.getCreationDate()));
		
		entity = mapper.toEntity(review);
		assertEquals(entity.getContent(), review.getContent());
		assertEquals(entity.getAuthor(), review.getAuthor());
		assertEquals(entity.getSubject(), review.getSubject());
		assertEquals(entity.getProductID(), review.getProductID());
		assertEquals(entity.getReviewID(), review.getReviewID());
		assertEquals(formatter.format(entity.getCreationDate()), formatter.format(review.getCreationDate()));
	}
}
