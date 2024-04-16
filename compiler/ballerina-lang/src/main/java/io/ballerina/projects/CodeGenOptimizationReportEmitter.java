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

package io.ballerina.projects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.TARGET_DIR_NAME;

/**
 * Emits time durations and optimized node details for codegen optimization.
 *
 * @since 2201.9.0
 */
public class CodeGenOptimizationReportEmitter {

    private static final Map<PackageID, Long> birOptimizationDurations = new LinkedHashMap<>();
    private static long nativeOptimizationDuration = 0;
    private static final PrintStream out = System.out;
    private static final String CODEGEN_OPTIMIZATION_REPORT = "codegen_optimization_report.json";

    protected static void flipBirOptimizationTimer(PackageID pkgId) {
        if (birOptimizationDurations.containsKey(pkgId)) {
            long totalDuration = System.currentTimeMillis() - birOptimizationDurations.get(pkgId);
            birOptimizationDurations.put(pkgId, totalDuration);
        } else {
            birOptimizationDurations.put(pkgId, System.currentTimeMillis());
        }
    }

    protected static void flipNativeOptimizationTimer() {
        if (nativeOptimizationDuration == 0) {
            nativeOptimizationDuration = System.currentTimeMillis();
        } else {
            nativeOptimizationDuration = System.currentTimeMillis() - nativeOptimizationDuration;
        }
    }

    protected static void emitBirOptimizationDuration() {
        long totalDuration = birOptimizationDurations.values().stream().mapToLong(Long::longValue).sum();

        out.printf("Duration for unused BIR node analysis : %dms%n", totalDuration);
        birOptimizationDurations.forEach((key, value) -> out.printf("    %s : %dms%n", key, value));
        out.println();
    }

    protected static void emitNativeOptimizationDuration() {
        out.println(
                "Duration for Bytecode Optimization (analysis + deletion) : " + nativeOptimizationDuration + "ms");
        nativeOptimizationDuration = 0;
    }

    protected static void emitOptimizedExecutableSize(Path executableFilePath) {
        try {
            float optimizedJarSize =
                    Files.size(Path.of(executableFilePath.toString().replace(".jar", "_OPTIMIZED.jar"))) /
                            (1024f * 1024f);
            System.out.printf("Optimized file size : %f MB%n", optimizedJarSize);
        } catch (IOException e) {
            throw new ProjectException("Failed to emit optimized executable size ", e);
        }
    }

    protected static void emitCodegenOptimizationReport(
            Map<PackageID, UsedBIRNodeAnalyzer.InvocationData> invocationDataMap, Path projectDirectoryPath,
            ProjectKind projectKind) {

        Path reportParentDirectoryPath = projectDirectoryPath.resolve(TARGET_DIR_NAME).toAbsolutePath().normalize();

        if (projectKind == ProjectKind.SINGLE_FILE_PROJECT) {
            projectDirectoryPath = projectDirectoryPath.toAbsolutePath().getParent();
            reportParentDirectoryPath = projectDirectoryPath;
        }

        if (!Files.exists(reportParentDirectoryPath)) {
            try {
                Files.createDirectories(reportParentDirectoryPath);
            } catch (IOException e) {
                throw new ProjectException("Failed to create Optimization Report directory ", e);
            }
        }

        Map<String, CodegenOptimizationReport> reports = new LinkedHashMap<>();
        invocationDataMap.forEach((key, value) -> {
            reports.put(key.toString(), getCodegenOptimizationReport(value));
        });

        Path jsonFilePath = reportParentDirectoryPath.resolve(CODEGEN_OPTIMIZATION_REPORT);
        File jsonFile = new File(jsonFilePath.toString());

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            String json = gson.toJson(reports);
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ProjectException("couldn't write data to optimization report file : ", e);
        }
    }

    private static CodegenOptimizationReport getCodegenOptimizationReport(
            UsedBIRNodeAnalyzer.InvocationData invocationData) {
        return new CodegenOptimizationReport(getFunctionNames(invocationData.usedFunctions),
                getFunctionNames(invocationData.unusedFunctions), getTypeDefNames(invocationData.usedTypeDefs),
                getTypeDefNames(invocationData.unusedTypeDefs), invocationData.usedNativeClassPaths);
    }

    private static CodegenOptimizationReport.FunctionNames getFunctionNames(HashSet<BIRNode.BIRFunction> birFunctions) {
        CodegenOptimizationReport.FunctionNames functionNames =
                new CodegenOptimizationReport.FunctionNames(new HashSet<>(), new HashSet<>());
        birFunctions.forEach(birFunction -> {
            if (birFunction.origin == SymbolOrigin.COMPILED_SOURCE) {
                functionNames.sourceFunctions().add(getFunctionName(birFunction));
            } else {
                functionNames.virtualFunctions().add(getFunctionName(birFunction));
            }
        });
        return functionNames;
    }

    private static String getFunctionName(BIRNode.BIRFunction birFunction) {
        if (birFunction.receiver == null) {
            return birFunction.name.value;
        }

        // If the function is an attached function, we have to emit the parent as well.
        return birFunction.receiver.type.toString() + "." + birFunction.name.value;
    }

    private static CodegenOptimizationReport.TypeDefinitions getTypeDefNames(
            HashSet<BIRNode.BIRTypeDefinition> birTypeDefs) {
        CodegenOptimizationReport.TypeDefinitions typeDefNames =
                new CodegenOptimizationReport.TypeDefinitions(new HashSet<>(), new HashSet<>());
        birTypeDefs.forEach(birTypeDef -> {
            if (birTypeDef.origin == SymbolOrigin.COMPILED_SOURCE) {
                typeDefNames.sourceTypeDefinitions().add(birTypeDef.internalName.value);
            } else {
                typeDefNames.virtualTypeDefinitions().add(birTypeDef.internalName.value);
            }
        });
        return typeDefNames;
    }

}
