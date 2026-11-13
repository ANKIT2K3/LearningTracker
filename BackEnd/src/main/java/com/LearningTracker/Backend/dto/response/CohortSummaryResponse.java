package com.LearningTracker.Backend.dto.response;

public class CohortSummaryResponse {

    private String cohort;
    private long totalAssignments;
    private long completedAssignments;

    public CohortSummaryResponse() {}

    public CohortSummaryResponse(String cohort, long totalAssignments, long completedAssignments) {
        this.cohort = cohort;
        this.totalAssignments = totalAssignments;
        this.completedAssignments = completedAssignments;
    }

    public String getCohort() { return cohort; }
    public void setCohort(String cohort) { this.cohort = cohort; }

    public long getTotalAssignments() { return totalAssignments; }
    public void setTotalAssignments(long totalAssignments) { this.totalAssignments = totalAssignments; }

    public long getCompletedAssignments() { return completedAssignments; }
    public void setCompletedAssignments(long completedAssignments) { this.completedAssignments = completedAssignments; }
}
