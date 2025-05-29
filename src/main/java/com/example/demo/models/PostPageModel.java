package com.example.demo.models;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PostPageModel {
    private List<PostModel> posts;
    private int totalPages;
    private Long totalElements;
    private int currentPage;
    private int pageSize;
}