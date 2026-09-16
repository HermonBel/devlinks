package com.example.demo.bookmark;

import com.example.demo.bookmark.dto.BookmarkRequest;
import com.example.demo.bookmark.dto.BookmarkResponse;
import com.example.demo.tag.dto.TagResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookmarkMapper {

    public Bookmark toEntity(BookmarkRequest request) {
        Bookmark bookmark = new Bookmark();
        bookmark.setTitle(request.title());
        bookmark.setUrl(request.url());
        bookmark.setDescription(request.description());
        return bookmark;
    }

    public BookmarkResponse toResponse(Bookmark bookmark) {
        Set<TagResponse> tags = bookmark.getTags().stream()
                .map(t -> new TagResponse(t.getId(), t.getName()))
                .collect(Collectors.toSet());
        return new BookmarkResponse(
                bookmark.getId(),
                bookmark.getTitle(),
                bookmark.getUrl(),
                bookmark.getDescription(),
                tags
        );
    }
}