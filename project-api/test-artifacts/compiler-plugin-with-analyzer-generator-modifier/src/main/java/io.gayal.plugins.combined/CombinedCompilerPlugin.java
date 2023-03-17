/*
 * Copyright (c) 2023, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gayal.plugins.combined;

import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CodeGenerator;
import io.ballerina.projects.plugins.CodeGeneratorContext;
import io.ballerina.projects.plugins.CodeModifier;
import io.ballerina.projects.plugins.CodeModifierContext;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

enum CodeType {
    ANALYZER,
    GENERATOR,
    MODIFIER;

    public String getName() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}

/***
 * A compiler plugin which contains all the three types of compiler plugins.
 */
public class CombinedCompilerPlugin extends CompilerPlugin {

    String lockFilePath = "./src/test/resources/compiler_plugin_tests/" +
            "package_comp_plugin_with_analyzer_generator_modifier/target/combined_plugin_output.lock";
    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCodeAnalyzer(new CodeAnalyzer() {
            @Override
            public void init(CodeAnalysisContext codeAnalysisContext) {
                appendToOutputFile(CodeType.ANALYZER);
            }
        });
        pluginContext.addCodeGenerator(new CodeGenerator() {
            @Override
            public void init(CodeGeneratorContext codeGeneratorContext) {
                appendToOutputFile(CodeType.GENERATOR);
            }
        });
        pluginContext.addCodeModifier(new CodeModifier() {
            @Override
            public void init(CodeModifierContext codeModifierContext) {
                appendToOutputFile(CodeType.MODIFIER);
            }
        });
    }

    private void appendToOutputFile(CodeType codeType) {
        String filePath = "./src/test/resources/compiler_plugin_tests/" +
                "package_comp_plugin_with_analyzer_generator_modifier/target/combined_plugin_output.txt";
        File outputFile = new File(filePath);

        acquireLock();
        try (FileOutputStream fileStream = new FileOutputStream(outputFile, true);
             Writer writer = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8)) {
            writer.write("Initialized " + codeType.getName() + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        releaseLock();
    }

    private void acquireLock() {
        try {
            File lockFile = new File(lockFilePath);
            while (lockFile.exists()) {
                Thread.sleep(100);
            }
            if (!lockFile.createNewFile()) {
                throw new RuntimeException("Error while creating the lock file: " + lockFilePath);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(
                    "Error while creating the lock file: " + lockFilePath + " " + e.getMessage());
        }
    }

    private void releaseLock() {
        try {
            Files.delete(Paths.get(lockFilePath));
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error while deleting the lock file: " + lockFilePath + " " + e.getMessage());
        }
    }
}
