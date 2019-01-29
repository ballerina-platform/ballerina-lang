package org.wso2.ballerinalang.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to handle packages in Ballerina.lock.
 *
 * @since 0.973.1
 */
public class LockFilePackage {
    private String org = "";
    private String name = "";
    private String version = "";
    private List<LockFilePackage> dependencyPackages = new ArrayList<>();

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
     * Default constructor.
     */
    public LockFilePackage() {
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
     * Set package name.
     *
     * @param name package name
     */
    public void setName(String name) {
        this.name = name;
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
     * Set package version.
     *
     * @param version version
     */
    public void setVersion(String version) {
        this.version = version;
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

    /**
     * Set the org-name of the package.
     *
     * @param org org-name of the package
     */
    public void setOrg(String org) {
        this.org = org;
    }

    /**
     * Remove duplicates from import packages list.
     *
     * @param list list of elements
     * @return import packages list without duplicates
     */
    private List<LockFilePackage> removeDuplicates(List<LockFilePackage> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Add a package to the imports list.
     *
     * @param lockFilePackage imported package
     */
    public void addImport(LockFilePackage lockFilePackage) {
        this.dependencyPackages.add(lockFilePackage);
        dependencyPackages = removeDuplicates(dependencyPackages);
    }
}
