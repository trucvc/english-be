package com.hnue.english.model;

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

    @Column(name = "review_interval")
    private int reviewInterval;

    @Column(name = "last_reviewed")
    private Date lastReviewed;

    @Column(name = "next_review")
    private Date nextReview;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "vocab_id")
    private Vocabulary vocabulary;

    public UserProgress(){

    }

    public UserProgress(int reviewInterval, Date lastReviewed, Date nextReview) {
        this.reviewInterval = reviewInterval;
        this.lastReviewed = lastReviewed;
        this.nextReview = nextReview;
    }
}
