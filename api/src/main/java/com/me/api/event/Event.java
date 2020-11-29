package com.me.api.event;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class Event<K,D> {

	private K key;
	private D data;
	private LocalDateTime creationDate;
	private Event.Type type;
	
	public enum Type {DELETE;}
	
	public Event() {
		
		this.key = null;
		this.data = null;
		this.type = Event.Type.DELETE;
		this.creationDate = LocalDateTime.now();
	}
	
	/**
	 * @param key
	 * @param type
	 */
	public Event(K key, Event.Type type) {
		
		this.key = key;
		this.data = null;
		this.type = type;
		this.creationDate = LocalDateTime.now();
	}
	
	/**
	 * @param key
	 * @param data
	 * @param type
	 */
	public Event(K key, D data, Event.Type type) {
		
		this.key = key;
		this.data = data;
		this.type = type;
		this.creationDate = LocalDateTime.now();
	}
}
