package org.keycloak.dashboard.rep;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public class GitHubData {

    public Date updatedDate;

    @JsonProperty
    public List<String> areas;
    @JsonProperty
    public List<GitHubIssue> issues;
    @JsonProperty
    public List<GitHubIssue> prs;

    public int issuesWithPr;

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public List<String> getAreas() {
        return areas;
    }

    public void setAreas(List<String> areas) {
        this.areas = areas;
    }

    public List<GitHubIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<GitHubIssue> issues) {
        this.issues = issues;
    }

    public List<GitHubIssue> getPrs() {
        return prs;
    }

    public void setPrs(List<GitHubIssue> prs) {
        this.prs = prs;
    }

    public int getIssuesWithPr() {
        return issuesWithPr;
    }

    public void setIssuesWithPr(int issuesWithPr) {
        this.issuesWithPr = issuesWithPr;
    }
}
