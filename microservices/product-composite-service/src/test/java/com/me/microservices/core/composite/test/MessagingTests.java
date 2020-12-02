package com.me.microservices.core.composite.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.cloud.stream.test.matcher.MessageQueueMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.me.api.Api;
import com.me.api.event.Event;
import com.me.microservices.core.composite.producer.MessageProcessor;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class MessagingTests {

	@Autowired
	private MessageProcessor channels;
	
	@Autowired
	private MessageCollector messageCollector;
	
	@Autowired
	private WebTestClient client;
	
	private BlockingQueue<Message<?>> productQueue;
	private BlockingQueue<Message<?>> recommendationQueue;
	private BlockingQueue<Message<?>> reviewQueue;
	
	private static final Integer PRODUCT_ID = 100;
	
	@Before
	public void setup() {
		
		productQueue = messageCollector.forChannel(channels.outputProducts());
		recommendationQueue = messageCollector.forChannel(channels.outputRecommendations());
		reviewQueue = messageCollector.forChannel(channels.outputReviews());		
	}
	
	@Test
	public void deleteProducts() {
		
		client.delete().uri(uri -> uri.pathSegment("api", "v1", Api.PRODUCT_COMPOSITE_PATH, PRODUCT_ID.toString()).build()).
			accept(MediaType.APPLICATION_JSON).exchange().
				expectStatus().isEqualTo(HttpStatus.OK);
		
		assertEquals(productQueue.size(), 1);
		
		Matcher<String> eventMatcher = EventMatcher.isItTheSame(new Event<Integer>(PRODUCT_ID, Event.Type.DELETE));
		assertThat(productQueue, MessageQueueMatcher.receivesPayloadThat(eventMatcher));
		
		assertEquals(recommendationQueue.size(), 1);
		assertThat(recommendationQueue, MessageQueueMatcher.receivesPayloadThat(eventMatcher));
		
		assertEquals(reviewQueue.size(), 1);
		assertThat(reviewQueue, MessageQueueMatcher.receivesPayloadThat(eventMatcher));
	}
	
	/**
	 * Event matcher.
	 * @author rudysaniez
	 */
	public static class EventMatcher extends TypeSafeMatcher<String> {

		private final Event<?> expectedEvent;
		private final ObjectMapper jack = new ObjectMapper();
		
		private EventMatcher(Event<?> expectedEvent) {
			this.expectedEvent = expectedEvent;
		}
		
		/**
		 * @param expectedEvent
		 * @return {@link Matcher} of {@link String} parameter of type
		 */
		public static Matcher<String> isItTheSame(Event<?> expectedEvent) {
			return new EventMatcher(expectedEvent);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void describeTo(Description description) {
			description.appendText(eventToJson(expectedEvent));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean matchesSafely(String eventJson) {
			
			if(StringUtils.isEmpty(eventJson)) return false;
			
			Map<String, Object> actual = jsonToMapAndRemoveSomeFields(eventJson, List.of("creationDate", "data"));
			Map<String, Object> expected = eventToMapAndRemoveSomeFields(expectedEvent, List.of("creationDate", "data"));
			
			return actual.equals(expected);
		}
		
		/**
		 * @param json
		 * @return {@link Map} of {@link Event}
		 */
		protected Map<String, Object> jsonToMap(String json) {
			
			try {
				return jack.readValue(json, new TypeReference<HashMap<String, Object>>() {});
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		/**
		 * @param json
		 * @param fields
		 * @return {@link Map} that contains some data of the event
		 */
		protected Map<String, Object> jsonToMapAndRemoveSomeFields(String json, List<String> fields) {
			
			Map<String, Object> actual = jsonToMap(json);
			
			if(fields != null && !fields.isEmpty()) fields.forEach(f -> actual.remove(f));
			
			return actual;
		}
		
		/**
		 * @param event
		 * @return {@link Map} that contains data of the event
		 */
		protected Map<String, Object> eventToMap(Event<?> event)   {
			return jack.convertValue(event, new TypeReference<HashMap<String, Object>>() {});
		}
		
		/**
		 * @param event
		 * @param fields
		 * @return {@link Map} that contains some data of the event
		 */
		protected Map<String, Object> eventToMapAndRemoveSomeFields(Event<?> event, List<String> fields) {
			
			Map<String, Object> actual = eventToMap(event);
			
			if(fields != null && !fields.isEmpty()) fields.forEach(f -> actual.remove(f));
			
			return actual;
		}
		
		/**
		 * @param event
		 * @return event to json
		 */
		protected String eventToJson(Event<?> event) {
			
			if(event == null) throw new IllegalArgumentException("The argument must not be null");
			
			try {
				return jack.writeValueAsString(event);
			}
			catch(JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
