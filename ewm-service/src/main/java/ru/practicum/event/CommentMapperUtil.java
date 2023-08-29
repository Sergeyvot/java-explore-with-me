package ru.practicum.event;

import lombok.experimental.UtilityClass;
import ru.practicum.event.dto.CommentDto;
import ru.practicum.event.dto.NewCommentDto;
import ru.practicum.event.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.time.ZoneId;

@UtilityClass
public class CommentMapperUtil {

    public Comment toComment(NewCommentDto newCommentDto, User author, Event event) {
        Comment.CommentBuilder comment = Comment.builder();

        comment.text(newCommentDto.getText());
        comment.author(author);
        comment.event(event);
        comment.stateComment(StateComment.PENDING);

        return comment.build();
    }

    public CommentDto toCommentDto(Comment comment) {
        CommentDto.CommentDtoBuilder commentDto = CommentDto.builder();

        commentDto.id(comment.getId());
        commentDto.text(comment.getText());
        commentDto.authorName(comment.getAuthor().getName());

        if (comment.getPublishedOn() != null) {
            commentDto.publishedOn(LocalDateTime.ofInstant(comment.getPublishedOn(), ZoneId.systemDefault()));
        }

        return commentDto.build();
    }
}
