package org.ballerinalang.moduleloader.model;

import org.ballerinalang.toml.exceptions.TomlException;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.LockFileProcessor;
import org.ballerinalang.toml.parser.ManifestProcessor;

import java.io.InputStream;
import java.util.Set;

public class Project {

    public org.ballerinalang.toml.model.Project project;
    public Manifest manifest;
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
}
