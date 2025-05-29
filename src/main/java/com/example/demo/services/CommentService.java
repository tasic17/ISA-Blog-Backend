package com.example.demo.services;

import com.example.demo.entities.Comment;
import com.example.demo.entities.Post;
import com.example.demo.entities.User;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.mappers.CommentMapper;
import com.example.demo.models.CommentCreateModel;
import com.example.demo.models.CommentModel;
import com.example.demo.repositories.ICommentRepository;
import com.example.demo.repositories.IPostRepository;
import com.example.demo.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final ICommentRepository commentRepository;
    private final IPostRepository postRepository;
    private final IUserRepository userRepository;

    public List<CommentModel> getPostComments(Integer postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return CommentMapper.toModelList(comments);
    }

    public Page<CommentModel> getPostCommentsPageable(Integer postId, int page, int size) {
        Page<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNull(
                postId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return comments.map(CommentMapper::toModel);
    }

    @Transactional
    public CommentModel createComment(CommentCreateModel model) {
        User user = getCurrentUser();

        Post post = postRepository.findById(model.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(model.getContent());

        if (model.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(model.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }

        comment = commentRepository.save(comment);
        return CommentMapper.toModel(comment);
    }

    @Transactional
    public CommentModel updateComment(Integer id, String content) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        User currentUser = getCurrentUser();

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("User is not authorized to update this comment");
        }

        comment.setContent(content);
        comment = commentRepository.save(comment);

        return CommentMapper.toModel(comment);
    }

    @Transactional
    public void deleteComment(Integer id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        User currentUser = getCurrentUser();

        if (!currentUser.isAdmin() && !comment.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("User is not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public void approveComment(Integer id, boolean approve) {
        User currentUser = getCurrentUser();

        if (!currentUser.isAdmin()) {
            throw new UnauthorizedException("Only admin can approve comments");
        }

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        comment.setIsApproved(approve);
        commentRepository.save(comment);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}