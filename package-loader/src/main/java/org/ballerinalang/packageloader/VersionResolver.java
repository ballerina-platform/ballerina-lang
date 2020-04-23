package org.ballerinalang.packageloader;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.LockFileProcessor;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

public class VersionResolver {

    private final Path projectDir;
    private final LockFile lockFile;
    private final Manifest manifest;
    private final Map<PackageID, Manifest> dependencyManifests;

    public VersionResolver(CompilerContext context, Map<PackageID, Manifest> dependencyManifests) {
        CompilerOptions options = CompilerOptions.getInstance(context);
        this.projectDir = Paths.get(options.get(PROJECT_DIR));
        this.lockFile = LockFileProcessor.getInstance(context, Boolean.parseBoolean(options.get(LOCK_ENABLED))).getLockFile();
        this.manifest = ManifestProcessor.getInstance(context).getManifest();
        this.dependencyManifests = dependencyManifests;
    }

    public String resolve(PackageID moduleId, PackageID enclModuleId) {
        // Check if module is an absolute version
        // Check if lock file exists
        if (enclModuleId != null && this.hasLockFile(Paths.get(String.valueOf(this.projectDir)))) {
            // Not a top level module or bal
            if (this.lockFile.getImports().containsKey(enclModuleId.toString())) {
                List<LockFileImport> foundBaseImport = lockFile.getImports().get(enclModuleId.toString());

                for (LockFileImport nestedImport : foundBaseImport) {
                    if (moduleId.orgName.value.equals(nestedImport.getOrgName()) && moduleId.name.value
                            .equals(nestedImport.getName())) {
                        // return lock file version
                        return  nestedImport.getVersion();
                    }
                }
            }
        }

        // Set version from the Ballerina.toml of the current project.
        if (enclModuleId != null && this.manifest != null) {

            for (Dependency dependency : this.manifest.getDependencies()) {
                if (dependency.getModuleName().equals(moduleId.name.value) && dependency.getOrgName()
                        .equals(moduleId.orgName.value) && dependency.getMetadata().getVersion() != null && !"*"
                        .equals(dependency.getMetadata().getVersion())) {
                    return dependency.getMetadata().getVersion();
                }
            }
        }

        // Set the version from Ballerina.toml found in dependent balos.
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

        // If it is an absolute version return
        // If it is a range check central
        // return version returned from central
        return null;
    }

    /**
     * Check if lock file is empty.
     *
     * @param sourceRoot The source root of the project.
     * @return True if lock file is valid, else false.
     */
    private boolean hasLockFile(Path sourceRoot) {
        return RepoUtils.isBallerinaProject(sourceRoot) && null != this.lockFile && null != this.lockFile.getImports()
                && this.lockFile.getImports().size() > 0;
    }
}
