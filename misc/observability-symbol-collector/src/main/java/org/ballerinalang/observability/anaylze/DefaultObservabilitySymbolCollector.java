/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.observability.anaylze;

import com.google.gson.JsonElement;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.diagramutil.DiagramUtil;
import org.ballerinalang.observability.anaylze.model.PackageHolder;
import org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static io.ballerina.projects.util.ProjectConstants.CACHES_DIR_NAME;

/**
 * Default implementation of {@link ObservabilitySymbolCollector}.
 *
 * @since 2.0.0
 */
public class DefaultObservabilitySymbolCollector implements ObservabilitySymbolCollector {

    private static final String SYNTAX_TREE_DIR = "syntax-tree";
    private static final String SYNTAX_TREE_FILE_NAME = "syntax-tree.json";
    private static final String SYNTAX_TREE_META_FILENAME = "meta.properties";

    // JSON Keys
    private static final String BALLERINA_VERSION_KEY = "ballerinaVersion";
    private static final String PACKAGE_ORG_KEY = "packageOrg";
    private static final String PACKAGE_NAME_KEY = "packageName";
    private static final String PACKAGE_VERSION_KEY = "packageVersion";
    private static final String PACKAGE_MODULES_KEY = "modules";
    private static final String MODULE_NAME_KEY = "moduleName";
    private static final String MODULE_DOCUMENTS_KEY = "documents";
    private static final String DOCUMENT_NAME_KEY = "documentName";
    private static final String DOCUMENT_SYNTAX_TREE_KEY = "syntaxTree";

    // Metadata Keys
    private static final String PROGRAM_HASH_KEY = "PROGRAM_HASH";

    private static final PrintStream out = System.out;

    private boolean isObservabilityIncluded = false;
    private final PackageHolder packageHolder = new PackageHolder();

    @Override
    public void process(Project project) {
        isObservabilityIncluded = project.buildOptions().observabilityIncluded();
        if (!isObservabilityIncluded) {
            return;
        }

        // Skip this part if project has not updated
        if (ProjectUtils.isProjectUpdated(project)) {
            packageHolder.setSyntaxTreeDataString(generateSyntaxTreeJsonString(project));
        }
    }

    @Override
    public void writeToExecutable(Path executableFile, Project project) throws IOException {
        if (!isObservabilityIncluded) {
            return;
        }
        try (FileSystem fs = FileSystems.newFileSystem(executableFile,
                DefaultObservabilitySymbolCollector.class.getClassLoader())) {
            Path syntaxTreeDirPath = fs.getPath(SYNTAX_TREE_DIR);
            Files.createDirectories(syntaxTreeDirPath);

            // Writing Syntax Tree Json
            String syntaxTreeDataString;
            if (ProjectUtils.isProjectUpdated(project)) {
                syntaxTreeDataString = packageHolder.getSyntaxTreeDataString();
            } else {
                // When project is not updated read Syntax Tree Json from Observability Symbols Jar
                Path observeJarCachePath = project.targetDir()
                        .resolve(CACHES_DIR_NAME)
                        .resolve(project.currentPackage().packageOrg().value())
                        .resolve(project.currentPackage().packageName().value())
                        .resolve(project.currentPackage().packageVersion().value().toString())
                        .resolve("observe")
                        .resolve(project.currentPackage().packageOrg().value() + "-"
                                + project.currentPackage().packageName().value()
                                + "-observability-symbols.jar");
                JarFile observeSymbolsJar = new JarFile(String.valueOf(observeJarCachePath));
                ZipEntry syntaxTreeJsonEntry = observeSymbolsJar.getEntry("syntax-tree/syntax-tree.json");
                InputStream syntaxTreeJsonStream = observeSymbolsJar.getInputStream(syntaxTreeJsonEntry);
                syntaxTreeDataString = new String(syntaxTreeJsonStream.readAllBytes(), StandardCharsets.UTF_8);
            }
            Files.write(syntaxTreeDirPath.resolve(SYNTAX_TREE_FILE_NAME),
                    syntaxTreeDataString.getBytes(StandardCharsets.UTF_8));

            // Writing Syntax Tree Metadata
            Properties props = new Properties();
            try (OutputStream outputStream =
                         Files.newOutputStream(syntaxTreeDirPath.resolve(SYNTAX_TREE_META_FILENAME))) {
                props.setProperty(PROGRAM_HASH_KEY, getSyntaxTreeHash(syntaxTreeDataString));
                props.store(outputStream, null);
            } catch (NoSuchAlgorithmException e) {
                out.println("error: failed to store Enriched Syntax Tree Json into the Jar due to " + e.getMessage());
            }
        }
    }

    private String getSyntaxTreeHash(String syntaxTreeDataString) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(syntaxTreeDataString.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedHash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String generateSyntaxTreeJsonString(Project project) {
        Package currentPackage = project.currentPackage();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        packageCompilation.diagnosticResult();      // Trigger Compilation

        packageHolder.setOrg(currentPackage.packageOrg().toString());
        packageHolder.setName(currentPackage.packageName().toString());
        packageHolder.setVersion(currentPackage.packageVersion().toString());

        final String ballerinaVersion = RepoUtils.getBallerinaVersion();
        StringBuilder jsonStringBuilder = new StringBuilder().append("{\"")
                .append(BALLERINA_VERSION_KEY).append("\":\"").append(ballerinaVersion).append("\",\"")
                .append(PACKAGE_ORG_KEY).append("\":\"").append(packageHolder.getOrg()).append("\",\"")
                .append(PACKAGE_NAME_KEY).append("\":\"").append(packageHolder.getName()).append("\",\"")
                .append(PACKAGE_VERSION_KEY).append("\":\"").append(packageHolder.getVersion()).append("\",\"")
                .append(PACKAGE_MODULES_KEY).append("\":{");


        ModuleId[] moduleIds = currentPackage.moduleIds().toArray(new ModuleId[0]);
        Arrays.sort(moduleIds, (moduleId1, moduleId2) -> {
            String moduleName1 = moduleId1.moduleName();
            String moduleName2 = moduleId2.moduleName();
            return moduleName1.compareTo(moduleName2);
        });

        for (ModuleId moduleId : moduleIds) {
            SemanticModel semanticModel = packageCompilation.getSemanticModel(moduleId);
            Module module = currentPackage.module(moduleId);
            String moduleName = module.descriptor().name().toString();

            jsonStringBuilder.append("\"").append(moduleName).append("\":{\"")
                    .append(MODULE_NAME_KEY).append("\":\"").append(moduleName).append("\",\"")
                    .append(MODULE_DOCUMENTS_KEY).append("\":{");

            DocumentId[] documentIds = module.documentIds().toArray(new DocumentId[0]);
            Arrays.sort(documentIds, (documentId1, documentId2) -> {
                String documentName1 = module.document(documentId1).name();
                String documentName2 = module.document(documentId2).name();
                return documentName1.compareTo(documentName2);
            });

            for (DocumentId documentId : documentIds) {
                Document document = module.document(documentId);
                JsonElement syntaxTreeJSON = DiagramUtil.getSyntaxTreeJSON(document, semanticModel);

                jsonStringBuilder.append("\"").append(document.name()).append("\":{\"")
                        .append(DOCUMENT_NAME_KEY).append("\":\"")
                        .append(document.name()).append("\",\"")
                        .append(DOCUMENT_SYNTAX_TREE_KEY).append("\":")
                        .append(syntaxTreeJSON.toString())
                        .append("}")
                        .append(",");
            }
            jsonStringBuilder.deleteCharAt(jsonStringBuilder.length() - 1);
            jsonStringBuilder.append("}}")
                    .append(",");
        }
        jsonStringBuilder.deleteCharAt(jsonStringBuilder.length() - 1);

        return jsonStringBuilder.append("}}").toString();
    }
}
