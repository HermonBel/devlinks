package com.example.demo.bookmark.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record BookmarkRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be at most 100 characters")
        String title,

        @NotBlank(message = "URL is required")
        @Size(max = 500, message = "URL must be at most 500 characters")
        String url,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        Set<@Size(max = 50, message = "Tag names must be at most 50 characters") String> tags
) {}