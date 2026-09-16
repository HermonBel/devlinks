package com.example.demo.bookmark;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("SELECT b FROM Bookmark b")
    @EntityGraph(attributePaths = "tags")
    List<Bookmark> findAllWithTags();

    @Query("SELECT b FROM Bookmark b WHERE b.id = :id")
    @EntityGraph(attributePaths = "tags")
    Optional<Bookmark> findByIdWithTags(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT b FROM Bookmark b
        LEFT JOIN b.tags t
        WHERE (:q IS NULL
               OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
               OR LOWER(b.description) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))
          AND (:tag IS NULL
               OR LOWER(t.name) = LOWER(CAST(:tag AS string)))
        """)
    @EntityGraph(attributePaths = "tags")
    Page<Bookmark> search(
            @Param("q") String q,
            @Param("tag") String tag,
            Pageable pageable
    );
}