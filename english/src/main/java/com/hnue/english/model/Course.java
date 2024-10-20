package com.hnue.english.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course")
@Data
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private int id;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "description")
    private String description;

    @JsonManagedReference
    @OneToMany(mappedBy = "course", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Topic> topics;

    public Course(){

    }

    public Course(String courseName, String description) {
        this.courseName = courseName;
        this.description = description;
    }

    public void add(Topic topic){
        if (topics == null){
            topics = new ArrayList<>();
        }
        topics.add(topic);
        topic.setCourse(this);
    }
}
