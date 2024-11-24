package com.hnue.english.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "form_submission")
@Data
public class FormSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private int id;

    @Column(name = "form_type")
    private int formType;

    @Column(name = "content")
    private String content;

    @Column(name = "status")
    private int status;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @JsonManagedReference
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private User user;

    public FormSubmission(){

    }

    public FormSubmission(int formType, String content, int status) {
        this.formType = formType;
        this.content = content;
        this.status = status;
    }
}
