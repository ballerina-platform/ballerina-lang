// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package io.ballerina.cli.utils;

import io.ballerina.projects.Project;
import io.ballerina.projects.internal.model.Target;
import org.ballerinalang.test.runtime.entity.TestSuite;
import org.ballerinalang.testerina.core.TestProcessor;

import java.util.List;
import java.util.Map;

/**
 * Record for storing arguments to use in createTestSuitesForProject in TestUtils.
 *
 * @since 2201.9.0
 */

public record TestSuiteCreatingArgs(Project project, Target target, TestProcessor testProcessor,
                                    Map<String, TestSuite> testSuiteMap, List<String> moduleNamesList,
                                    List<String> mockClassNames, boolean isRerunTestExecution, boolean report,
                                    boolean coverage) {

}