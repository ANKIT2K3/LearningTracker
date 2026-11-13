package com.LearningTracker.Backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "learners")
public class Learner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "account_id",
        nullable = false,
        unique = true
    )
    private Account account;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(nullable = false, length = 100)
    private String cohort;

    public Learner() {
    }

    public Learner(Account account, String displayName, String cohort) {
        this.account = account;
        this.displayName = displayName;
        this.cohort = cohort;
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCohort() {
        return cohort;
    }

    public void setCohort(String cohort) {
        this.cohort = cohort;
    }
}
