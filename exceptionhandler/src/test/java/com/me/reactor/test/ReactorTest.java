package com.me.reactor.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.Ignore;
import org.junit.Test;

import com.me.handler.exception.NotFoundException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Ignore
public class ReactorTest {

	@Test
	public void testStream() {
		
		assertThat(List.of(1, 2, 3, 4).stream().filter(i -> i % 2 == 0).map(i -> i * 2).collect(Collectors.toList())).
			containsExactly(4, 8);
	}
	
	@Test
	public void testStreamProductSales() {
		
		List<ProductSalesModel> listOfModel = IntStream.rangeClosed(1, 200).mapToObj(i -> new ProductSalesEntity(i)).
			sorted((p1, p2) -> p1.amount < p2.amount ? 1 : -1).limit(5).
				map(entity -> new ProductSalesModel(entity.id, entity.amount)).collect(Collectors.toList());
		
		assertThat(listOfModel).isNotEmpty();
		
		listOfModel.forEach(System.out::println);
	}
	
	@Test
	public void testFlux() {
		
		List<Integer> flux = new ArrayList<>();
		Flux.just(1, 2, 3, 4).filter(i -> i % 2 == 0).map(i -> i * 2).log().subscribe(i -> flux.add(i));
		assertThat(flux).containsExactly(4, 8);
	}
	
	@Test
	public void testFluxBlocking() {
		
		List<Integer> list = Flux.just(1, 2, 3, 4).filter(i -> i % 2 == 0).map(i -> i * 2).log().collectList().
				block(Duration.of(15, ChronoUnit.SECONDS));
		
		assertThat(list).containsExactly(4, 8);
	}
	
	@Test
	public void testMonoProductSales() {
		
		Mono<ProductSalesModel> monoOfProductSales = Mono.just(new ProductSalesEntity(1)).
				switchIfEmpty(Mono.error(new NotFoundException())).
					log().map( entity -> new ProductSalesModel(entity.getId(), entity.getAmount()));
		
		ProductSalesModel productSales = monoOfProductSales.doFirst(() -> System.out.println("> Preparation")).
				blockOptional(Duration.of(30, ChronoUnit.SECONDS)).
					orElseThrow(() -> new NotFoundException());
		
		assertNotNull(productSales);
	}

	@Test(expected=NotFoundException.class)
	public void testEmptyMono() {
		
		Mono<ProductSalesEntity> monoOfProductSales = getEmptyMono().
				switchIfEmpty(Mono.error(new NotFoundException())).
					log();
		
		monoOfProductSales.doFirst(() -> System.out.println(" > Start blockOptional with empty mono")).
				blockOptional(Duration.of(5, ChronoUnit.SECONDS));
	}
	
	public Mono<ProductSalesEntity> getEmptyMono() {
		return Mono.empty();
	}
	
	@Test
	public void testMonoFromCallable() {
		
		Mono.just(Integer.valueOf(1)).subscribe(i -> System.out.println(i));
		
		Mono.fromCallable(() -> HttpClients.createDefault().execute(new HttpGet("http://google.fr"))).
			subscribe(http -> {
				
				try {
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(http.getEntity().getContent()));
					
					String line = reader.readLine();
					while(line != null) {
						
						System.out.println(line);
						line = reader.readLine();
					}
					
				}
				catch(IOException ioe) {
					ioe.printStackTrace();
				}
		});
	}
	
	@Test
	public void testFluxParallel() {
		
		List<ProductSalesEntity> products = new ArrayList<>();
		Flux.range(1, 3000).publishOn(Schedulers.parallel()).map(ProductSalesEntity::new).subscribe(ps -> products.add(ps));
		
		assertThat(products).isNotEmpty();
		
		products.forEach(System.out::println);
	}
	
	@Data
	public static class ProductSalesEntity {
		
		private final Integer id;
		private final Integer amount;
		
		public ProductSalesEntity(Integer id) {
			
			this.id = id;
			this.amount = new Random().nextInt(10000);
		}
	}
	
	@Data @AllArgsConstructor @NoArgsConstructor
	public static class ProductSalesModel {
		
		private Integer id;
		private Integer amount;
	}
}
