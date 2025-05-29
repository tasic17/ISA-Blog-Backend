package com.example.demo.models;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentModel {
    private Integer id;
    private Integer postId;
    private Integer userId;
    private String userName;
    private String userProfilePicture;
    private Integer parentCommentId;
    private String content;
    private Boolean isApproved;
    private LocalDateTime createdAt;
    private List<CommentModel> replies;
}
