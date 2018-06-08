package org.wso2.ballerinalang.compiler;

import java.util.List;

/**
 * Class to handle packages in Ballerina.lock.
 */
public class LockFilePackage {
    private String org;
    private String name;
    private String version;
    private List<LockFilePackage> dependencyPackages;

    /**
     * Constructor.
     *
     * @param org     org-name
     * @param name    package name
     * @param version package version
     */
    LockFilePackage(String org, String name, String version) {
        this.org = org;
        this.name = name;
        this.version = version;
    }

    /**
     * Get package name.
     *
     * @return package name
     */
    public String getName() {
        return name;
    }

    /**
     * Get package version.
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get imported packages as a string.
     *
     * @return imported packages as a string
     */
    public List<LockFilePackage> getDependencies() {
        return dependencyPackages;
    }

    /**
     * Set dependencies of packages.
     *
     * @param dependencyPackages dependency package list
     */
    void setDependencyPackages(List<LockFilePackage> dependencyPackages) {
        this.dependencyPackages = dependencyPackages;
    }

    /**
     * Get org-name of the package.
     *
     * @return org-name
     */
    public String getOrg() {
        return org;
    }
}
