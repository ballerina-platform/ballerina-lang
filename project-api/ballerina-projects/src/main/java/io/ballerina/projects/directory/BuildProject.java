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
package io.ballerina.projects.directory;

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.Project;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * {@code BuildProject} represents Ballerina project instance created from the project directory.
 *
 * @since 2.0.0
 */
public class BuildProject extends Project {
    private LockFile lockFile; // related to build command?

    public static BuildProject loadProject(Path projectPath) throws Exception {
        return new BuildProject(projectPath);
    }

    private BuildProject(Path projectPath) throws Exception {
        super();
        if (!RepoUtils.isBallerinaProject(projectPath)) {
            throw new Exception("invalid Ballerina source path:" + projectPath);
        }
        packagePath = projectPath.toString();
        //TODO: replace with the new toml processor
        ballerinaToml = ManifestProcessor.parseTomlContentAsStream(getTomlContent(projectPath));
        this.context.setSourceRoot(projectPath);
        this.context.setTargetPath(ProjectFiles.createTargetDirectoryStructure(projectPath));
    }

    private InputStream getTomlContent(Path projectPath) {
        Path tomlFilePath = projectPath.resolve("Ballerina.toml");
        if (Files.exists(tomlFilePath)) {
            try {
                return Files.newInputStream(tomlFilePath);
            } catch (IOException ignore) {
            }
        }
        return new ByteArrayInputStream(new byte[0]);
    }

    public Package getPackage() {
        final PackageConfig packageConfig = PackageLoader.loadPackage(packagePath, false);
        this.context.addPackage(packageConfig);
        return this.context.currentPackage();
    }
}
