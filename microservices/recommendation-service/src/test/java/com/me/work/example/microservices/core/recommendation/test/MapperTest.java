package com.me.work.example.microservices.core.recommendation.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.mapstruct.factory.Mappers;

import com.me.work.example.api.core.recommendation.Recommendation;
import com.me.work.example.microservices.core.recommendation.bo.RecommendationEntity;
import com.me.work.example.microservices.core.recommendation.mapper.RecommendationMapper;

public class MapperTest {

	private RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);
	
	@Test
	public void mapper() {
		
		RecommendationEntity entity = new RecommendationEntity(1, 1, "rsaniez", 1, "This product is broken!");
		
		Recommendation model = mapper.toModel(entity);
		assertEquals(model.getAuthor(), entity.getAuthor());
		assertEquals(model.getContent(), entity.getContent());
		assertEquals(model.getRate(), entity.getRate());
		assertEquals(model.getRecommendationID(), entity.getRecommendationID());
		assertEquals(model.getProductID(), entity.getProductID());
		
		entity = mapper.toBusinessObject(model);
		
		assertEquals(entity.getContent(), model.getContent());
		assertEquals(entity.getAuthor(), model.getAuthor());
		assertEquals(entity.getRate(), model.getRate());
		assertEquals(entity.getRecommendationID(), model.getRecommendationID());
		assertEquals(entity.getProductID(), model.getProductID());
	}
}
