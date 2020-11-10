package com.me.microservices.core.review.repository;

import java.io.Serializable;
import java.util.Optional;

import org.reactivestreams.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

/**
 * @author rudysaniez
 * @param <T>
 * @param <ID>
 */
@NoRepositoryBean
public class ReactiveJpaRepositoryImpl<T, ID extends Serializable> implements ReactiveCrudRepository<T, ID> {

	JpaRepository<T, ID> repo;
	Scheduler scheduler;
	
	public ReactiveJpaRepositoryImpl(JpaRepository<T, ID> repo, Scheduler scheduler) {
		this.repo = repo;
		this.scheduler = scheduler;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends T> Mono<S> save(S entity) {
		
		return Mono.just(entity).publishOn(scheduler).
			transform(m -> m.map(repo::save)).
			subscribeOn(scheduler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends T> Flux<S> saveAll(Iterable<S> entities) {
		
		return Flux.just(entities).publishOn(scheduler).
				transform(f -> f.flatMap(listOfEntities -> Flux.fromIterable(repo.saveAll(listOfEntities)))).
				subscribeOn(scheduler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends T> Flux<S> saveAll(Publisher<S> entityStream) {
		
		return Flux.from(entityStream).buffer().publishOn(scheduler).
				transform(flux -> flux.flatMap(listOfEntities -> Flux.fromIterable(repo.saveAll(listOfEntities)))).
				subscribeOn(scheduler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<T> findById(ID id) {
		
		return Mono.just(id).publishOn(scheduler).
				transform(m -> m.map(repo::findById).map(Optional::get).onErrorResume(e -> Mono.empty())).
				subscribeOn(scheduler);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<T> findById(Publisher<ID> id) {
		
		return Mono.from(id).publishOn(scheduler).
				transform(m -> m.map(repo::findById).map(Optional::get).onErrorResume(e -> Mono.empty())).
				subscribeOn(scheduler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Boolean> existsById(ID id) {
		
		return Mono.just(id).publishOn(scheduler).
				transform(m -> m.map(repo::existsById).onErrorReturn(false)).
				subscribeOn(scheduler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Boolean> existsById(Publisher<ID> id) {
		
		return Mono.from(id).publishOn(scheduler).
				transform(m -> m.map(repo::existsById).onErrorReturn(false)).
				subscribeOn(scheduler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Flux<T> findAll() {
		
		return Flux.<T>empty().publishOn(scheduler).
			concatWith(Flux.fromIterable(repo.findAll())).
			subscribeOn(scheduler).publish().autoConnect(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Flux<T> findAllById(Iterable<ID> ids) {

		return Mono.just(ids).flux().publishOn(scheduler).
			transform(flux -> flux.map(repo::findAllById).flatMap(Flux::fromIterable));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Flux<T> findAllById(Publisher<ID> idStream) {
		
		return Flux.from(idStream).buffer().publishOn(scheduler).
			transform(flux -> flux.flatMap(listOfIds -> Flux.fromIterable(repo.findAllById(listOfIds)))).
			subscribeOn(scheduler).publish().autoConnect(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Long> count() {
		
		return Mono.<Long>empty().publishOn(scheduler).
				concatWith( Mono.defer( () -> Mono.fromSupplier(repo::count))).
				subscribeOn(scheduler).single();
	}

	/**
	 * @param id
	 * @return mono of {@link Boolean}
	 */
	public Mono<Boolean> deleteEntityById(ID id) {
		
		return Mono.<Boolean>empty().publishOn(scheduler).
				concatWith(Mono.<Boolean>defer(() -> Mono.fromRunnable( () -> repo.deleteById(id)))).
				single(Boolean.TRUE);
	}

	/**
	 * @param entity
	 * @return mono of {@link Boolean}
	 */
	public Mono<Boolean> deleteEntity(T entity) {
		
		return Mono.<Boolean>empty().publishOn(scheduler).
				concatWith( Mono.<Boolean>defer( () -> Mono.fromRunnable( () -> repo.delete(entity)))).single(Boolean.TRUE);
	}

	/**
	 * @param entities
	 * @return mono of {@link Boolean}
	 */
	public Mono<Boolean> deleteAllEntities() {
		
		return Mono.<Boolean>empty().publishOn(scheduler).
				concatWith( Mono.<Boolean>defer( () -> Mono.fromRunnable( () -> repo.deleteAll()))).single(Boolean.TRUE);
	}
	
	/**
	 * @param entities
	 * @return mono of {@link Boolean}
	 */
	public Mono<Boolean> deleteAllEntities(Iterable<? extends T> entities) {
		
		return Mono.<Boolean>empty().publishOn(scheduler).
				concatWith( Mono.<Boolean>defer( () -> Mono.fromRunnable( () -> repo.deleteAll(entities)))).single(Boolean.TRUE);
	}
	
	/**
	 * Unsupported operations.
	 */

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Void> deleteById(ID id) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Void> deleteById(Publisher<ID> id) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Void> delete(T entity) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Void> deleteAll(Iterable<? extends T> entities) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Void> deleteAll(Publisher<? extends T> entityStream) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Void> deleteAll() {
		throw new UnsupportedOperationException();
	}
}
