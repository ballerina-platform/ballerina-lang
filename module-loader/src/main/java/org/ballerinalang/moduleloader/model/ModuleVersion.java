package org.ballerinalang.moduleloader.model;

import org.ballerinalang.moduleloader.Repo;

public class ModuleVersion {
    private Repo repo;
    private String version;

    public ModuleVersion(Repo repo, String version) {
        this.repo = repo;
        this.version = version;
    }

    public Repo getRepo() {
        return repo;
    }

    public void setRepo(Repo repo) {
        this.repo = repo;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
