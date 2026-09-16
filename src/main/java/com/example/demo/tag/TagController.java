package com.example.demo.tag;

import com.example.demo.tag.dto.TagResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagRepository repository;

    public TagController(TagRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TagResponse> getAllTags() {
        return repository.findAll().stream()
                .map(t -> new TagResponse(t.getId(), t.getName()))
                .toList();
    }
}