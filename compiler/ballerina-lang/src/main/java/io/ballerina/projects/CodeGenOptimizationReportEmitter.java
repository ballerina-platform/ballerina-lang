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
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Emits time durations and optimized node details for codegen optimization.
 *
 * @since 2201.10.0
 */
public class CodeGenOptimizationReportEmitter {

    private static final CompilerContext.Key<CodeGenOptimizationReportEmitter> CODEGEN_OPTIMIZATION_REPORT_EMITTER_KEY =
            new CompilerContext.Key<>();
    private final Map<PackageID, Long> birOptimizationDurations;
    private long nativeOptimizationDuration;
    private final PrintStream out;
    private static final String CODEGEN_OPTIMIZATION_REPORT = "codegen_optimization_report.json";

    private CodeGenOptimizationReportEmitter(CompilerContext compilerContext) {
        compilerContext.put(CODEGEN_OPTIMIZATION_REPORT_EMITTER_KEY, this);
        this.out = System.out;
        this.birOptimizationDurations = new HashMap<>();
        this.nativeOptimizationDuration = 0;
    }

    public static CodeGenOptimizationReportEmitter getInstance(CompilerContext compilerContext) {
        CodeGenOptimizationReportEmitter codegenOptimizationReportEmitter =
                compilerContext.get(CODEGEN_OPTIMIZATION_REPORT_EMITTER_KEY);
        if (codegenOptimizationReportEmitter == null) {
            codegenOptimizationReportEmitter = new CodeGenOptimizationReportEmitter(compilerContext);
        }
        return codegenOptimizationReportEmitter;
    }

    protected void flipBirOptimizationTimer(PackageID pkgId) {
        if (this.birOptimizationDurations.containsKey(pkgId)) {
            long totalDuration = System.currentTimeMillis() - this.birOptimizationDurations.get(pkgId);
            this.birOptimizationDurations.put(pkgId, totalDuration);
        } else {
            this.birOptimizationDurations.put(pkgId, System.currentTimeMillis());
        }
    }

    protected void flipNativeOptimizationTimer() {
        if (this.nativeOptimizationDuration == 0) {
            this.nativeOptimizationDuration = System.currentTimeMillis();
        } else {
            this.nativeOptimizationDuration = System.currentTimeMillis() - this.nativeOptimizationDuration;
        }
    }

    protected void emitBirOptimizationDuration() {
        long totalDuration = this.birOptimizationDurations.values().stream().mapToLong(Long::longValue).sum();
        this.out.printf("Duration for unused BIR node analysis : %dms%n", totalDuration);
        this.birOptimizationDurations.forEach((key, value) -> this.out.printf("    %s : %dms%n", key, value));
        this.out.println();
    }

    protected void emitNativeOptimizationDuration() {
        this.out.println(
                "Duration for Bytecode optimization (analysis + deletion) : " + this.nativeOptimizationDuration + "ms");
        this.nativeOptimizationDuration = 0;
    }

    protected void emitOptimizedExecutableSize(Path executableFilePath) {
        try {
            float optimizedJarSize = Files.size(executableFilePath) / (1024f * 1024f);
            System.out.printf("Optimized file size : %f MB%n", optimizedJarSize);
        } catch (IOException e) {
            throw new ProjectException("Failed to emit optimized executable size ", e);
        }
    }

    protected void emitCodegenOptimizationReport(Map<PackageID, UsedBIRNodeAnalyzer.InvocationData> invocationDataMap,
                                                 Path targetDirectoryPath) {
        if (!Files.exists(targetDirectoryPath)) {
            try {
                Files.createDirectories(targetDirectoryPath);
            } catch (IOException e) {
                throw new ProjectException("Failed to create optimization report directory ", e);
            }
        }

        Map<String, CodegenOptimizationReport> reports = new LinkedHashMap<>();
        invocationDataMap.forEach((key, value) -> reports.put(key.toString(), getCodegenOptimizationReport(value)));
        Path jsonFilePath = targetDirectoryPath.resolve(CODEGEN_OPTIMIZATION_REPORT);
        File jsonFile = new File(jsonFilePath.toString());

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            String json = gson.toJson(reports);
            writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ProjectException("Couldn't write data to optimization report file : ", e);
        }
    }

    private CodegenOptimizationReport getCodegenOptimizationReport(
            UsedBIRNodeAnalyzer.InvocationData invocationData) {
        return new CodegenOptimizationReport(getFunctionNames(invocationData.usedFunctions),
                getFunctionNames(invocationData.unusedFunctions), getTypeDefNames(invocationData.usedTypeDefs),
                getTypeDefNames(invocationData.unusedTypeDefs), invocationData.usedNativeClassPaths);
    }

    private CodegenOptimizationReport.FunctionNames getFunctionNames(Set<BIRNode.BIRFunction> birFunctions) {
        CodegenOptimizationReport.FunctionNames functionNames =
                new CodegenOptimizationReport.FunctionNames(new LinkedHashSet<>(), new LinkedHashSet<>());
        birFunctions.forEach(birFunction -> {
            if (birFunction.origin == SymbolOrigin.COMPILED_SOURCE) {
                functionNames.sourceFunctions().add(getFunctionName(birFunction));
            } else {
                functionNames.virtualFunctions().add(getFunctionName(birFunction));
            }
        });
        return functionNames;
    }

    private String getFunctionName(BIRNode.BIRFunction birFunction) {
        if (birFunction.receiver == null) {
            return birFunction.name.value;
        }

        // If the function is an attached function, we have to emit the parent as well.
        return birFunction.receiver.type.toString() + "." + birFunction.name.value;
    }

    private CodegenOptimizationReport.TypeDefinitions getTypeDefNames(Set<BIRNode.BIRTypeDefinition> birTypeDefs) {
        CodegenOptimizationReport.TypeDefinitions typeDefNames =
                new CodegenOptimizationReport.TypeDefinitions(new LinkedHashSet<>(), new LinkedHashSet<>());
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
