package com.example.demo.bookmark;

import com.example.demo.bookmark.dto.BookmarkRequest;
import com.example.demo.bookmark.dto.BookmarkResponse;
import com.example.demo.tag.Tag;
import com.example.demo.tag.TagRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookmarkMapper mapper;

    @InjectMocks
    private BookmarkService bookmarkService;

    private User testUser;
    private Bookmark testBookmark;
    private BookmarkRequest testRequest;
    private BookmarkResponse testResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setDisplayName("Test User");

        testBookmark = new Bookmark();
        testBookmark.setId(10L);
        testBookmark.setTitle("Spring Docs");
        testBookmark.setUrl("https://spring.io");
        testBookmark.setOwner(testUser);
        testBookmark.setTags(new HashSet<>());

        testRequest = new BookmarkRequest(
                "Spring Docs",
                "https://spring.io",
                "Reference",
                Set.of("java", "spring")
        );

        testResponse = new BookmarkResponse(
                10L, "Spring Docs", "https://spring.io", "Reference", Set.of()
        );
    }

    @Test
    void create_shouldAttachOwnerAndSave() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(mapper.toEntity(testRequest)).thenReturn(testBookmark);
        when(tagRepository.findByNameIn(any())).thenReturn(new HashSet<>());
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(testBookmark);
        when(mapper.toResponse(testBookmark)).thenReturn(testResponse);

        // When
        BookmarkResponse result = bookmarkService.create(1L, testRequest);

        // Then
        assertThat(result).isEqualTo(testResponse);
        assertThat(testBookmark.getOwner()).isEqualTo(testUser);
        verify(bookmarkRepository).save(testBookmark);
        verify(userRepository).findById(1L);
    }

    @Test
    void create_shouldCreateNewTagsThatDontExist() {
        // Given: tags "java" and "spring" don't exist yet
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(mapper.toEntity(testRequest)).thenReturn(testBookmark);
        when(tagRepository.findByNameIn(any())).thenReturn(new HashSet<>()); // none exist
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> {
            Tag tag = inv.getArgument(0);
            tag.setId(System.nanoTime()); // simulate id assignment
            return tag;
        });
        when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(testBookmark);
        when(mapper.toResponse(testBookmark)).thenReturn(testResponse);

        // When
        bookmarkService.create(1L, testRequest);

        // Then: two new tags saved
        verify(tagRepository, times(2)).save(any(Tag.class));
    }

    @Test
    void create_shouldReuseExistingTags() {
        // Given: "java" already exists
        Tag existingJava = new Tag("java");
        existingJava.setId(5L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(mapper.toEntity(testRequest)).thenReturn(testBookmark);
        when(tagRepository.findByNameIn(any())).thenReturn(Set.of(existingJava));
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(testBookmark);
        when(mapper.toResponse(testBookmark)).thenReturn(testResponse);

        // When
        bookmarkService.create(1L, testRequest);

        // Then: only "spring" is saved as new (java reused)
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    void findById_shouldReturnEmptyWhenNotOwnedByUser() {
        // Given
        when(bookmarkRepository.findByIdWithTagsAndOwner(99L, 1L)).thenReturn(Optional.empty());

        // When
        Optional<BookmarkResponse> result = bookmarkService.findById(99L, 1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void delete_shouldReturnFalseWhenNotOwned() {
        // Given
        when(bookmarkRepository.existsByIdAndOwner(99L, 1L)).thenReturn(false);

        // When
        boolean result = bookmarkService.delete(99L, 1L);

        // Then
        assertThat(result).isFalse();
        verify(bookmarkRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_shouldReturnTrueWhenOwned() {
        // Given
        when(bookmarkRepository.existsByIdAndOwner(10L, 1L)).thenReturn(true);

        // When
        boolean result = bookmarkService.delete(10L, 1L);

        // Then
        assertThat(result).isTrue();
        verify(bookmarkRepository).deleteById(10L);
    }

    @Test
    void search_shouldReturnEmptyPageWhenNoIdsMatch() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(bookmarkRepository.searchIdsForUser(eq(1L), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        // When
        Page<BookmarkResponse> result = bookmarkService.search(1L, null, null, pageable);

        // Then
        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(bookmarkRepository, never()).findAllWithTagsByIdIn(any());
    }

    @Test
    void update_shouldReturnEmptyWhenNotOwned() {
        // Given
        when(bookmarkRepository.findByIdWithTagsAndOwner(99L, 1L)).thenReturn(Optional.empty());

        // When
        Optional<BookmarkResponse> result = bookmarkService.update(99L, 1L, testRequest);

        // Then
        assertThat(result).isEmpty();
        verify(bookmarkRepository, never()).save(any());
    }
}