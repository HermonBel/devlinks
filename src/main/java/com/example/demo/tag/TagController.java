package com.example.demo.tag;

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
    public List<Tag> getAllTags() {
        return repository.findAll();
    }
}