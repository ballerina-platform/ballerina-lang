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

import java.util.Set;

/**
 * Java record to store class level native optimization report data in json format.
 *
 * @param startPointClasses start points identified by the NativeDependencyOptimizer
 * @param usedExternalClasses used classes which are not present in the executable JAR (JRE classes)
 * @param usedClasses used classes present in the executable JAR
 * @param unusedClasses unused classes present in the executable JAR
 *
 * @since 2201.9.0
 */
public record NativeDependencyOptimizationReport(Set<String> startPointClasses, Set<String> usedExternalClasses,
                                                 Set<String> usedClasses, Set<String> unusedClasses) {

}
