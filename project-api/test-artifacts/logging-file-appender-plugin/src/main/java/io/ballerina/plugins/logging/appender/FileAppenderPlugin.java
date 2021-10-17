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
package io.ballerina.plugins.logging.appender;

import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * A sample {@code CompilerPlugin} that writes diagnostics to a file.
 *
 * @since 2.0.0
 */
public class FileAppenderPlugin extends CompilerPlugin {
    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCodeAnalyzer(new FileAppenderAnalyzer());
    }

    /**
     * A sample {@code CodeAnalyzer} that writes diagnostics to a file.
     *
     * @since 2.0.0
     */
    public static class FileAppenderAnalyzer extends CodeAnalyzer {
        @Override
        public void init(CodeAnalysisContext analysisContext) {
            analysisContext.addCompilationAnalysisTask(compilationAnalysisContext -> {
                Path logFilePath;
                if (compilationAnalysisContext.currentPackage().project().kind() == ProjectKind.BALA_PROJECT) {
                    throw new UnsupportedOperationException();
                }
                if (compilationAnalysisContext.currentPackage().project().kind().equals(ProjectKind.BUILD_PROJECT)) {
                    logFilePath = Paths.get("build/logs/diagnostics.log").toAbsolutePath();
                } else {
                    logFilePath = Paths.get("build/logs/single-file/diagnostics.log").toAbsolutePath();
                }
                StringBuilder logs = new StringBuilder();
                compilationAnalysisContext.compilation().diagnosticResult().diagnostics().forEach(
                        diagnostic -> logs.append(diagnostic.toString()).append("\n"));
                try {
                    Files.createDirectories(Optional.of(logFilePath.getParent()).get());
                    Files.writeString(
                            logFilePath, logs, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException e) {
                    throw new ProjectException("unable to write logs of project '"
                            + compilationAnalysisContext.currentPackage().project().sourceRoot()
                            + "' to '" + logFilePath + "'. " + e.getMessage());
                }
            });
        }
    }
}
