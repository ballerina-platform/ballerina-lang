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
package org.ballerinalang.langserver.workspace;

import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Contains a set of utility methods to manage projects.
 *
 * @since 2.0.0
 */
public class TestWorkspaceManager {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/project");
    private final String dummyContent = "function foo() {" + CommonUtil.LINE_SEPARATOR + "}";
    private BallerinaWorkspaceManager workspaceManager;

    @BeforeMethod
    void initWorkspaceManager() {
        // Need to get a clean workspace manager before each test method
        workspaceManager = BallerinaWorkspaceManager.getInstance(new LanguageServerContextImpl());
    }

    @Test(dataProvider = "fileOpenUpdateTestDataProvider")
    public void testOpenDocument(Path filePath) throws IOException, WorkspaceDocumentException {
        // Inputs from lang server
        openFile(filePath);

        // Assert content
        Optional<Document> document = workspaceManager.document(filePath);
        Assert.assertNotNull(document.get());
        Assert.assertEquals(document.get().syntaxTree().textDocument().toString(), dummyContent);
    }

    @Test(dataProvider = "fileOpenUpdateTestDataProvider", dependsOnMethods = "testOpenDocument")
    public void testUpdateDocument(Path filePath) throws WorkspaceDocumentException {
        // Inputs from lang server
        DidChangeTextDocumentParams params = new DidChangeTextDocumentParams();
        VersionedTextDocumentIdentifier doc = new VersionedTextDocumentIdentifier(filePath.toUri().toString(), 1);
        params.setTextDocument(doc);
        params.getContentChanges().add(new TextDocumentContentChangeEvent(dummyContent));

        // Notify workspace manager
        workspaceManager.didChange(filePath, params);

        Optional<Document> document = workspaceManager.document(filePath);
        Assert.assertNotNull(document.get());
        Assert.assertEquals(document.get().syntaxTree().textDocument().toString(), dummyContent);
    }

    @Test
    public void testWSEventsCreateBalSource() throws WorkspaceDocumentException, IOException {
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal").toAbsolutePath();

        // Open project
        openFile(filePath);
        Module oldModule = workspaceManager.module(filePath).orElseThrow();

        // Create a new file and send CREATED event
        Path newFile = RESOURCE_DIRECTORY.resolve("myproject").resolve("new-file.bal").toAbsolutePath();
        Files.write(newFile, "".getBytes());
        FileEvent fileEvent = new FileEvent(newFile.toUri().toString(), FileChangeType.Created);
        try {
            workspaceManager.didChangeWatched(newFile, fileEvent);
            // Creating new document changes the Module
            Assert.assertNotSame(oldModule, workspaceManager.module(filePath).orElseThrow());
        } finally {
            Files.deleteIfExists(newFile);
        }
    }

    @Test
    public void testWSEventsDeleteBalSource() throws WorkspaceDocumentException, IOException {
        // Create a new file
        Path newFile = RESOURCE_DIRECTORY.resolve("myproject").resolve("delete-file.bal").toAbsolutePath();
        Files.write(newFile, "".getBytes());

        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal").toAbsolutePath();

        // Open project
        openFile(filePath);
        Module oldModule = workspaceManager.module(filePath).orElseThrow();

        // Delete a file and send DELETED event
        Files.delete(newFile);
        FileEvent fileEvent = new FileEvent(newFile.toUri().toString(), FileChangeType.Deleted);
        workspaceManager.didChangeWatched(newFile, fileEvent);

        // File deletion forces a new module, modules should not be the same
        Assert.assertNotSame(oldModule, workspaceManager.module(filePath).orElseThrow());
    }

    @Test
    public void testWSEventsDeleteBalSourceOnSingleFileProj() throws WorkspaceDocumentException, IOException {
        // Create a new file
        Path singleFile = RESOURCE_DIRECTORY.resolve("single-file").resolve("delete-file.bal").toAbsolutePath();
        Files.write(singleFile, "".getBytes());

        // Open project
        openFile(singleFile);

        // Delete a file and send DELETED event
        Files.delete(singleFile);
        FileEvent fileEvent = new FileEvent(singleFile.toUri().toString(), FileChangeType.Deleted);
        workspaceManager.didChangeWatched(singleFile, fileEvent);

        try {
            // File deletion for a single file-project removes the project from mapping
            Files.write(singleFile, "".getBytes()); // need to create file again to not to fail .root() call

            // .project() call should return empty
            Assert.assertTrue(workspaceManager.project(singleFile).isEmpty());
        } finally {
            Files.deleteIfExists(singleFile);
        }
    }

    @Test
    public void testWSEventsCreateBalTomlOnSingleFileProj() throws WorkspaceDocumentException, IOException {
        Path filePath = RESOURCE_DIRECTORY.resolve("single-file").resolve("main.bal").toAbsolutePath();

        // Open project
        openFile(filePath);

        // Create a Ballerina.toml and send CREATED event
        Path newTomlFile = RESOURCE_DIRECTORY.resolve("single-file").resolve(ProjectConstants.BALLERINA_TOML)
                .toAbsolutePath();
        Files.write(newTomlFile, "".getBytes());
        FileEvent fileEvent = new FileEvent(newTomlFile.toUri().toString(), FileChangeType.Created);
        try {
            workspaceManager.didChangeWatched(newTomlFile, fileEvent);
            Optional<Project> project = workspaceManager.project(filePath);

            // Project should not return empty
            Assert.assertTrue(project.isPresent());
            // Project should be of type BUILD_PROJECT
            Assert.assertSame(project.get().kind(), ProjectKind.BUILD_PROJECT);
        } finally {
            Files.deleteIfExists(newTomlFile);
        }
    }

    @Test
    public void testWSEventsCreateBalTomlOnBuildProj() throws WorkspaceDocumentException, IOException {
        Path filePath = RESOURCE_DIRECTORY.resolve("single-file").resolve("main.bal").toAbsolutePath();

        // Create a Ballerina.toml file
        Path tomlFile = RESOURCE_DIRECTORY.resolve("single-file").resolve(ProjectConstants.BALLERINA_TOML)
                .toAbsolutePath();
        Files.write(tomlFile, "[package]\norg = \"sameera\"\nname = \"myproject\"\nversion = \"0.1.0\"".getBytes());

        // Open project
        openFile(filePath);

        // Delete a file and send DELETED event
        Files.delete(tomlFile);
        FileEvent fileEvent = new FileEvent(tomlFile.toUri().toString(), FileChangeType.Deleted);
        workspaceManager.didChangeWatched(tomlFile, fileEvent);

        // Project should return empty
        Assert.assertTrue(workspaceManager.project(filePath).isEmpty());
    }

    @Test
    public void testWSEventsCreateCloudToml() throws WorkspaceDocumentException, IOException {
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal").toAbsolutePath();

        // Open project
        openFile(filePath);

        // Create a Cloud.toml and send CREATED event
        Path cloudTomlFile = RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.CLOUD_TOML)
                .toAbsolutePath();
        Files.write(cloudTomlFile, "".getBytes());
        FileEvent fileEvent = new FileEvent(cloudTomlFile.toUri().toString(), FileChangeType.Created);
        try {
            workspaceManager.didChangeWatched(cloudTomlFile, fileEvent);

            Optional<Project> project = workspaceManager.project(filePath);
            // Project should not empty
            Assert.assertTrue(project.isPresent());
            // Project should contain Cloud.toml
            Assert.assertTrue(project.get().currentPackage().cloudToml().isPresent());
        } finally {
            Files.deleteIfExists(cloudTomlFile);
        }
    }

    @Test
    public void testWSEventsDeleteCloudToml() throws WorkspaceDocumentException, IOException {
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal").toAbsolutePath();

        // Create a Cloud.toml file
        Path cloudTomlFile = RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.CLOUD_TOML)
                .toAbsolutePath();
        Files.write(cloudTomlFile, "".getBytes());

        // Open project
        openFile(filePath);

        // Delete a file and send DELETED event
        Files.delete(cloudTomlFile);
        FileEvent fileEvent = new FileEvent(cloudTomlFile.toUri().toString(), FileChangeType.Deleted);
        workspaceManager.didChangeWatched(cloudTomlFile, fileEvent);

        Optional<Project> project = workspaceManager.project(filePath);
        // Project should not empty
        Assert.assertTrue(project.isPresent());
        // Project should not contain Cloud.toml
        Assert.assertTrue(project.get().currentPackage().cloudToml().isEmpty());
    }

    @Test
    public void testWSEventsCreateCompilerPluginToml() throws WorkspaceDocumentException, IOException {
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal").toAbsolutePath();

        // Open project
        openFile(filePath);

        // Create a Compiler-plugin.toml and send CREATED event
        Path compilerPluginTomlFile = RESOURCE_DIRECTORY.resolve("myproject")
                .resolve(ProjectConstants.COMPILER_PLUGIN_TOML).toAbsolutePath();
        Files.write(compilerPluginTomlFile, "".getBytes());
        FileEvent fileEvent = new FileEvent(compilerPluginTomlFile.toUri().toString(), FileChangeType.Created);
        try {
            workspaceManager.didChangeWatched(compilerPluginTomlFile, fileEvent);

            Optional<Project> project = workspaceManager.project(filePath);
            // Project should not empty
            Assert.assertTrue(project.isPresent());
            // Project should contain Compiler-plugin.toml
            Assert.assertTrue(project.get().currentPackage().compilerPluginToml().isPresent());
        } finally {
            Files.deleteIfExists(compilerPluginTomlFile);
        }
    }

    @Test
    public void testWSEventsDeleteCompilerPluginToml() throws WorkspaceDocumentException, IOException {
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal").toAbsolutePath();

        // Create a Compiler-plugin.toml file
        Path compilerPluginToml = RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.COMPILER_PLUGIN_TOML)
                .toAbsolutePath();
        Files.write(compilerPluginToml, "".getBytes());

        // Open project
        openFile(filePath);

        // Delete a file and send DELETED event
        Files.delete(compilerPluginToml);
        FileEvent fileEvent = new FileEvent(compilerPluginToml.toUri().toString(), FileChangeType.Deleted);
        workspaceManager.didChangeWatched(compilerPluginToml, fileEvent);

        Optional<Project> project = workspaceManager.project(filePath);
        // Project should not empty
        Assert.assertTrue(project.isPresent());
        // Project should not contain Compiler-plugin.toml
        Assert.assertTrue(project.get().currentPackage().compilerPluginToml().isEmpty());
    }

    @Test
    public void testWSEventsCreateDependenciesToml() throws WorkspaceDocumentException, IOException {
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal").toAbsolutePath();

        // Open project
        openFile(filePath);

        // Create a Dependencies.toml and send CREATED event
        Path depsTomlFile = RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.DEPENDENCIES_TOML)
                .toAbsolutePath();
        Files.write(depsTomlFile, "".getBytes());
        FileEvent fileEvent = new FileEvent(depsTomlFile.toUri().toString(), FileChangeType.Created);
        try {
            workspaceManager.didChangeWatched(depsTomlFile, fileEvent);

            Optional<Project> project = workspaceManager.project(filePath);
            // Project should not empty
            Assert.assertTrue(project.isPresent());
            // Project should contain Dependencies.toml
            Assert.assertTrue(project.get().currentPackage().dependenciesToml().isPresent());
        } finally {
            Files.deleteIfExists(depsTomlFile);
        }
    }

    @Test
    public void testWSEventsDeleteDependenciesToml() throws WorkspaceDocumentException, IOException {
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal").toAbsolutePath();

        // Create a toml file
        Path depsTomlFile = RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.DEPENDENCIES_TOML)
                .toAbsolutePath();
        Files.write(depsTomlFile, "".getBytes());

        // Open project
        openFile(filePath);

        // Delete a file and send DELETED event
        Files.delete(depsTomlFile);
        FileEvent fileEvent = new FileEvent(depsTomlFile.toUri().toString(), FileChangeType.Deleted);
        workspaceManager.didChangeWatched(depsTomlFile, fileEvent);

        Optional<Project> project = workspaceManager.project(filePath);
        // Project should not empty
        Assert.assertTrue(project.isPresent());
        // Project should not contain Dependencies.toml
        Assert.assertTrue(project.get().currentPackage().dependenciesToml().isEmpty());
    }

    @Test
    public void testWSEventsDeleteModule() throws WorkspaceDocumentException, IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject2");
        Path filePath = projectPath.resolve("main.bal").toAbsolutePath();

        // Create a new module with a file
        Path modelsPath = projectPath.resolve(ProjectConstants.MODULES_ROOT).resolve("models").toAbsolutePath();
        Files.createDirectory(modelsPath);
        Path modelFilePath = modelsPath.resolve("model.bal").toAbsolutePath();
        Files.createFile(modelFilePath);

        // Open project
        openFile(filePath);
        Project oldProject = workspaceManager.project(filePath).orElseThrow();

        // Delete a module and send DELETED event
        Files.delete(modelFilePath);
        Files.delete(modelsPath);
        FileEvent fileEvent = new FileEvent(modelsPath.toUri().toString(), FileChangeType.Deleted);
        workspaceManager.didChangeWatched(modelsPath, fileEvent);

        Optional<Project> project = workspaceManager.project(filePath);
        // Project should not be empty
        Assert.assertTrue(project.isPresent());
        // Project should have been reloaded
        Assert.assertNotSame(oldProject, project.get());
    }

    @Test
    public void testWSEventsDeleteModulesDir() throws WorkspaceDocumentException, IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject2");
        Path filePath = projectPath.resolve("main.bal").toAbsolutePath();

        // Open project
        openFile(filePath);
        Project oldProject = workspaceManager.project(filePath).orElseThrow();

        Path modulesPath = projectPath.resolve(ProjectConstants.MODULES_ROOT).toAbsolutePath();
        Path modulesPathNew = projectPath.resolve(ProjectConstants.RESOURCE_DIR_NAME).toAbsolutePath();

        // Delete a file and send DELETED event
        Files.move(modulesPath, modulesPathNew);
        FileEvent fileEvent = new FileEvent(modulesPath.toUri().toString(), FileChangeType.Deleted);
        try {
            workspaceManager.didChangeWatched(modulesPath, fileEvent);
            Optional<Project> project = workspaceManager.project(filePath);
            // Project should not be empty
            Assert.assertTrue(project.isPresent());
            // Project should have been reloaded
            Assert.assertNotSame(oldProject, project.get());
        } finally {
            Files.move(modulesPathNew, modulesPath);
        }
    }

    private void openFile(Path singleFile) throws WorkspaceDocumentException {
        DidOpenTextDocumentParams params = new DidOpenTextDocumentParams();
        TextDocumentItem textDocumentItem = new TextDocumentItem();
        textDocumentItem.setUri(singleFile.toUri().toString());
        textDocumentItem.setText(dummyContent);
        params.setTextDocument(textDocumentItem);
        workspaceManager.didOpen(singleFile, params);
    }

    @DataProvider
    public Object[] fileOpenUpdateTestDataProvider() {
        return new Path[]{
                RESOURCE_DIRECTORY.resolve("single-file").resolve("main.bal").toAbsolutePath(),
                RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal").toAbsolutePath()
        };
    }

    @DataProvider
    public Object[] workspaceEventsTestDataProvider() {
        return new Path[]{
                RESOURCE_DIRECTORY.resolve("single-file").resolve("main.bal").toAbsolutePath(),
                RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal").toAbsolutePath()
        };
    }
}
