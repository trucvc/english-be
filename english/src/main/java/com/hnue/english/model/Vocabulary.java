package com.hnue.english.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "vocabulary")
@Data
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vocab_id")
    private int id;

    @Column(name = "word")
    private String word;

    @Column(name = "meaning")
    private String meaning;

    @Column(name = "example_sentence")
    private String exampleSentence;

    @Column(name = "pronunciation")
    private String pronunciation;

    @Column(name = "audio")
    private String audio;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @JsonManagedReference
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @JsonBackReference
    @OneToMany(mappedBy = "vocabulary", cascade = CascadeType.ALL)
    private List<UserProgress> userProgresses;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "folder_vocabulary",
                joinColumns = @JoinColumn(name = "vocab_id"),
                inverseJoinColumns = @JoinColumn(name = "folder_id"))
    private List<Folder> folders;

    public Vocabulary(){

    }

    public Vocabulary(String word, String meaning, String exampleSentence, String pronunciation) {
        this.word = word;
        this.meaning = meaning;
        this.exampleSentence = exampleSentence;
        this.pronunciation = pronunciation;
    }

    public void addUserProgress(UserProgress theUserProgress){
        if (userProgresses == null){
            userProgresses = new ArrayList<>();
        }
        userProgresses.add(theUserProgress);
        theUserProgress.setVocabulary(this);
    }

    public void addFolder(Folder theFolder){
        if (folders == null){
            folders = new ArrayList<>();
        }
        folders.add(theFolder);
    }
}
