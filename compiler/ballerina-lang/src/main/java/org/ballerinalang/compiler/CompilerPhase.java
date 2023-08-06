/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.compiler;

/**
 * {@code CompilerPhase} represents a phase of the compiler.
 *
 * Ideally, we should rename this class as ASTPass.
 *
 * @since 0.94
 */
public enum CompilerPhase {

    DEFINE("define"),

    TYPE_CHECK("typeCheck"),

    CODE_ANALYZE("codeAnalyze"), 

    DATAFLOW_ANALYZE("dataflowAnalyze"),

    ISOLATION_ANALYZE("isolationAnalyze"),

    DOCUMENTATION_ANALYZE("documentationAnalyze"),

    CONSTANT_PROPAGATION("constantPropagation"),

    COMPILER_PLUGIN("compilerPlugin"),

    DESUGAR("desugar"),

    BIR_GEN("birGen"),

    BIR_EMIT("birEmit"),

    CODE_GEN("codeGen"),

    DEAD_CODE_ANALYZE("deadCodeAnalyze");

    private final String value;

    CompilerPhase(String value) {
        this.value = value;
    }

    public static CompilerPhase fromValue(String value) {
        return switch (value) {
            case "define" -> DEFINE;
            case "typeCheck" -> TYPE_CHECK;
            case "codeAnalyze" -> CODE_ANALYZE;
            case "documentationAnalyze" -> DOCUMENTATION_ANALYZE;
            case "constantPropagation" -> CONSTANT_PROPAGATION;
            case "compilerPlugin" -> COMPILER_PLUGIN;
            case "desugar" -> DESUGAR;
            case "codeGen" -> CODE_GEN;
            case "birGen" -> BIR_GEN;
            case "birEmit" -> BIR_EMIT;
            case "deadCodeAnalyze" -> DEAD_CODE_ANALYZE;
            default -> throw new IllegalArgumentException("invalid compiler phase: " + value);
        };
    }

    @Override
    public String toString() {
        return value;
    }
}
