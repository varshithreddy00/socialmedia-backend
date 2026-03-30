package com.socialmedia.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public PagedResponse() {}

    public static <T> PagedResponse<T> of(Page<T> pageData) {
        PagedResponse<T> response = new PagedResponse<>();
        response.content = pageData.getContent();
        response.page = pageData.getNumber();
        response.size = pageData.getSize();
        response.totalElements = pageData.getTotalElements();
        response.totalPages = pageData.getTotalPages();
        response.last = pageData.isLast();
        return response;
    }

    // ---- Getters & Setters ----

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }
}
