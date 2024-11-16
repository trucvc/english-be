package com.hnue.english.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "course_progress")
@Data
public class CourseProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private int id;

    @Column(name = "is_completed")
    private int isCompleted;

    @Column(name = "completed_at")
    private Date completedAt;

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "course_id")
    private Course course;

    public CourseProgress(){

    }

    public CourseProgress(int isCompleted, Date completedAt) {
        this.isCompleted = isCompleted;
        this.completedAt = completedAt;
    }
}
