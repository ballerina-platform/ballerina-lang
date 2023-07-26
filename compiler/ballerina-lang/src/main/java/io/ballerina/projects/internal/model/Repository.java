package io.ballerina.projects.internal.model;

public class Repository {
    private String id;
    private String url;
    private String repoName;
    private String username;
    private String password;

    private Repository(String id, String repoName,String url, String username, String password) {
        this.id = id;
        this.url = url;
        this.repoName = repoName;
        this.username = username;
        this.password = password;
    }

    public static Repository from(String id, String repoName,String url, String username, String password) {
        return new Repository(id, repoName, url, username, password);
    }

    public static Repository from() {
        return new Repository("", "", "", "", "");
    }

    public String id() {
        return id;
    }

    public String repository() {
        return repoName;
    }

    public String url() {
        return url;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }
}
