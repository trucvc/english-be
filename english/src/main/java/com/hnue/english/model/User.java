package com.hnue.english.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user")
@Data
public class User implements UserDetails{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int userId;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "subscription_plan")
    private String subscriptionPlan;

    @Column(name = "subscription_start_date")
    private Date subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private Date subscriptionEndDate;

    @Column(name = "role")
    private String role;

    @Column(name = "paid")
    private int paid;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserProgress> userProgresses;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Folder> folders;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Subscription subscription;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<FormSubmission> formSubmissions;

    public User(){

    }

    public User(String email, String password, String fullName, String subscriptionPlan, Date subscriptionStartDate, Date subscriptionEndDate, String role, int paid) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.subscriptionPlan = subscriptionPlan;
        this.subscriptionStartDate = subscriptionStartDate;
        this.subscriptionEndDate = subscriptionEndDate;
        this.role = role;
        this.paid = paid;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(getRole()));
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void addUserProgress(UserProgress theUserProgress){
        if (userProgresses == null){
            userProgresses = new ArrayList<>();
        }
        userProgresses.add(theUserProgress);
        theUserProgress.setUser(this);
    }

    public void addFolder(Folder theFolder){
        if (folders == null){
            folders = new ArrayList<>();
        }
        folders.add(theFolder);
        theFolder.setUser(this);
    }

    public void addFormSubmission(FormSubmission theFormSubmission){
        if (formSubmissions == null){
            formSubmissions = new ArrayList<>();
        }
        formSubmissions.add(theFormSubmission);
        theFormSubmission.setUser(this);
    }
}
