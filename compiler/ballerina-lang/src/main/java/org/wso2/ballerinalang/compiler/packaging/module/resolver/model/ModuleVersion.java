package org.wso2.ballerinalang.compiler.packaging.module.resolver.model;

import org.wso2.ballerinalang.compiler.packaging.module.resolver.Repo;

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
