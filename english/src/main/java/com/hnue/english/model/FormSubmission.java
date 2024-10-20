package com.hnue.english.model;

import jakarta.persistence.*;
import lombok.Data;

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
