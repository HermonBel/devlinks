package com.example.demo.bookmark;

import com.example.demo.bookmark.dto.BookmarkRequest;
import com.example.demo.bookmark.dto.BookmarkResponse;
import com.example.demo.error.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService service;

    public BookmarkController(BookmarkService service) {
        this.service = service;
    }

    @GetMapping
    public Page<BookmarkResponse> getAllBookmarks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return service.search(q, tag, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookmark(@PathVariable Long id, HttpServletRequest request) {
        return service.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiError(404, "Not Found",
                                "Bookmark " + id + " not found",
                                request.getRequestURI())));
    }

    @PostMapping
    public ResponseEntity<BookmarkResponse> createBookmark(
            @Valid @RequestBody BookmarkRequest req) {
        return new ResponseEntity<>(service.create(req), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBookmark(
            @PathVariable Long id,
            @Valid @RequestBody BookmarkRequest req,
            HttpServletRequest request) {
        return service.update(id, req)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiError(404, "Not Found",
                                "Bookmark " + id + " not found",
                                request.getRequestURI())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBookmark(@PathVariable Long id, HttpServletRequest request) {
        if (service.delete(id)) return ResponseEntity.noContent().build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "Not Found",
                        "Bookmark " + id + " not found",
                        request.getRequestURI()));
    }
}