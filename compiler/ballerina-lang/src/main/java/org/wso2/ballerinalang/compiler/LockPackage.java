package org.wso2.ballerinalang.compiler;

import java.util.List;

/**
 * Class to handle packages in Ballerina.lock.
 */
public class LockPackage {
    private String org;
    private String name;
    private String version;
    private List<LockPackage> dependencyPackages;

    /**
     * Constructor.
     *
     * @param org     org-name
     * @param name    package name
     * @param version package version
     */
    LockPackage(String org, String name, String version) {
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
    public List<LockPackage> getDependencies() {
        return dependencyPackages;
    }

    @Override
    public String toString() {
        return "{" + LockFileConstants.ORG_NAME + "=\"" + org + "\", " + LockFileConstants.NAME + "=\""
                + name + "\"," + LockFileConstants.VERSION + "=\"" + version + "\"}";
    }

    /**
     * Set dependencies of packages.
     *
     * @param dependencyPackages dependency package list
     */
    void setDependencyPackages(List<LockPackage> dependencyPackages) {
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
