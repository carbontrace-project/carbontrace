package com.carbontrace.common;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pagination envelope placed inside {@link ApiResponse#getData()} for paged list
 * endpoints (COMMANDO.md Section 11).
 *
 * @param <T> element type of {@code content}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
