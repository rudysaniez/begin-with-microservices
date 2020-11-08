package com.me.microservices.core.review.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.me.microservices.core.review.bo.ReviewEntity;
import com.me.microservices.core.review.repository.ReactiveJpaRepositoryImpl;

@AutoConfigureTestDatabase(connection=EmbeddedDatabaseConnection.HSQL)
@Transactional
@Commit
@DataJpaTest
@RunWith(SpringRunner.class)
public class ReactiveJpaRepositoryTest {
	
	@Autowired
	ReactiveJpaRepositoryImpl<ReviewEntity, Integer> repo;
	
	@Test
	public void setup() {
		
		boolean exists = repo.existsById(1).block();
		assertFalse(exists);
		
		ReviewEntity entity = new ReviewEntity(1, 1, "rudysaniez", "Product is well!", "The product is well! it's my opinion.");
		entity = repo.save(entity).block();
		assertNotNull(entity);
		
		assertNotNull(repo.findById(entity.getId()));
		
		assertEquals(1, repo.count().block());
		
		repo.deleteEntity(entity).block();
		
		exists = repo.existsById(1).block();
		assertFalse(exists);
	}
}
