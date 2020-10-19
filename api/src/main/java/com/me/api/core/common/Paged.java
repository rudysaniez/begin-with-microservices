package com.me.api.core.common;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class Paged<T> {

	private List<T> content;
	PageMetadata page;
}
