package com.hnue.english.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "folder")
@Data
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private int id;

    @Column(name = "folder_name")
    private String folderName;

    @Column(name = "description")
    private String description;

    @JsonManagedReference
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private User user;

    @JsonManagedReference
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "folder_vocabulary",
                joinColumns = @JoinColumn(name = "folder_id"),
                inverseJoinColumns = @JoinColumn(name = "vocab_id"))
    private List<Vocabulary> vocabularies;

    public Folder(){

    }

    public Folder(String folderName, String description) {
        this.folderName = folderName;
        this.description = description;
    }

    public void addVocabulary(Vocabulary theVocabulary){
        if (vocabularies == null){
            vocabularies = new ArrayList<>();
        }
        vocabularies.add(theVocabulary);
    }
}
