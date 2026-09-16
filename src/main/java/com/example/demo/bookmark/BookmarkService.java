package com.example.demo.bookmark;

import com.example.demo.tag.Tag;
import com.example.demo.tag.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class BookmarkService {

    private static final Logger log = LoggerFactory.getLogger(BookmarkService.class);

    private final BookmarkRepository repository;
    private final TagRepository tagRepository;

    public BookmarkService(BookmarkRepository repository, TagRepository tagRepository) {
        this.repository = repository;
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<Bookmark> findAll() {
        return repository.findAllWithTags();
    }

    @Transactional(readOnly = true)
    public Optional<Bookmark> findById(Long id) {
        return repository.findByIdWithTags(id);
    }

    @Transactional
    public Bookmark create(Bookmark bookmark) {
        log.info("Creating bookmark: {}", bookmark.getTitle());
        bookmark.setTags(resolveTags(bookmark.getTags()));
        return repository.save(bookmark);
    }

    @Transactional
    public Optional<Bookmark> update(Long id, Bookmark updated) {
        log.info("Updating bookmark id={}", id);
        return repository.findById(id).map(existing -> {
            existing.setTitle(updated.getTitle());
            existing.setUrl(updated.getUrl());
            existing.setDescription(updated.getDescription());
            existing.setTags(resolveTags(updated.getTags()));
            return repository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        log.info("Deleting bookmark id={}", id);
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }


    @Transactional(readOnly = true)
    public Page<Bookmark> search(String q, String tag, Pageable pageable) {
        String normalizedQ = (q == null || q.isBlank()) ? null : q.trim();
        String normalizedTag = (tag == null || tag.isBlank()) ? null : tag.trim().toLowerCase();
        return repository.search(normalizedQ, normalizedTag, pageable);
    }

    /**
     * Given a set of Tag objects that may or may not exist in the DB,
     * return a set of managed Tags: existing ones are reused, new ones created.
     */
    private Set<Tag> resolveTags(Set<Tag> incoming) {
        if (incoming == null || incoming.isEmpty()) return new HashSet<>();

        Set<String> names = incoming.stream()
                .map(Tag::getName)
                .map(String::trim)
                .map(String::toLowerCase)               // ← normalize case
                .filter(n -> !n.isEmpty())
                .collect(Collectors.toSet());

        if (names.isEmpty()) return new HashSet<>();

        // 1. Fetch existing tags in one query
        Set<Tag> existing = tagRepository.findByNameIn(names);
        Set<String> existingNames = existing.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        // 2. Save any new tags first
        Set<Tag> result = new HashSet<>(existing);
        for (String name : names) {
            if (!existingNames.contains(name)) {
                Tag saved = tagRepository.save(new Tag(name));   // ← SAVE IT
                result.add(saved);
            }
        }
        return result;
    }
}