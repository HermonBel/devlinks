package com.example.demo.tag;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tags", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (name != null) name = name.trim().toLowerCase();
    }

    @NotBlank(message = "Tag name is required")
    @Size(max = 50, message = "Tag name must be at most 50 characters")
    @Column(nullable = false, unique = true)
    private String name;

    public Tag() {}

    public Tag(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return id != null && id.equals(tag.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}