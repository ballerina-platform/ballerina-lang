package io.ballerina.projects.internal.model;

public class Repository {
    private String id;
    private String url;
    private String username;
    private String password;

    private Repository(String id, String url, String username, String password) {
        this.id = id;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static Repository from(String id, String url, String username, String password) {
        return new Repository(id, url, username, password);
    }

    public static Repository from() {
        return new Repository("", "", "", "");
    }

    public String id() {
        return id;
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
