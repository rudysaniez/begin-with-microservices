package com.me.api.core.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Deprecated
@Data @AllArgsConstructor @NoArgsConstructor
public class PageMetadata   {
	
  private long size;
  private long totalElements;
  private long totalPages;
  private long number;
}
