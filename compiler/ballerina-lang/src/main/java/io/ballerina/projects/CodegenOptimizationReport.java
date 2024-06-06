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

import java.util.Set;

/**
 * Java record used to store codegen optimization report data to JSON.
 *
 * @param usedFunctionNames   used BIR functions identified by the UsedBIRNodeAnalyzer
 * @param unusedFunctionNames unused BIR functions identified by the UsedBIRNodeAnalyzer
 * @param usedTypeDefNames    used BIR type definitions identified by the UsedBIRNodeAnalyzer
 * @param unusedTypeDefNames  unused BIR type definitions identified by the UsedBIRNodeAnalyzer
 * @param usedNativeClassPaths native class paths used by external functions
 *
 * @since 2201.10.0
 */
public record CodegenOptimizationReport(FunctionNames usedFunctionNames, FunctionNames unusedFunctionNames,
                                        TypeDefinitions usedTypeDefNames, TypeDefinitions unusedTypeDefNames,
                                        Set<String> usedNativeClassPaths) {

    /**
     * @param sourceFunctions  BIR functions directly derived from source
     * @param virtualFunctions BIR functions generated during desugar
     */
    protected record FunctionNames(Set<String> sourceFunctions, Set<String> virtualFunctions) {

    }

    /**
     * @param sourceTypeDefinitions  BIR type definitions directly derived from source
     * @param virtualTypeDefinitions BIR type definitions generated during desugar
     */
    protected record TypeDefinitions(Set<String> sourceTypeDefinitions, Set<String> virtualTypeDefinitions) {

    }
}
