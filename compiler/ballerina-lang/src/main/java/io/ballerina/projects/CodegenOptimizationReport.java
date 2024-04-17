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
 * Java class to store codegen optimization report data in json format.
 *
 * @since 2201.9.0
 */
public class CodegenOptimizationReport {

    FunctionNames usedFunctionNames;
    FunctionNames unusedFunctionNames;
    TypeDefinitions usedTypeDefNames;
    TypeDefinitions unusedTypeDefNames;
    Set<String> usedNativeClassPaths;

    public CodegenOptimizationReport(FunctionNames usedFunctionNames, FunctionNames unusedFunctionNames,
                                     TypeDefinitions usedTypeDefNames, TypeDefinitions unusedTypeDefNames,
                                     Set<String> usedNativeClassPaths) {
        this.usedFunctionNames = usedFunctionNames;
        this.unusedFunctionNames = unusedFunctionNames;
        this.usedTypeDefNames = usedTypeDefNames;
        this.unusedTypeDefNames = unusedTypeDefNames;
        this.usedNativeClassPaths = usedNativeClassPaths;
    }

    protected record FunctionNames(Set<String> sourceFunctions, Set<String> virtualFunctions) {

    }

    protected record TypeDefinitions(Set<String> sourceTypeDefinitions, Set<String> virtualTypeDefinitions) {

    }
}
