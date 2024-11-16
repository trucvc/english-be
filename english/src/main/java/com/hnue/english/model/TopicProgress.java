package com.hnue.english.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "topic_progress")
@Data
public class TopicProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private int id;

    @Column(name = "is_completed")
    private int isCompleted;

    @Column(name = "completed_at")
    private Date completedAt;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "topic_id")
    private Topic topic;

    public TopicProgress(){

    }

    public TopicProgress(int isCompleted, Date completedAt) {
        this.isCompleted = isCompleted;
        this.completedAt = completedAt;
    }
}
