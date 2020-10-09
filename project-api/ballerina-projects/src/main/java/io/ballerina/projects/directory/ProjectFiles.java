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

import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.utils.ProjectUtils;
import io.ballerina.toml.Toml;
import io.ballerina.toml.ast.TomlDiagnostic;
import io.ballerina.toml.ast.TomlNodeLocation;
import io.ballerina.toml.ast.TomlTable;
import io.ballerina.toml.ast.TomlTransformer;
import io.ballerina.toml.ast.TomlValidator;
import io.ballerina.toml.syntax.tree.ModulePartNode;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains a set of utility methods that create an in-memory representation of a Ballerina project directory.
 *
 * @since 2.0.0
 */
public class ProjectFiles {
    private static final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.bal");

    private ProjectFiles() {
    }

    protected static PackageData loadPackageData(Path filePath, boolean singleFileProject) {
        // Handle single file project
        if (singleFileProject) {
            DocumentData documentData = loadDocument(filePath);
            ModuleData defaultModule = ModuleData.from(filePath, Collections.singletonList(documentData),
                    Collections.emptyList());
            return PackageData.from(filePath, defaultModule, Collections.emptyList());
        }

        // Handle build project
        if (filePath == null) {
            throw new IllegalArgumentException("packageDir cannot be null");
        }

        // Check whether the directory exists
        Path packageDirPath = filePath.toAbsolutePath();
        if (!packageDirPath.toFile().canRead()
                || !packageDirPath.toFile().canWrite() || !packageDirPath.toFile().canExecute()) {
            throw new RuntimeException("insufficient privileges to path: " + packageDirPath);
        }
        if (!Files.exists(packageDirPath)) {
            // TODO handle the error
            // TODO use a custom runtime error
            throw new RuntimeException("directory does not exists: " + filePath);
        }

        if (!Files.isDirectory(packageDirPath)) {
            throw new RuntimeException("Not a directory: " + filePath);
        }

//        // Check whether this is a project: Ballerina.toml file has to be there
//        Path ballerinaTomlPath = packageDirPath.resolve("Ballerina.toml");
//        if (!Files.exists(ballerinaTomlPath)) {
//            // TODO handle the error
//            // TODO use a custom runtime error
//            throw new RuntimeException("Not a package directory: " + filePath);
//        }

        // Load Ballerina.toml
        // Load default module
        // load other modules
        ModuleData defaultModule = loadModule(packageDirPath);
        List<ModuleData> otherModules = loadOtherModules(packageDirPath);
        return PackageData.from(packageDirPath, defaultModule, otherModules);
    }

    private static List<ModuleData> loadOtherModules(Path packageDirPath) {
        Path modulesDirPath = packageDirPath.resolve("modules");
        if (!Files.isDirectory(modulesDirPath)) {
            return Collections.emptyList();
        }

        try (Stream<Path> pathStream = Files.walk(modulesDirPath, 1)) {
            return pathStream
                    .filter(path -> !path.equals(modulesDirPath))
                    .filter(Files::isDirectory)
                    .map(ProjectFiles::loadModule)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ModuleData loadModule(Path moduleDirPath) {
        // validate moduleName
        if (!ProjectUtils.validateModuleName(moduleDirPath.toFile().getName())) {
            throw new RuntimeException("Invalid module name : '" + moduleDirPath.getFileName() + "' :\n" +
                    "Module name can only contain alphanumerics, underscores and periods " +
                    "and the maximum length is 256 characters");
        }
        List<DocumentData> srcDocs = loadDocuments(moduleDirPath);
        List<DocumentData> testSrcDocs;
        Path testDirPath = moduleDirPath.resolve("tests");
        if (Files.isDirectory(testDirPath)) {
            testSrcDocs = loadDocuments(testDirPath);
        } else {
            testSrcDocs = Collections.emptyList();
        }
        // TODO Read Module.md file. Do we need to? Balo creator may need to package Module.md
        return ModuleData.from(moduleDirPath, srcDocs, testSrcDocs);
    }

    public static List<DocumentData> loadDocuments(Path dirPath) {
        try (Stream<Path> pathStream = Files.walk(dirPath, 1)) {
            return pathStream
                    .filter(matcher::matches)
                    .map(ProjectFiles::loadDocument)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static DocumentData loadDocument(Path documentFilePath) {
        String content;
        if (documentFilePath == null) {
            throw new RuntimeException("document path cannot be null");
        }
        try {
            content = Files.readString(documentFilePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return DocumentData.from(Optional.of(documentFilePath.getFileName()).get().toString(), content);
    }

    public static SyntaxTree parseToml(Path tomlFilePath) {
        try {
            String tomlStr = Files.readString(tomlFilePath, StandardCharsets.UTF_8);
            TextDocument textDocument = TextDocuments.from(tomlStr);
            return SyntaxTree.from(textDocument, tomlFilePath.toString());
        } catch (IOException e) {
            // TODO Error handling
            throw new RuntimeException("Failed to read the Ballerina.toml", e);
        }
    }

    private static Toml getTomlModel(SyntaxTree tomlSyntaxTree) {
        List<TomlDiagnostic> diagnostics = new ArrayList<>();
        diagnostics.addAll(reportSyntaxDiagnostics(tomlSyntaxTree));
        TomlTransformer nodeTransformer = new TomlTransformer();
        TomlTable transformedTable = (TomlTable) nodeTransformer.transform((ModulePartNode) tomlSyntaxTree.rootNode());
        TomlValidator tomlValidator = new TomlValidator();
        tomlValidator.visit(transformedTable);
        transformedTable.setSyntacticalDiagnostics(diagnostics);
        diagnostics.addAll(transformedTable.collectSemanticDiagnostics());
        return new Toml(transformedTable);
    }

    private static List<TomlDiagnostic> reportSyntaxDiagnostics(SyntaxTree tree) {
        List<TomlDiagnostic> diagnostics = new ArrayList<>();
        for (Diagnostic syntaxDiagnostic : tree.diagnostics()) {
            TomlNodeLocation tomlNodeLocation = new TomlNodeLocation(syntaxDiagnostic.location().lineRange(),
                    syntaxDiagnostic.location().textRange());
            TomlDiagnostic tomlDiagnostic =
                    new TomlDiagnostic(tomlNodeLocation, syntaxDiagnostic.diagnosticInfo(), syntaxDiagnostic.message());
            diagnostics.add(tomlDiagnostic);
        }
        return diagnostics;
    }

    public static PackageDescriptor createPackageDescriptor(SyntaxTree tomlSyntaxTree) {
        Toml tomlModel = getTomlModel(tomlSyntaxTree);
        if (!tomlModel.getDiagnostics().isEmpty()) {
            // TODO proper error handling
            throw new RuntimeException("Ballerina.toml file has toml syntax errors");
        }

        Toml pkgTable = tomlModel.getTable("package");
        PackageName packageName = PackageName.from(pkgTable.getString("name"));
        PackageOrg packageOrg = PackageOrg.from(pkgTable.getString("org"));
        PackageVersion packageVersion = PackageVersion.from(pkgTable.getString("version"));
        // TODO Populate dependencies
        return new PackageDescriptor(packageName, packageOrg, packageVersion, Collections.emptyList());
    }
}
