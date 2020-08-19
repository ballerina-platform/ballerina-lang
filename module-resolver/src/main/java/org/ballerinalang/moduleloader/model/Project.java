package org.ballerinalang.moduleloader.model;

import org.ballerinalang.toml.exceptions.TomlException;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.LockFileProcessor;
import org.ballerinalang.toml.parser.ManifestProcessor;

import java.io.InputStream;
import java.util.Set;

public class Project {

    private org.ballerinalang.toml.model.Project project;
    private Manifest manifest;
    public LockFile lockFile;

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

    public boolean isModuleExists(ModuleId moduleId) {
        return true;
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
