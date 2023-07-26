package io.ballerina.projects.internal.repositories;

import java.util.Map;

public class CustomPkgRepositoryContainer {
    Map<String, CustomPackageRepository> customPackageRepositories;
    public CustomPkgRepositoryContainer(Map<String, CustomPackageRepository> customPackageRepositories) {
        this.customPackageRepositories = customPackageRepositories;
    }

    public Map<String, CustomPackageRepository> getCustomPackageRepositories() {
        return customPackageRepositories;
    }
}
