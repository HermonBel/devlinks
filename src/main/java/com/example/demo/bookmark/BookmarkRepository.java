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

    @Query("SELECT b FROM Bookmark b WHERE b.id = :id AND b.owner.id = :userId")
    @EntityGraph(attributePaths = "tags")
    Optional<Bookmark> findByIdWithTagsAndOwner(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    @Query("""
        SELECT b.id FROM Bookmark b
        LEFT JOIN b.tags t
        WHERE b.owner.id = :userId
          AND (:q IS NULL
               OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
               OR LOWER(b.description) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))
          AND (:tag IS NULL
               OR LOWER(t.name) = LOWER(CAST(:tag AS string)))
        GROUP BY b.id
        """)
    Page<Long> searchIdsForUser(
            @Param("userId") Long userId,
            @Param("q") String q,
            @Param("tag") String tag,
            Pageable pageable
    );

    @Query("SELECT b FROM Bookmark b WHERE b.id IN :ids")
    @EntityGraph(attributePaths = "tags")
    List<Bookmark> findAllWithTagsByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(b) > 0 FROM Bookmark b WHERE b.id = :id AND b.owner.id = :userId")
    boolean existsByIdAndOwner(
            @Param("id") Long id,
            @Param("userId") Long userId
    );
}