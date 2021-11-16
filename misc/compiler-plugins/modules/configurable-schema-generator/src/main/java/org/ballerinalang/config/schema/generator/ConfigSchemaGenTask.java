/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.config.schema.generator;

import io.ballerina.projects.ConfigReader;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.CompilationAnalysisContext;
import org.ballerinalang.config.schema.builder.ConfigSchemaBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Task to generate JSON schema for configurable variables.
 *
 * @since 2.0.0
 */
public class ConfigSchemaGenTask implements AnalysisTask<CompilationAnalysisContext> {

    static final String CONFIG_SCHEMA = "config-schema.json";

    @Override
    public void perform(CompilationAnalysisContext compilationAnalysisContext) {
        String schema = ConfigSchemaBuilder.getConfigSchemaContent(ConfigReader.getConfigVariables(
                compilationAnalysisContext.currentPackage()));
        writeConfigJSONSchema(schema, compilationAnalysisContext.currentPackage().project());
    }

    /**
     * Write config schema JSON to the target/bin path.
     *
     * @param schema JSON schema content as String
     * @param project Project instance
     */
    private void writeConfigJSONSchema(String schema, Project project) {
        Path path;
        if (project.kind().equals(ProjectKind.SINGLE_FILE_PROJECT)) {
            path = Paths.get(System.getProperty("user.dir"));
        } else {
            path = project.sourceRoot();
        }
        if (path != null && !schema.isEmpty()) {
            Path configSchemaFile = path.resolve(CONFIG_SCHEMA);
            createIfNotExists(configSchemaFile);
            writeContent(configSchemaFile, schema);
        }
    }

    /**
     * Create the file in given path if not exists.
     *
     * @param filePath The path to write the config schema JSON
     */
    private static void createIfNotExists(Path filePath) {
        Path parentDir = filePath.getParent();
        if (parentDir != null && !parentDir.toFile().exists()) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException ioException) {
                throw new ProjectException("Failed to create " + parentDir.toString());
            }
        }
        if (!filePath.toFile().exists()) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw new ProjectException("Failed to create " + filePath.toString());
            }
        }
    }

    /**
     * Write given content to the file.
     *
     * @param filePath Path to the file to write content
     * @param content  String content to write
     */
    private static void writeContent(Path filePath, String content) {
        try {
            Files.write(filePath, Collections.singleton(content));
        } catch (IOException e) {
            throw new ProjectException("Failed to write dependencies to the " + filePath.toString() + " file");
        }
    }

}
