package com.example.demo.bookmark;

import com.example.demo.tag.Tag;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bookmarks")
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "URL is required")
    @Size(max = 500, message = "URL must be at most 500 characters")
    @Column(nullable = false)
    private String url;

    @Column(length = 2000)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "bookmark_tags",
            joinColumns = @JoinColumn(name = "bookmark_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    public Bookmark() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }

    // Helpers for bidirectional management
    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }
}