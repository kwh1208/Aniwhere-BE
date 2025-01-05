package com.example.aniwhere.domain.anime;

import com.example.aniwhere.domain.casting.Casting;
import com.example.aniwhere.domain.category.Category;
import com.example.aniwhere.domain.episodes.Episodes;
import com.example.aniwhere.domain.review.Review;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "anime")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Anime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long animeId;

    private String title;
    private String director;
    private String characterDesign;
    private String musicDirector;
    private String animationDirector;
    private String script;
    private String producer;
    private String studio;
    private LocalDate releaseDate;
    private LocalDate endDate;
    @Column(name = "episodes")
    private Integer episodesNum;
    private String runningTime;
    private String status;
    private String trailer;
    private String description;
    private String poster;
    private Integer airingQuarter;
    private Boolean isAdult;
    private String duration;
    private String weekday;
    private String backgroundImage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "anime", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Casting> castings;

    @OneToMany(mappedBy = "anime", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;


    @ManyToMany
    @JoinTable(
            name = "animecategories",
            joinColumns = @JoinColumn(name = "anime_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    @OneToMany(mappedBy = "anime")
    private final List<Episodes> episodesList = new ArrayList<Episodes>();

    public void addEpisodes(Episodes episodes) {
        this.episodesList.add(episodes);
        if (episodes.getAnime() != this) {
            episodes.registerAnime(this);
        }
    }

    @OneToMany(mappedBy = "anime", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Episodes> episodes;
}
