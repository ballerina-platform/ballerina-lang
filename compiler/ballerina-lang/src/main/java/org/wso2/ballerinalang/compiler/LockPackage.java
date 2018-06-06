package org.wso2.ballerinalang.compiler;

/**
 * Class to handle packages in Ballerina.lock.
 */
public class LockPackage {
    private String name;
    private String version;
    private String dependencies;

    /**
     * Constructor.
     *
     * @param name         package name
     * @param version      package version
     * @param dependencies imported packages as a string
     */
    LockPackage(String name, String version, String dependencies) {
        this.name = name;
        this.version = version;
        this.dependencies = dependencies;
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
    public String getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return name + " : " + version;
    }
}
