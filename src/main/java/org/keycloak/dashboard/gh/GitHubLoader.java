package org.keycloak.dashboard.gh;

import org.keycloak.dashboard.Config;
import org.keycloak.dashboard.rep.GitHubData;
import org.keycloak.dashboard.rep.GitHubIssue;
import org.keycloak.dashboard.util.DateUtil;
import org.kohsuke.github.GHException;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHPerson;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GitHubLoader {

    private GitHub gitHub;

    private GitHubIssuesLoader issuesLoader;

    private WorkflowRuntimeLoader workflowRuntimeLoader;

    public GitHubLoader() throws IOException {
        gitHub = GitHubBuilder.fromEnvironment().withJwtToken(TokenUtil.token()).build();
        issuesLoader = new GitHubIssuesLoader(gitHub);
        workflowRuntimeLoader = new WorkflowRuntimeLoader();
    }

    public GitHubData load() throws Exception {
        GitHubData data = new GitHubData();
        data.setAreas(queryAreas());

        // GitHub Action token doesn't have access to list team members, maintained manually in team-members.yml for now
        // data.setKeycloakDevelopers(queryDevTeam());

        data.setIssues(loadIssues());
        data.setPrs(loadPRs());
        data.setIssuesWithPr(queryIssuesWithPr());
        data.setPullRequestWaits(workflowRuntimeLoader.load());
        return data;
    }

    public GitHubData update(GitHubData data) throws Exception {
        data.setAreas(queryAreas());

        // GitHub Action token doesn't have access to list team members, maintained manually in team-members.yml for now
        // data.setKeycloakDevelopers(queryDevTeam());

        data.setIssues(updateIssues(data.getIssues()));
        data.setPrs(updatePRs(data.getPrs()));
        data.setIssuesWithPr(queryIssuesWithPr());

        if (data.getPullRequestWaits() == null || data.getPullRequestWaits().isEmpty()) {
            data.setPullRequestWaits(workflowRuntimeLoader.load());
        } else {
            data.setPullRequestWaits(workflowRuntimeLoader.update(data.getPullRequestWaits()));
        }

        return data;
    }

    private List<String> queryAreas() throws IOException {
        System.out.print("Fetching areas: ");
        List<String> areas = new LinkedList<>();
        GHRepository repository = gitHub.getRepository("keycloak/keycloak");
        for (GHLabel l : repository.listLabels()) {
            if (l.getName().startsWith("area/")) {
                areas.add(l.getName());
            }
        }
        System.out.println(".");
        return areas;
    }

    public List<String> queryTeam(String team) throws IOException {
        System.out.print("Fetching " + team + " team members: ");
        List<String> members = gitHub.getOrganization("keycloak").getTeamByName(team).getMembers().stream().map(GHPerson::getLogin).collect(Collectors.toList());
        System.out.println(".");
        return members;
    }

    private List<GitHubIssue> loadIssues() throws IOException {
        List<String> queries = new LinkedList<>();
        queries.add("repo:keycloak/keycloak is:issue is:open label:kind/bug");
        for (String month : DateUtil.monthStrings(Config.MAX_HISTORY)) {
            queries.add("repo:keycloak/keycloak is:issue is:closed label:kind/bug closed:" + month);
        }
        return issuesLoader.loadIssues(queries.toArray(new String[0]));
    }

    private List<GitHubIssue> updateIssues(List<GitHubIssue> issues) throws IOException {
        return issuesLoader.updateIssues(issues, "repo:keycloak/keycloak is:issue label:kind/bug");
    }

    private List<GitHubIssue> loadPRs() throws IOException {
        List<String> queries = new LinkedList<>();
        queries.add("repo:keycloak/keycloak is:pr is:open");
        for (String month : DateUtil.monthStrings(Config.MAX_HISTORY)) {
            queries.add("repo:keycloak/keycloak is:pr is:closed closed:" + month);
        }
        return issuesLoader.loadPRs(queries.toArray(new String[0]));
    }

    private List<GitHubIssue> updatePRs(List<GitHubIssue> issues) throws IOException {
        return issuesLoader.updatePRs(issues, "repo:keycloak/keycloak is:pr");
    }

    private int queryIssuesWithPr() throws IOException {
        System.out.print("Fetching bugs with PRs: ");
        int totalCount = gitHub.searchIssues().q("repo:keycloak/keycloak is:issue is:open label:kind/bug linked:pr").list().withPageSize(1).getTotalCount();
        System.out.println(".");
        return totalCount;
    }



}
