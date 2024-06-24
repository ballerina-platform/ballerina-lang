/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler.bir.codegen.bytecodeoptimizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ballerina.projects.ProjectException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Emits optimized class file details for native dependency optimization.
 *
 * @since 2201.10.0
 */
public final class NativeDependencyOptimizationReportEmitter {

    private NativeDependencyOptimizationReportEmitter() {
    }

    private static final String NATIVE_DEPENDENCY_OPTIMIZATION_REPORT = "native_dependency_optimization_report.json";

    public static void emitCodegenOptimizationReport(NativeDependencyOptimizationReport report,
                                                     Path reportParentDirectoryPath) {
        if (!Files.exists(reportParentDirectoryPath)) {
            try {
                Files.createDirectories(reportParentDirectoryPath);
            } catch (IOException e) {
                throw new ProjectException("Failed to create optimization report directory ", e);
            }
        }

        Path jsonFilePath = reportParentDirectoryPath.resolve(NATIVE_DEPENDENCY_OPTIMIZATION_REPORT);
        File jsonFile = new File(jsonFilePath.toString());

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            String json = gson.toJson(report);
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ProjectException("couldn't write data to optimization report file : ", e);
        }
    }
}

