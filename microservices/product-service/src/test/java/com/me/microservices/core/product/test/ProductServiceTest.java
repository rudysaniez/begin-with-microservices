package com.me.microservices.core.product.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.me.api.Api;
import com.me.api.core.product.Product;
import com.me.microservices.core.product.bo.ProductEntity;
import com.me.microservices.core.product.mapper.ProductMapper;
import com.me.microservices.core.product.repository.ProductRepository;

import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ProductServiceTest {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ProductMapper mapper;
	
	@Value("${spring.webflux.base-path}") 
	private String basePath;
	
	private static final String PRODUCT_NAME = "MARTEAU VERTUEUX";
	private static final Integer PRODUCT_ID = 1;
	private static final Integer PRODUCT_WEIGHT = 1;
	
	private static final String PARAM_NAME = "name";
	private static final String PARAM_PAGE_NUMBER = "pageNumber";
	private static final String PARAM_PAGE_SIZE = "pageSize";
	
	@Before
	public void setupdb() {
		
		productRepository.deleteAll().block();
		
		ProductEntity productEntity = new ProductEntity(PRODUCT_ID, PRODUCT_NAME, PRODUCT_WEIGHT);
		
		createAndVerifyStatus(mapper.toModel(productEntity), HttpStatus.CREATED).
			jsonPath("$.name").isEqualTo(PRODUCT_NAME);
	}
	
	@Test
	public void getProduct() {
		
		getAndVerifyStatus(PRODUCT_ID, HttpStatus.OK).
			jsonPath("$.productID").isEqualTo(PRODUCT_ID).
			jsonPath("$.name").isEqualTo(PRODUCT_NAME);
	}
	
	@Test
	public void getProductNotFound() {
		
		getAndVerifyStatus(13, HttpStatus.NOT_FOUND).
			jsonPath("$.path").isEqualTo("/" + Api.PRODUCT_PATH + "/13").
			jsonPath("$.message").isEqualTo("Product with productID=13 doesn't not exists.");
	}
	
	@Test
	public void getProductWithInvalidProductID() {
		
		getAndVerifyStatus(0, HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.path").isEqualTo("/" + Api.PRODUCT_PATH + "/0").
			jsonPath("$.message").isEqualTo("ProductID should be greater than 0.");
	}
	
	@Test
	public void findProductByName() {

		createAndVerifyStatus(new Product(2, "MARTEAU DU VENT", 3), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(3, "MARTEAU DU CHEVALIER", 3), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(4, "MARTEAU DU GUERRIER", 4), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(5, "DAGUE DU VOLEUR", 1), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(6, "DAGUE DU BOIS", 1), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(7, "EPEE DE FEU", 2), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(8, "MASSE DE BOIS", 4), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(9, "MASSE DE FER", 7), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(10, "MASSE DE CARBONE", 3), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(11, "MASSE DE FEU", 2), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(12, "MASSE ARDENTE", 2), HttpStatus.CREATED);
		
		getAndVerifyStatus(PRODUCT_ID, HttpStatus.OK);
		getAndVerifyStatus(2, HttpStatus.OK);
		getAndVerifyStatus(3, HttpStatus.OK);
		getAndVerifyStatus(5, HttpStatus.OK);
		getAndVerifyStatus(6, HttpStatus.OK);
		getAndVerifyStatus(7, HttpStatus.OK);
		getAndVerifyStatus(8, HttpStatus.OK);
		getAndVerifyStatus(9, HttpStatus.OK);
		getAndVerifyStatus(10, HttpStatus.OK);
		getAndVerifyStatus(11, HttpStatus.OK);
		getAndVerifyStatus(12, HttpStatus.OK);
		
		/**
		 * Search products starting with by "mart".
		 * Load the page 0.
		 */
		String searchThat = "mart";
		String itemByPage = "2";
		Long count = productRepository.countByNameStartingWith(searchThat.toUpperCase()).block();
		assertEquals(4, count);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("name", searchThat);
		params.add("pageNumber", "0");
		params.add("pageSize", itemByPage);
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.page.size").isEqualTo(itemByPage).
			jsonPath("$.page.totalElements").isEqualTo(count).
			jsonPath("$.page.totalPages").isEqualTo(2).
			jsonPath("$.content[0].productID").isEqualTo(1).
			jsonPath("$.content[1].productID").isEqualTo(2);
		
		/**
		 * Search products starting with by "mart".
		 * Load the page 1.
		 */
		params = new LinkedMultiValueMap<>(3);
		params.add("name", searchThat);
		params.add("pageNumber", "1");
		params.add("pageSize", itemByPage);
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.page.size").isEqualTo(itemByPage).
			jsonPath("$.page.totalElements").isEqualTo(count).
			jsonPath("$.page.totalPages").isEqualTo(2).
			jsonPath("$.content[0].productID").isEqualTo(3).
			jsonPath("$.content[1].productID").isEqualTo(4);
		
		/**
		 * Search products starting with by "dagu".
		 */
		searchThat = "dagu"; itemByPage = "1";
		count = productRepository.countByNameStartingWith(searchThat.toUpperCase()).block();
		assertEquals(2, count);
		
		params = new LinkedMultiValueMap<>(3);
		params.add(PARAM_NAME, searchThat);
		params.add(PARAM_PAGE_NUMBER, "0");
		params.add(PARAM_PAGE_SIZE, itemByPage);
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.page.size").isEqualTo(itemByPage).
			jsonPath("$.page.totalPages").isEqualTo(2).
			jsonPath("$.page.totalElements").isEqualTo(count).
			jsonPath("$.content[0].productID").isEqualTo(5);
		
		/**
		 * Search products starting with by "mass".
		 * Load items in the same page.
		 */
		searchThat = "mass"; itemByPage = "20";
		count = productRepository.countByNameStartingWith(searchThat.toUpperCase()).block();
		assertEquals(5, count);
		
		params = new LinkedMultiValueMap<>(3);
		params.add(PARAM_NAME, searchThat);
		params.add(PARAM_PAGE_NUMBER, "0");
		params.add(PARAM_PAGE_SIZE, itemByPage);
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.page.size").isEqualTo(itemByPage).
			jsonPath("$.page.totalPages").isEqualTo(1).
			jsonPath("$.page.totalElements").isEqualTo(count).
			jsonPath("$.content[0].productID").isEqualTo(8);
		
		/**
		 * Search products starting with "ma".
		 * Load the page 0.
		 */
		searchThat = "ma"; itemByPage = "2";
		count = productRepository.countByNameStartingWith(searchThat.toUpperCase()).block();
		assertEquals(9, count);
		
		params = new LinkedMultiValueMap<>(3);
		params.add(PARAM_NAME, searchThat);
		params.add(PARAM_PAGE_NUMBER, "0");
		params.add(PARAM_PAGE_SIZE, itemByPage);
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.page.size").isEqualTo(itemByPage).
			jsonPath("$.page.number").isEqualTo(0).
			jsonPath("$.page.totalPages").isEqualTo(5).
			jsonPath("$.page.totalElements").isEqualTo(count).
			jsonPath("$.content[0].productID").isEqualTo(1).
			jsonPath("$.content[1].productID").isEqualTo(2);
		
		/**
		 * Search products starting with "ma".
		 * Load the last page.
		 */
		params = new LinkedMultiValueMap<>(3);
		params.add(PARAM_NAME, searchThat);
		params.add(PARAM_PAGE_NUMBER, "4");
		params.add(PARAM_PAGE_SIZE, itemByPage);
		
		getAndVerifyStatus(params, HttpStatus.OK).
		jsonPath("$.page.size").isEqualTo(itemByPage).
		jsonPath("$.page.number").isEqualTo(4).
		jsonPath("$.page.totalPages").isEqualTo(5).
		jsonPath("$.page.totalElements").isEqualTo(count).
		jsonPath("$.content[0].productID").isEqualTo(12);
		
		/**
		 * Search product doesn't not exists.
		 */
		searchThat = "zz"; itemByPage="2";
		
		params = new LinkedMultiValueMap<>(3);
		params.add(PARAM_NAME, searchThat);
		params.add(PARAM_PAGE_NUMBER, "0");
		params.add(PARAM_PAGE_SIZE, itemByPage);
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.content").isEmpty();
	}
	
	@Test
	public void saveProduct() {
		
		productRepository.deleteAll().block();
		
		createAndVerifyStatus(new Product(1, "DAGUE DE LA JUSTICE", 1), HttpStatus.CREATED).
			jsonPath("$.name").isEqualTo("DAGUE DE LA JUSTICE");
	}
	
	@Test
	public void saveProductWithEmptyName() {
		
		productRepository.deleteAll().block();
		
		createAndVerifyStatus(new Product(PRODUCT_ID, "", PRODUCT_WEIGHT), HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	//public void saveProductWithInvalidProductID
	
	@Test
	public void saveDuplicateProduct() {
		
		getAndVerifyStatus(PRODUCT_ID, HttpStatus.OK);
		
		createAndVerifyStatus(new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_WEIGHT), HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo(String.format("Duplicate key : check the productID (%d) or the name (%s) of product.", 
					PRODUCT_ID, PRODUCT_NAME));
	}
	
	@Test
	public void updateProduct() {
		
		getAndVerifyStatus(PRODUCT_ID, HttpStatus.OK).
			jsonPath("$.name").isEqualTo(PRODUCT_NAME).
			jsonPath("$.weight").isEqualTo(PRODUCT_WEIGHT);
		
		Product product = new Product(PRODUCT_ID, "DAGUE_DE_FER", 2);
		
		updateAndVerifyStatus(product, HttpStatus.OK).
			jsonPath("$.name").isEqualTo("DAGUE_DE_FER");
		
		getAndVerifyStatus(PRODUCT_ID, HttpStatus.OK).
			jsonPath("$.name").isEqualTo("DAGUE_DE_FER").
			jsonPath("$.weight").isEqualTo(2);
	}
	
	@Test
	public void deleteProduct() {
		
		deleteAndVerifyStatus(PRODUCT_ID, HttpStatus.OK);
		
		getAndVerifyStatus(PRODUCT_ID, HttpStatus.NOT_FOUND).
			jsonPath("$.message").isEqualTo(String.format("Product with productID=%d doesn't not exists.", PRODUCT_ID));
	}
	
	/**
	 * @param productID
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec getAndVerifyStatus(Integer productID, HttpStatus status) {
		
		return client.get().uri(basePath + "/" + Api.PRODUCT_PATH + "/" + String.valueOf(productID)).
			accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(status).
				expectBody();
	}
	
	/**
	 * @param params
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec getAndVerifyStatus(MultiValueMap<String, String> params, HttpStatus status) {
		
		return client.get().uri(builder -> builder.path(basePath + "/" + Api.PRODUCT_PATH).queryParams(params).build()).
				accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(status).
				expectBody();
	}

	/**
	 * @param product
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec createAndVerifyStatus(Product product, HttpStatus status) {
		
		return client.post().uri(basePath + "/" + Api.PRODUCT_PATH).body(Mono.just(product), Product.class).
				accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(status).
				expectBody();
	}
	
	/**
	 * @param product
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec updateAndVerifyStatus(Product product, HttpStatus status) {
		
		return client.put().uri(basePath + "/" + Api.PRODUCT_PATH + "/" + product.getProductID()).
				body(Mono.just(product), Product.class).
				accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(status).
				expectBody();
	}

	/**
	 * @param productID
	 * @param status
	 * @return {@link BodyContentSpec}
	 */
	private BodyContentSpec deleteAndVerifyStatus(Integer productID, HttpStatus status) {
		
		return client.delete().uri(basePath + "/" + Api.PRODUCT_PATH + "/" + productID).
				accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(status).
				expectBody();
	}
}
