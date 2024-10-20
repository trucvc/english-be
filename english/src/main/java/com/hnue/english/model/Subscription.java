package com.hnue.english.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "subscription")
@Data
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private int id;

    @Column(name = "plan")
    private String plan;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private User user;

    public Subscription(){

    }

    public Subscription(String plan, Date startDate, Date endDate) {
        this.plan = plan;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
