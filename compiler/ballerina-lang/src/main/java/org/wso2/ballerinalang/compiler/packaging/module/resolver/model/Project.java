/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

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

/**
 * Represents Ballerina project in module resolver.
 */
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
