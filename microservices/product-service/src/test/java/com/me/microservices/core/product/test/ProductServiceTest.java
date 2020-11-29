package com.me.microservices.core.product.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.me.api.Api;
import com.me.api.core.product.Product;
import com.me.api.event.Event;
import com.me.microservices.core.product.repository.ProductRepository;
import com.me.microservices.core.product.services.AsciiArtService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class ProductServiceTest {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private AsciiArtService asciiArt;
	
	@Value("${spring.webflux.base-path}") 
	private String basePath;
	
	@Autowired
	private Sink channel;
	
	@Rule
	public OutputCaptureRule output = new OutputCaptureRule();
	
	private static final String PRODUCT_NAME = "PANNEAU SOLAIRE";
	private static final Integer PRODUCT_ID = 1;
	private static final Integer PRODUCT_WEIGHT = 1;
	
	private static final String PARAM_NAME = "name";
	private static final String PARAM_PAGE_NUMBER = "pageNumber";
	private static final String PARAM_PAGE_SIZE = "pageSize";
	
	@Before
	public void setupdb() {
		
		asciiArt.display("SETUP");
		
		productRepository.deleteAll().block();
		
		Product product = new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_WEIGHT);
		
		createAndVerifyStatus(product, HttpStatus.CREATED).
			jsonPath("$.name").isEqualTo(PRODUCT_NAME);
		
		IntStream.rangeClosed(PRODUCT_ID + 1, 21).mapToObj(i -> new Product(i, PRODUCT_NAME + "_" + i, PRODUCT_WEIGHT + i)).
			forEach(p -> createAndVerifyStatus(p, HttpStatus.CREATED));
	}
	
	@Test
	public void getProduct() {
		
		asciiArt.display("GET PRODUCT");
		
		getAndVerifyStatus(PRODUCT_ID, HttpStatus.OK).
			jsonPath("$.productID").isEqualTo(PRODUCT_ID).
			jsonPath("$.name").isEqualTo(PRODUCT_NAME);
	}
	
	@Test
	public void getProductNotFoundException() {
		
		asciiArt.display("GET PRODUCT BUT NOT FOUND EXCEPTION");
		
		getAndVerifyStatus(999, HttpStatus.NOT_FOUND).
			jsonPath("$.path").isEqualTo("/" + Api.PRODUCT_PATH + "/999").
			jsonPath("$.message").isEqualTo(String.format("Product with productID=%d doesn't not exists.", 999));
	}
	
	@Test
	public void getProductInvalidInputException() {
		
		asciiArt.display("GET PRODUCT BUT INVALID INPUT EXCEPTION");
		
		getAndVerifyStatus(0, HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.path").isEqualTo("/" + Api.PRODUCT_PATH + "/0").
			jsonPath("$.message").isEqualTo("ProductID should be greater than 0.");
	}
	
	@Test
	public void getProductByName() {

		asciiArt.display("GET PRODUCT BY NAME");
		
		createAndVerifyStatus(new Product(30, "MARTEAU EN BOIS", 3), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(31, "MARTEAU", 3), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(32, "MARTEAU MENUISIER", 4), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(33, "TOURNEVIS", 1), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(34, "TOURNEVIS ELECTRONIQUE", 1), HttpStatus.CREATED);
		createAndVerifyStatus(new Product(35, "TOURNEVIS A FRAPPER", 2), HttpStatus.CREATED);
		
		/**
		 * PAGE 0.
		 */
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(3);
		params.add("name", "MART");
		params.add("pageNumber", "0");
		params.add("pageSize", "2");
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.page.size").isEqualTo(2).
			jsonPath("$.page.totalElements").isEqualTo(productRepository.countByNameStartingWith("MART").block()).
			jsonPath("$.page.totalPages").isEqualTo(2);
		
		/**
		 * PAGE 1.
		 */
		params = new LinkedMultiValueMap<>(3);
		params.add("name", "MART");
		params.add("pageNumber", "1");
		params.add("pageSize", "2");
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.page.size").isEqualTo(2).
			jsonPath("$.page.totalPages").isEqualTo(2).
			jsonPath("$.content[0].name").isEqualTo("MARTEAU MENUISIER");
		
		/**
		 * Search products starting with by "dagu".
		 */
		params = new LinkedMultiValueMap<>(3);
		params.add(PARAM_NAME, "TOURNE");
		params.add(PARAM_PAGE_NUMBER, "0");
		params.add(PARAM_PAGE_SIZE, "1");
		
		getAndVerifyStatus(params, HttpStatus.OK).
			jsonPath("$.page.size").isEqualTo(1).
			jsonPath("$.page.totalPages").isEqualTo(3).
			jsonPath("$.page.totalElements").isEqualTo(productRepository.countByNameStartingWith("TOURNE").block());
	}
	
	@Test
	public void createProduct() {
		
		asciiArt.display("CREATE PRODUCT");
		
		createAndVerifyStatus(new Product(999, "SCIE CIRCULAIRE", 2), HttpStatus.CREATED).
			jsonPath("$.name").isEqualTo("SCIE CIRCULAIRE");
	}
	
	@Test
	public void createProductWithEmptyName() {
		
		asciiArt.display("CREATE PRODUCT WITH EMPTY NAME");
		
		createAndVerifyStatus(new Product(999, "", PRODUCT_WEIGHT), HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	@Test
	public void createProductDuplicateKeyException() {
		
		asciiArt.display("CREATE PRODUCT BUT DUPLICATE KEY EXCEPTION");
		
		getAndVerifyStatus(PRODUCT_ID, HttpStatus.OK);
		
		createAndVerifyStatus(new Product(PRODUCT_ID, PRODUCT_NAME, PRODUCT_WEIGHT), HttpStatus.UNPROCESSABLE_ENTITY).
			jsonPath("$.message").isEqualTo(String.format("Duplicate key : check the productID (%d) or the name (%s) of product.", 
					PRODUCT_ID, PRODUCT_NAME));
	}
	
	@Test
	public void updateProduct() {
		
		asciiArt.display("UPDATE PRODUCT");
		
		getAndVerifyStatus(PRODUCT_ID, HttpStatus.OK).
			jsonPath("$.name").isEqualTo(PRODUCT_NAME).
			jsonPath("$.weight").isEqualTo(PRODUCT_WEIGHT);
		
		Product product = new Product(PRODUCT_ID, PRODUCT_NAME, 50);
		
		updateAndVerifyStatus(product, HttpStatus.OK).
			jsonPath("$.weight").isEqualTo(50);
		
		getAndVerifyStatus(PRODUCT_ID, HttpStatus.OK).
			jsonPath("$.weight").isEqualTo(50);
	}
	
	@Test
	public void deleteProduct() {
		
		asciiArt.display("DELETE PRODUCT");
		
		deleteAndVerifyStatus(PRODUCT_ID, HttpStatus.OK);
		
		getAndVerifyStatus(PRODUCT_ID, HttpStatus.NOT_FOUND).
			jsonPath("$.message").isEqualTo(String.format("Product with productID=%d doesn't not exists.", PRODUCT_ID));
	}
	
	@Test
	public void deleteProductAsynchronous() {
		
		asciiArt.display("DELETE PRODUCT ASYNCHRONOUS");
		
		sendDeleteProductEvent(20);
		assertNull(productRepository.findByProductID(20).block());
	}
	
	@Test
	public void deleteProductAsyncNotFoundException() {
		
		asciiArt.display("DELETE PRODUCT ASYNC BUT NOT FOUND EXCEPTION");
		
		sendDeleteProductEvent(999);
		assertThat(output).contains(String.format("The product with id=%d can't be deleted because it doesn't not exists.", 999));
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
	
	/**
	 * @param productId
	 */
	public void sendDeleteProductEvent(Integer productId) {
		
		Event<Integer, Product> event = new Event<Integer, Product>(productId, null, Event.Type.DELETE);
		log.info(" > One message will be sent for a product deletion ({}).", event.toString());
		channel.input().send(MessageBuilder.withPayload(event).build());
	}
}
