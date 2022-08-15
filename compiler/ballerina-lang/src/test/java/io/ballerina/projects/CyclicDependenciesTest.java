/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.projects;

import guru.nidi.graphviz.model.MutableGraph;
import io.ballerina.projects.internal.ResolutionEngine;
import io.ballerina.projects.test.resolution.packages.internal.DotGraphUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Contains test cases to validate the detection of cyclic dependencies in {@code DependencyGraph}.
 */
public class CyclicDependenciesTest {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources", "cyclic-graphs");

    private static final ResolutionEngine.DependencyNode PACKAGE_01 = createNode("nipyf", "js", "1.0.2");
    private static final ResolutionEngine.DependencyNode PACKAGE_02 = createNode("nipyf", "toml", "1.0.0");
    private static final ResolutionEngine.DependencyNode PACKAGE_03 = createNode("nipyf", "yaml", "0.1.0");
    private static final ResolutionEngine.DependencyNode PACKAGE_04 = createNode("wso2", "cache", "2.0.1");
    private static final ResolutionEngine.DependencyNode PACKAGE_05 = createNode("wso2", "database", "3.2.2");
    private static final ResolutionEngine.DependencyNode PACKAGE_06 = createNode("wso2", "thread", "1.0.0");

    @Test
    public void testAcyclicDependencies() {
        MutableGraph mutableGraph = DotGraphUtils.createGraph(RESOURCE_DIRECTORY.resolve("case000.dot"));
        DependencyGraph<ResolutionEngine.DependencyNode> dependencyGraph =
                DotGraphUtils.createDependencyNodeGraph(mutableGraph);
        List<ResolutionEngine.DependencyNode> sortedDependencies =
                dependencyGraph.toTopologicallySortedList();

        Assert.assertEquals(dependencyGraph.findCycles().size(), 0);
        Assert.assertNotNull(sortedDependencies);
        Assert.assertEquals(sortedDependencies, List.of(PACKAGE_04, PACKAGE_02, PACKAGE_06, PACKAGE_01));
    }

    @Test(dataProvider = "provideCyclicDependencies")
    public void testCyclicDependencies(String testCase, List<List<ResolutionEngine.DependencyNode>> expectedCycles) {
        MutableGraph mutableGraph = DotGraphUtils.createGraph(RESOURCE_DIRECTORY.resolve(testCase + ".dot"));
        DependencyGraph<ResolutionEngine.DependencyNode> dependencyGraph =
                DotGraphUtils.createDependencyNodeGraph(mutableGraph);
        Set<List<ResolutionEngine.DependencyNode>> actualCycles = dependencyGraph.findCycles();

        Assert.assertNotNull(actualCycles);
        Assert.assertEquals(actualCycles.size(), expectedCycles.size());
        actualCycles.forEach(cycle -> Assert.assertTrue(expectedCycles.contains(cycle)));
    }

    @DataProvider(name = "provideCyclicDependencies")
    private Object[][] provideCyclicDependencies() {
        return new Object[][]{
                {"case001", List.of(
                        Arrays.asList(PACKAGE_01, PACKAGE_02, PACKAGE_04, PACKAGE_01),
                        Arrays.asList(PACKAGE_01, PACKAGE_04, PACKAGE_01)
                )},
                {"case002", List.of(
                        Arrays.asList(PACKAGE_01, PACKAGE_02, PACKAGE_03, PACKAGE_01),
                        Arrays.asList(PACKAGE_04, PACKAGE_05, PACKAGE_04),
                        Arrays.asList(PACKAGE_01, PACKAGE_04, PACKAGE_05, PACKAGE_03, PACKAGE_01)
                )},
                {"case003", List.of(
                        Arrays.asList(PACKAGE_04, PACKAGE_02, PACKAGE_04)
                )},
                {"case004", List.of(
                        Arrays.asList(PACKAGE_01, PACKAGE_02, PACKAGE_03, PACKAGE_01),
                        Arrays.asList(PACKAGE_01, PACKAGE_02, PACKAGE_04, PACKAGE_01),
                        Arrays.asList(PACKAGE_01, PACKAGE_02, PACKAGE_05, PACKAGE_01),
                        Arrays.asList(PACKAGE_01, PACKAGE_02, PACKAGE_06, PACKAGE_01)
                )},
                {"case005", List.of(
                        Arrays.asList(PACKAGE_01, PACKAGE_02, PACKAGE_01)
                )},
                {"case006", List.of(
                        Arrays.asList(PACKAGE_01, PACKAGE_02, PACKAGE_04, PACKAGE_01),
                        Arrays.asList(PACKAGE_02, PACKAGE_04, PACKAGE_03, PACKAGE_02),
                        Arrays.asList(PACKAGE_01, PACKAGE_03, PACKAGE_02, PACKAGE_04, PACKAGE_01)
                )},
                {"case007", List.of(
                        Arrays.asList(PACKAGE_01, PACKAGE_02, PACKAGE_03, PACKAGE_01),
                        Arrays.asList(PACKAGE_01, PACKAGE_05, PACKAGE_02, PACKAGE_03, PACKAGE_01),
                        Arrays.asList(PACKAGE_02, PACKAGE_03, PACKAGE_02),
                        Arrays.asList(PACKAGE_02, PACKAGE_03, PACKAGE_04, PACKAGE_05, PACKAGE_02),
                        Arrays.asList(PACKAGE_02, PACKAGE_03, PACKAGE_06, PACKAGE_04, PACKAGE_05, PACKAGE_02)
                )}
        };
    }

    private static ResolutionEngine.DependencyNode createNode(String org, String name, String version) {
        return new ResolutionEngine.DependencyNode(PackageDescriptor.from(
                PackageOrg.from(org), PackageName.from(name), PackageVersion.from(version)
        ));
    }
}
