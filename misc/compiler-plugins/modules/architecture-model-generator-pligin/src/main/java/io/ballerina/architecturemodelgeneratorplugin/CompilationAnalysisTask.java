/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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

package io.ballerina.architecturemodelgeneratorplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ballerina.architecturemodelgenerator.ComponentModel;
import io.ballerina.architecturemodelgenerator.ComponentModelBuilder;
import io.ballerina.architecturemodelgeneratorplugin.diagnostic.DiagnosticMessage;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Project;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.CompilationAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import static io.ballerina.architecturemodelgeneratorplugin.PluginConstants.MODEL_DIR_NAME;
import static io.ballerina.architecturemodelgeneratorplugin.PluginConstants.MODEL_JSON_NAME;

/**
 * Compilation analyzer to generate component model.
 *
 * @since 2201.4.0
 */
public class CompilationAnalysisTask implements AnalysisTask<CompilationAnalysisContext> {
    @Override
    public void perform(CompilationAnalysisContext compilationAnalysisContext) {
        Project project = compilationAnalysisContext.currentPackage().project();

        //Used build option exportComponentModel() to enable plugin at the build time.
        BuildOptions buildOptions = project.buildOptions();
        if (buildOptions.exportComponentModel()) {
            Path outPath = project.targetDir();
            ComponentModelBuilder componentModelBuilder = new ComponentModelBuilder();
            ComponentModel projectModel = componentModelBuilder
                    .constructComponentModel(compilationAnalysisContext.currentPackage(),
                            compilationAnalysisContext.compilation());
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
            String componentModelJson = gson.toJson(projectModel) + System.lineSeparator();
            writeComponentModelJson(outPath, componentModelJson, compilationAnalysisContext);
        }
    }

    private void writeComponentModelJson(Path outPath, String componentModelJson, CompilationAnalysisContext context) {
        try {
            // Create ComponentModel directory if not exists in the path. If exists do not throw an error
            Path componentModelExportDir = outPath.resolve(MODEL_DIR_NAME);
            Files.createDirectories(componentModelExportDir);
            Path writePath = componentModelExportDir.resolve(MODEL_JSON_NAME);
            writeFile(writePath, componentModelJson);
        } catch (InvalidPathException | SecurityException | IOException e) {
            DiagnosticMessage diagnosticMessage = DiagnosticMessage.ERROR_100;
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(diagnosticMessage.getCode(),
                    diagnosticMessage.getMessageFormat(), diagnosticMessage.getSeverity());
            Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo, null, e.getMessage());
            context.reportDiagnostic(diagnostic);
        }
    }

    private void writeFile(Path filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }
}
