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
package io.ballerina.projects;

import guru.nidi.graphviz.model.MutableGraph;
import io.ballerina.projects.internal.DotGraphs;
import io.ballerina.projects.internal.ResolutionEngine.DependencyNode;
import io.ballerina.projects.test.resolution.packages.internal.DotGraphUtils;
import io.ballerina.projects.test.resolution.packages.internal.GraphComparisonResult;
import io.ballerina.projects.test.resolution.packages.internal.GraphUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.StringJoiner;

/**
 * Contains cases test {@code DotGraphs} utility.
 *
 * @since 2.0.0
 */
public class DotGraphsTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources",
            "dot-graphs");

    @Test(enabled = false)
    public void test() {
        String dotFileName = "all-in-one-graph.dot";
        MutableGraph expectedMutableGraph = DotGraphUtils.createGraph(RESOURCE_DIRECTORY.resolve(dotFileName));
        DependencyGraph<DependencyNode> expectedGraph =
                DotGraphUtils.createDependencyNodeGraph(expectedMutableGraph);

        String serializedGraph = DotGraphs.serializeDependencyNodeGraph(expectedGraph, Collections.emptyList());
        MutableGraph actualMutableGraph = DotGraphUtils.createGraph(serializedGraph);
        DependencyGraph<DependencyNode> actualGraph =
                DotGraphUtils.createDependencyNodeGraph(actualMutableGraph);

        GraphComparisonResult compResult = GraphUtils.compareGraph(actualGraph, expectedGraph);
        Assert.assertTrue(compResult.isIdenticalGraphs(), getDiagnosticLine(compResult.diagnostics()));
    }

    private String getDiagnosticLine(Collection<String> diagnostics) {
        StringJoiner strJoiner = new StringJoiner("\n");
        diagnostics.forEach(strJoiner::add);
        return strJoiner.toString();
    }
}
