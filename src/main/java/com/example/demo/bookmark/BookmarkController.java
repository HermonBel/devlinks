package com.example.demo.bookmark;

import com.example.demo.bookmark.dto.BookmarkRequest;
import com.example.demo.bookmark.dto.BookmarkResponse;
import com.example.demo.error.ApiError;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService service;
    private final UserRepository userRepository;

    public BookmarkController(BookmarkService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Page<BookmarkResponse> getAllBookmarks(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Long userId = currentUserId(principal);
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return service.search(userId, q, tag, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookmark(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = currentUserId(principal);
        return service.findById(id, userId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiError(404, "Not Found",
                                "Bookmark " + id + " not found",
                                request.getRequestURI())));
    }

    @PostMapping
    public ResponseEntity<BookmarkResponse> createBookmark(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody BookmarkRequest req) {
        Long userId = currentUserId(principal);
        return new ResponseEntity<>(service.create(userId, req), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBookmark(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody BookmarkRequest req,
            HttpServletRequest request) {
        Long userId = currentUserId(principal);
        return service.update(id, userId, req)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiError(404, "Not Found",
                                "Bookmark " + id + " not found",
                                request.getRequestURI())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBookmark(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = currentUserId(principal);
        if (service.delete(id, userId)) return ResponseEntity.noContent().build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(404, "Not Found",
                        "Bookmark " + id + " not found",
                        request.getRequestURI()));
    }

    /**
     * Resolves the authenticated principal's email to the user's id.
     * One indexed DB hit per request — negligible cost.
     */
    private Long currentUserId(UserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("No authenticated user");
        }
        return userRepository.findByEmail(principal.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + principal.getUsername()));
    }
}