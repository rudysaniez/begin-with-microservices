package com.me.microservices.core.review.repository;

import java.io.Serializable;

import org.reactivestreams.Publisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.me.handler.exception.InvalidInputException;
import com.me.handler.exception.NotFoundException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

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
		
		return Mono.just(entity).flux().publishOn(scheduler).
			transform(flux -> flux.map(repo::save)).
			subscribeOn(scheduler).publish().autoConnect(0).single();
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends T> Flux<S> saveAll(Iterable<S> entities) {
		
		return Mono.just(entities).flux().publishOn(scheduler).
				transform(flux -> flux.map(repo::saveAll).flatMap(Flux::fromIterable)).
				subscribeOn(scheduler).publish().autoConnect(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends T> Flux<S> saveAll(Publisher<S> entityStream) {
		
		return Mono.from(entityStream).flux().publishOn(scheduler).
				transform(flux -> flux.map(repo::save).
						onErrorMap(DataIntegrityViolationException.class, e -> new InvalidInputException())).
				subscribeOn(scheduler).publish().autoConnect(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<T> findById(ID id) {
		
		return Mono.just(id).flux().publishOn(scheduler).
				transform(flux -> flux.map(repo::findById).
						map(opt -> opt.orElseThrow(() -> new NotFoundException()))).
				subscribeOn(scheduler).publish().autoConnect(0).single();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<T> findById(Publisher<ID> id) {
		
		return Mono.from(id).flux().publishOn(scheduler).
				transform(flux -> flux.map(repo::findById).
						map(opt -> opt.orElseThrow(() -> new NotFoundException()))).
				subscribeOn(scheduler).publish().autoConnect(0).single();
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Boolean> existsById(ID id) {
		
		return Mono.just(id).flux().publishOn(scheduler).
				transform(flux -> flux.map(repo::existsById).onErrorReturn(false)).
				subscribeOn(scheduler).publish().autoConnect(0).single();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Boolean> existsById(Publisher<ID> id) {
		
		return Mono.from(id).flux().publishOn(scheduler).
				transform(flux -> flux.map(repo::existsById).onErrorReturn(false)).
				subscribeOn(scheduler).publish().autoConnect(0).single();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Flux<T> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Flux<T> findAllById(Iterable<ID> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Flux<T> findAllById(Publisher<ID> idStream) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Long> count() {
		
		return Mono.<Long>empty().publishOn(scheduler).
				concatWith( Mono.defer( () -> Mono.fromSupplier( repo::count))).
				subscribeOn(scheduler).publish().autoConnect(0).
				single();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Mono<Void> deleteById(ID id) {
		throw new UnsupportedOperationException();
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
