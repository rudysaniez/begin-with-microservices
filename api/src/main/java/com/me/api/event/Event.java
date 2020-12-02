package com.me.api.event;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class Event<K extends Number> {

	private K key;
	private LocalDateTime creationDate;
	private Event.Type type;
	
	public enum Type {DELETE;}
	
	public Event() {
		
		this.key = null;
		this.type = Event.Type.DELETE;
		this.creationDate = LocalDateTime.now();
	}
	
	/**
	 * @param key
	 * @param type
	 */
	public Event(K key, Event.Type type) {
		
		this.key = key;
		this.type = type;
		this.creationDate = LocalDateTime.now();
	}
}
