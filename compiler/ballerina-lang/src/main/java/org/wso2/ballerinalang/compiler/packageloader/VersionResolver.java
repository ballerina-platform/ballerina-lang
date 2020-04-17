package org.wso2.ballerinalang.compiler.packageloader;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class VersionResolver {

    private final String projectDir;
    private final LockFile lockFile;
    private final Manifest manifest;
    private final Map<PackageID, Manifest> dependencyManifests;

    public VersionResolver(String projectDir, LockFile lockFile, Manifest manifest,
            Map<PackageID, Manifest> dependencyManifests) {
        this.projectDir = projectDir;
        this.lockFile = lockFile;
        this.manifest = manifest;
        this.dependencyManifests = dependencyManifests;
    }

    public String resolve(PackageID moduleId, PackageID enclModuleId) {
        String version;
        version = resolveVersionFromBalLockFile(moduleId, enclModuleId);
        if (version == null) {
            version = resolveModuleFromBalToml(moduleId, enclModuleId);
        }
        if (version == null) {
            version = resolveVersionFromDependentBalosBalToml(moduleId, enclModuleId);
        }
        return version;
    }

    /**
     * Set the version from the Ballerina.lock file found in the current project.
     */
    private String resolveVersionFromBalLockFile(PackageID moduleId, PackageID enclModuleId) {
        if (enclModuleId != null && this.hasLockFile(Paths.get(this.projectDir))
                && this.lockFile.getImports().containsKey(enclModuleId.toString())) {
            // Not a top level package or bal
            List<LockFileImport> foundBaseImport = lockFile.getImports().get(enclModuleId.toString());

            for (LockFileImport nestedImport : foundBaseImport) {
                if (moduleId.orgName.value.equals(nestedImport.getOrgName()) && moduleId.name.value
                        .equals(nestedImport.getName())) {
                    return nestedImport.getVersion();
                }
            }
        }
        return null;
    }

    /**
     * Set version from the Ballerina.toml of the current project.
     */
    private String resolveModuleFromBalToml(PackageID moduleId, PackageID enclModuleId) {
        if (enclModuleId != null && this.manifest != null) {
            for (Dependency dependency : this.manifest.getDependencies()) {
                if (dependency.getModuleName().equals(moduleId.name.value)
                        && dependency.getOrgName().equals(moduleId.orgName.value)
                        && dependency.getMetadata().getVersion() != null
                        && !"*".equals(dependency.getMetadata().getVersion())) {
                    return dependency.getMetadata().getVersion();
                }
            }
        }
        return null;
    }

    /**
     * Set the version from Ballerina.toml found in dependent balos.
     */
    private String resolveVersionFromDependentBalosBalToml(PackageID moduleId, PackageID enclModuleId) {
        if (enclModuleId != null && this.dependencyManifests.size() > 0 && this.dependencyManifests
                .containsKey(enclModuleId)) {
            for (Dependency manifestDependency : this.dependencyManifests.get(enclModuleId).getDependencies()) {
                if (manifestDependency.getOrgName().equals(moduleId.orgName.value)
                        && manifestDependency.getModuleName().equals(moduleId.name.value)
                        && manifestDependency.getMetadata().getVersion() != null
                        && !"*".equals(manifestDependency.getMetadata().getVersion())) {
                    return manifestDependency.getMetadata().getVersion();
                }
            }
        }
        return null;
    }

    /**
     * Check if lock file is empty.
     *
     * @param sourceRoot The sourceroot of the project.
     * @return True if lock file is valid, else false.
     */
    private boolean hasLockFile(Path sourceRoot) {
        return RepoUtils.isBallerinaProject(sourceRoot) && null != this.lockFile && null != this.lockFile.getImports()
                && this.lockFile.getImports().size() > 0;
    }
}
