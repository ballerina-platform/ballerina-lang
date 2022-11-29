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

package io.ballerina.modelgenerator.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ballerina.modelgenerator.core.ComponentModel;
import io.ballerina.modelgenerator.core.ComponentModelBuilder;
import io.ballerina.modelgenerator.plugin.diagnostic.DiagnosticMessage;
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
import java.nio.file.Paths;

import static io.ballerina.modelgenerator.plugin.PluginConstants.COMPONENT_MODEL;
import static io.ballerina.modelgenerator.plugin.PluginConstants.PATH_SEPARATOR;

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
            String componentModelJson = gson.toJson(projectModel) + "\n";
            writeComponentModelJson(outPath, componentModelJson, compilationAnalysisContext);
        }
    }

    private void writeComponentModelJson(Path outPath, String componentModelJson, CompilationAnalysisContext context) {
        try {
            // Create ComponentModel directory if not exists in the path. If exists do not throw an error
            Files.createDirectories(Paths.get(outPath + PATH_SEPARATOR + COMPONENT_MODEL));
            Path writePath = outPath.resolve(COMPONENT_MODEL + PATH_SEPARATOR + "model.json");
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
