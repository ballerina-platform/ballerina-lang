package io.ballerina.projects.internal.repositories;

public interface CustomRepositoryHelper {
    boolean getPackageFromRemoteRepo(String org, String name, String version);
}
