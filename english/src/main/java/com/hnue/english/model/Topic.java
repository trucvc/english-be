package com.hnue.english.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "topic")
@Data
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private int id;

    @Column(name = "topic_name")
    private String topicName;

    @Column(name = "description")
    private String description;

    @Column(name = "display_order")
    private int displayOrder;

    @Column(name = "image")
    private String image;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @JsonBackReference
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(mappedBy = "topic", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Vocabulary> vocabularies;

    public Topic(){

    }

    public Topic(String topicName, String description, int displayOrder) {
        this.topicName = topicName;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    public void add(Vocabulary vocabulary){
        if (vocabularies == null){
            vocabularies = new ArrayList<>();
        }
        vocabularies.add(vocabulary);
        vocabulary.setTopic(this);
    }
}
