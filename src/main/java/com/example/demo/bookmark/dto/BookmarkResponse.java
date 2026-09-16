package com.example.demo.bookmark.dto;

import com.example.demo.tag.dto.TagResponse;

import java.util.Set;

public record BookmarkResponse(
        Long id,
        String title,
        String url,
        String description,
        Set<TagResponse> tags
) {}