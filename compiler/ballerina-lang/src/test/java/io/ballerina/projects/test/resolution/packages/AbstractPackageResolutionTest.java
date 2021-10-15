/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.test.resolution.packages;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;
import io.ballerina.projects.test.resolution.packages.internal.GraphComparisonResult;
import io.ballerina.projects.test.resolution.packages.internal.GraphUtils;
import io.ballerina.projects.test.resolution.packages.internal.PackageResolutionTestCase;
import io.ballerina.projects.test.resolution.packages.internal.PackageResolutionTestCaseBuilder;
import io.ballerina.projects.test.resolution.packages.internal.TestCaseFilePaths;
import io.ballerina.projects.test.resolution.packages.internal.TestCaseFilePaths.TestCaseFilePathsBuilder;
import org.testng.Assert;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.StringJoiner;

/**
 * Represents an abstract package resolution test case.
 * <p>
 * This class contains utility methods required for package resolution test cases.
 *
 * @since 2.0.0
 */
public abstract class AbstractPackageResolutionTest {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources",
            "package-resolution");

    public void runTestCase(String testSuiteDirName, String testCaseDirName, boolean sticky) {
        Path testSuitePath = RESOURCE_DIRECTORY.resolve(testSuiteDirName);
        TestCaseFilePaths filePaths = TestCaseFilePathsBuilder.build(testSuitePath,
                testSuitePath.resolve(testCaseDirName));
        PackageResolutionTestCase resolutionTestCase = PackageResolutionTestCaseBuilder.build(filePaths, sticky);
        DependencyGraph<DependencyNode> actualGraph = resolutionTestCase.execute(sticky);
        DependencyGraph<DependencyNode> expectedGraph = resolutionTestCase.getExpectedGraph(sticky);
        GraphComparisonResult compResult = GraphUtils.compareGraph(actualGraph, expectedGraph);
        Assert.assertTrue(compResult.isIdenticalGraphs(), getDiagnosticLine(compResult.diagnostics()));
    }

    private String getDiagnosticLine(Collection<String> diagnostics) {
        StringJoiner strJoiner = new StringJoiner("\n");
        diagnostics.forEach(strJoiner::add);
        return strJoiner.toString();
    }
}
