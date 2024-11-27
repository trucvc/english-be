package com.hnue.english.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "user_progress")
@Data
public class UserProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private int id;

    @Column(name = "last_reviewed")
    private Date lastReviewed;

    @Column(name = "next_review")
    private Date nextReview;

    @Column(name = "proficiency_level")
    private int level;

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private User user;

    @JsonManagedReference
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "vocab_id")
    private Vocabulary vocabulary;

    public UserProgress(){

    }

    public UserProgress(Date lastReviewed, Date nextReview) {
        this.lastReviewed = lastReviewed;
        this.nextReview = nextReview;
    }
}
