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

import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.util.ProjectConstants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Contains a set of utility methods to create a project.
 *
 * @since 2.0.0
 */
public class ProjectLoader {

    public static Project loadProject(Path path) {
        return loadProject(path, ProjectEnvironmentBuilder.getDefaultBuilder());
    }

    /**
     * Returns a project by deriving the type from the path provided.
     *
     * @param path ballerina project or standalone file path
     * @return project of applicable type
     */
    public static Project loadProject(Path path, ProjectEnvironmentBuilder projectEnvironmentBuilder) {
        Path absProjectPath = Optional.of(path.toAbsolutePath()).get();
        Path projectRoot;
        if (absProjectPath.toFile().isDirectory()) {
            if (ProjectConstants.MODULES_ROOT.equals(
                    Optional.of(absProjectPath.getParent()).get().toFile().getName())) {
                projectRoot = Optional.of(Optional.of(absProjectPath.getParent()).get().getParent()).get();
            } else {
                projectRoot = absProjectPath;
            }
            return BuildProject.load(projectEnvironmentBuilder, projectRoot);
        }
        // check if the file is a source file in the default module
        projectRoot = Optional.of(absProjectPath.getParent()).get();
        if (hasBallerinaToml(projectRoot)) {
            return BuildProject.load(projectEnvironmentBuilder, projectRoot);
        }

        // check if the file is a test file in the default module
        Path testsRoot = Optional.of(absProjectPath.getParent()).get();
        projectRoot = Optional.of(testsRoot.getParent()).get();
        if (ProjectConstants.TEST_DIR_NAME.equals(testsRoot.toFile().getName()) && hasBallerinaToml(projectRoot)) {
            return BuildProject.load(projectEnvironmentBuilder, projectRoot);
        }

        // check if the file is a source file in a non-default module
        Path modulesRoot = Optional.of(Optional.of(absProjectPath.getParent()).get().getParent()).get();
        projectRoot = modulesRoot.getParent();
        if (ProjectConstants.MODULES_ROOT.equals(modulesRoot.toFile().getName()) && hasBallerinaToml(projectRoot)) {
            return BuildProject.load(projectEnvironmentBuilder, projectRoot);
        }

        // check if the file is a test file in a non-default module
        modulesRoot = Optional.of(Optional.of(testsRoot.getParent()).get().getParent()).get();
        projectRoot = modulesRoot.getParent();

        if (ProjectConstants.MODULES_ROOT.equals(modulesRoot.toFile().getName()) && hasBallerinaToml(projectRoot)) {
            return BuildProject.load(projectEnvironmentBuilder, projectRoot);
        }

        return SingleFileProject.load(projectEnvironmentBuilder, absProjectPath);
    }

    public static Optional<Path> findProjectRoot(Path filepath) throws ProjectException, IllegalArgumentException {
        if (!Files.isRegularFile(filepath) || !filepath.toString().endsWith(ProjectConstants.BLANG_SOURCE_EXT)) {
            throw new IllegalArgumentException("provided path is not a valid ballerina file");
        }
        Path absProjectPath = filepath.toAbsolutePath();

        // check if the file is a source file in the default module
        if (hasBallerinaToml(Optional.of(absProjectPath.getParent()).get())) {
            return Optional.of(absProjectPath.getParent());
        }

        Path projectRoot;
        // check if the file is a test file in the default module
        Path testsRoot = Optional.of(absProjectPath.getParent()).get();
        projectRoot = Optional.of(testsRoot.getParent()).get();
        if (ProjectConstants.TEST_DIR_NAME.equals(testsRoot.toFile().getName()) && hasBallerinaToml(projectRoot)) {
            return Optional.of(projectRoot);
        }

        // check if the file is a source file in a non-default module
        Path modulesRoot = Optional.of(Optional.of(absProjectPath.getParent()).get().getParent()).get();
        projectRoot = modulesRoot.getParent();
        if (ProjectConstants.MODULES_ROOT.equals(modulesRoot.toFile().getName()) && hasBallerinaToml(projectRoot)) {
            return Optional.of(projectRoot);
        }

        // check if the file is a test file in a non-default module
        modulesRoot = Optional.of(Optional.of(testsRoot.getParent()).get().getParent()).get();
        projectRoot = modulesRoot.getParent();

        if (ProjectConstants.MODULES_ROOT.equals(modulesRoot.toFile().getName()) && hasBallerinaToml(projectRoot)) {
            return Optional.of(projectRoot);
        }

        return Optional.empty();
    }

    /**
     * Returns the documentId of the provided file path.
     *
     * @param documentFilePath file path of the document
     * @param project project that the file belongs to
     * @return documentId of the document
     */

    public static Document getDocument(Path documentFilePath, Project project) {
        if (project.kind().equals(ProjectKind.SINGLE_FILE_PROJECT)) {
            DocumentId documentId = project.currentPackage().getDefaultModule().documentIds().iterator().next();
            return project.currentPackage().getDefaultModule().document(documentId);
        }
        if (!(project instanceof BuildProject)) {
            throw new ProjectException("unsupported project type:" + project.kind());
        }
        BuildProject buildProject = (BuildProject) project;
        Path parent = Optional.of(documentFilePath.getParent()).get();

        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Optional<Path> modulePath = buildProject.modulePath(moduleId);
            if (modulePath.isEmpty()) {
                throw new ProjectException("document path does to belong to any module");
            }
            if (parent.equals(modulePath.get())
                    || parent.equals(modulePath.get().resolve(ProjectConstants.TEST_DIR_NAME))) {
                Module module = project.currentPackage().module(moduleId);
                for (DocumentId documentId : module.documentIds()) {
                    if (module.document(documentId).name().equals(
                            Optional.of(documentFilePath.getFileName()).get().toString())) {
                        return module.document(documentId);
                    }
                }

                for (DocumentId documentId : module.testDocumentIds()) {
                    if (module.document(documentId).name().equals(ProjectConstants.TEST_DIR_NAME + "/"
                            + Optional.of(documentFilePath.getFileName()).get().toString())) {
                        return module.document(documentId);
                    }
                }
            }
        }
        throw new ProjectException("document path does to belong to the project");
    }

    private static boolean hasBallerinaToml(Path filePath) {
        return filePath.resolve(ProjectConstants.BALLERINA_TOML).toFile().exists();
    }
}
