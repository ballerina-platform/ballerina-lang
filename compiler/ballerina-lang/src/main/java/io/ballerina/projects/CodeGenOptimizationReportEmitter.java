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

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Emits time durations and optimized node details for codegen optimization.
 *
 * @since 2201.9.0
 */
public class CodeGenOptimizationReportEmitter {

    private static final Map<PackageID, Long> birOptimizationDurations = new HashMap<>();
    private static long nativeOptimizationDuration = 0;
    private static final PrintStream out = System.out;

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
        out.println("Duration for unused BIR node analysis");
        birOptimizationDurations.forEach((key, value) -> out.printf("%8s : %dms%n", key, value));
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
            throw new RuntimeException(e);
        }
    }

    protected static void emitInvocationData(BPackageSymbol pkgSymbol) {
        out.println("/".repeat(60));
        out.printf("%s Optimization Report : %s %s%n", "-".repeat(5), pkgSymbol.pkgID, "-".repeat(5));
        out.println("/".repeat(60));

        out.println("Used Functions");
        out.println();

        for (BIRNode.BIRFunction function : pkgSymbol.invocationData.usedFunctions) {
            out.printf("%8s%n", function.originalName.value);
        }

        out.println();
        out.println("-".repeat(60));
        out.println();

        out.println("Deleted functions");
        out.println();
        for (BIRNode.BIRFunction function : pkgSymbol.invocationData.unusedFunctions) {
            out.printf("%8s%n", function.originalName.value);
        }

        out.println();
        out.println("-".repeat(60));
        out.println();

        out.println("Used Type definitions");
        out.println();
        for (BIRNode.BIRTypeDefinition typeDef : pkgSymbol.invocationData.usedTypeDefs) {
            out.printf("%8s%n", typeDef.originalName.value);
        }

        out.println();
        out.println("-".repeat(60));
        out.println();

        out.println("Deleted Type definitions");
        out.println();
        for (BIRNode.BIRTypeDefinition typeDef : pkgSymbol.invocationData.unusedTypeDefs) {
            out.printf("%8s%n", typeDef.originalName.value);
        }
    }

}
