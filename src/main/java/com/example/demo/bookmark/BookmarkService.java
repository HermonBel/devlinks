package com.example.demo.bookmark;

import com.example.demo.bookmark.dto.BookmarkRequest;
import com.example.demo.bookmark.dto.BookmarkResponse;
import com.example.demo.tag.Tag;
import com.example.demo.tag.TagRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookmarkService {

    private static final Logger log = LoggerFactory.getLogger(BookmarkService.class);

    private final BookmarkRepository repository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final BookmarkMapper mapper;

    public BookmarkService(BookmarkRepository repository,
                           TagRepository tagRepository,
                           UserRepository userRepository,
                           BookmarkMapper mapper) {
        this.repository = repository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<BookmarkResponse> search(Long userId, String q, String tag, Pageable pageable) {
        String normalizedQ = (q == null || q.isBlank()) ? null : q.trim();
        String normalizedTag = (tag == null || tag.isBlank()) ? null : tag.trim().toLowerCase();

        Page<Long> idPage = repository.searchIdsForUser(userId, normalizedQ, normalizedTag, pageable);

        if (idPage.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<Bookmark> bookmarks = repository.findAllWithTagsByIdIn(idPage.getContent());
        Map<Long, Bookmark> byId = bookmarks.stream()
                .collect(Collectors.toMap(Bookmark::getId, b -> b));

        List<BookmarkResponse> responses = idPage.getContent().stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(mapper::toResponse)
                .toList();

        return new PageImpl<>(responses, pageable, idPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Optional<BookmarkResponse> findById(Long id, Long userId) {
        return repository.findByIdWithTagsAndOwner(id, userId).map(mapper::toResponse);
    }

    @Transactional
    public BookmarkResponse create(Long userId, BookmarkRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        log.info("Creating bookmark for user {}: {}", userId, request.title());

        Bookmark bookmark = mapper.toEntity(request);
        bookmark.setOwner(owner);
        bookmark.setTags(resolveTags(request.tags()));

        Bookmark saved = repository.save(bookmark);
        return mapper.toResponse(saved);
    }

    @Transactional
    public Optional<BookmarkResponse> update(Long id, Long userId, BookmarkRequest request) {
        log.info("Updating bookmark id={} for user {}", id, userId);
        return repository.findByIdWithTagsAndOwner(id, userId).map(existing -> {
            existing.setTitle(request.title());
            existing.setUrl(request.url());
            existing.setDescription(request.description());
            existing.setTags(resolveTags(request.tags()));
            return mapper.toResponse(repository.save(existing));
        });
    }

    @Transactional
    public boolean delete(Long id, Long userId) {
        log.info("Deleting bookmark id={} for user {}", id, userId);
        if (!repository.existsByIdAndOwner(id, userId)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    private Set<Tag> resolveTags(Set<String> names) {
        if (names == null || names.isEmpty()) return new HashSet<>();

        Set<String> cleaned = names.stream()
                .filter(n -> n != null && !n.isBlank())
                .map(n -> n.trim().toLowerCase())
                .collect(Collectors.toSet());

        if (cleaned.isEmpty()) return new HashSet<>();

        Set<Tag> existing = tagRepository.findByNameIn(cleaned);
        Set<String> existingNames = existing.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<Tag> result = new HashSet<>(existing);
        for (String name : cleaned) {
            if (!existingNames.contains(name)) {
                result.add(tagRepository.save(new Tag(name)));
            }
        }
        return result;
    }
}