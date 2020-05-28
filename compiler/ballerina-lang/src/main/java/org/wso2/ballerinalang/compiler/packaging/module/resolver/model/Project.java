package org.wso2.ballerinalang.compiler.packaging.module.resolver.model;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.toml.exceptions.TomlException;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.LockFileProcessor;
import org.ballerinalang.toml.parser.ManifestProcessor;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Project {

    private org.ballerinalang.toml.model.Project project;
    private Manifest manifest;
    public LockFile lockFile;

    public Project(Manifest manifest, LockFile lockFile) {
        this.manifest = manifest;
        this.project = manifest.getProject();
        this.lockFile = lockFile;
    }

    org.ballerinalang.toml.model.Project getBallerinaToml() {
        return project;
    }

//    public Set<ModuleId> getModules() {
//
//    }

    public void parseBallerinaToml(String toml) throws TomlException {
        manifest = ManifestProcessor.parseTomlContentFromString(toml);
    }

    public boolean hasLockFile() {
        return lockFile != null;
    }

    public LockFile getLockFile() {
        return lockFile;
    }

    public void parseLockFile(InputStream toml) {
        lockFile = LockFileProcessor.parseTomlContentAsStream(toml);
    }

    public boolean isModuleExists(PackageID moduleId) {
        Path modulePath = Paths.get(this.project.getRepository(), "src", moduleId.getName().getValue());
        return modulePath.toFile().exists();
    }

    public org.ballerinalang.toml.model.Project getProject() {
        return project;
    }

    public void setProject(org.ballerinalang.toml.model.Project project) {
        this.project = project;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }
}
