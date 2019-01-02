/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.variabledef;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.analyzer.cyclefind.TarjanSccSolverAdjacencyList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Test forward variable definitions not allowed.
 *
 * @since 0.990.3
 */
public class ForwardReferencingGlobalDefinitionTest {
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        resultNegative = BCompileUtil.
                compile("test-src/statements/variabledef/forward-reference-in-global-vardef-negative.bal");
    }

    @Test(description = "Test compiler rejecting forward referenced global variables")
    public void globalDefinitionsWithForwardReferences() {
        Diagnostic[] diagnostics = resultNegative.getDiagnostics();
        Assert.assertTrue(diagnostics.length > 0);
        BAssertUtil.validateError(resultNegative, 0, "illegal forward reference to 'employee'", 32, 26);
        BAssertUtil.validateError(resultNegative, 1, "illegal forward reference to 'person'", 32, 36);
        BAssertUtil.validateError(resultNegative, 2, "illegal forward reference to 'person'", 35, 11);
        BAssertUtil.validateError(resultNegative, 3, "illegal forward reference to 'person'", 36, 10);
    }

    @Test(description = "Test algorithms on graph with cycles")
    public void dependencyGraphWithCycles() {
        int n = 8;
        TarjanSccSolverAdjacencyList graph = TarjanSccSolverAdjacencyList.createGraph(n);

        graph.addEdge(6, 0);
        graph.addEdge(6, 2);
        graph.addEdge(3, 4);
        graph.addEdge(6, 4);
        graph.addEdge(2, 0);
        graph.addEdge(0, 1);
        graph.addEdge(4, 5);
        graph.addEdge(5, 6);
        graph.addEdge(3, 7);
        graph.addEdge(7, 5);
        graph.addEdge(1, 2);
        graph.addEdge(7, 3);
        graph.addEdge(5, 0);

        Map<Integer, List<Integer>> multimap = graph.getSCCs();
        Assert.assertEquals(graph.sccCount(), 3);
        Assert.assertEquals(multimap.get(0), Arrays.asList(0, 1, 2));
        Assert.assertEquals(multimap.get(3), Arrays.asList(3, 7));
        Assert.assertEquals(multimap.get(4), Arrays.asList(4, 5, 6));

        Assert.assertEquals(graph.dependencyOrder, Arrays.asList(2, 1, 0, 6, 5, 4, 7, 3));
    }

    @Test(description = "Test cycle find algorithm with no cycles")
    public void cycleFindAlgorithmNoCycles() {
        int n = 5;
        TarjanSccSolverAdjacencyList graph = TarjanSccSolverAdjacencyList.createGraph(n);

        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(1, 4);
        graph.addEdge(2, 4);

        Assert.assertEquals(graph.sccCount(), 5);

        int index4 = graph.dependencyOrder.indexOf(4);
        int index2 = graph.dependencyOrder.indexOf(2);
        int index1 = graph.dependencyOrder.indexOf(1);
        int index0 = graph.dependencyOrder.indexOf(0);

        Assert.assertTrue(index4 < index2);
        Assert.assertTrue(index2 < index1);
        Assert.assertTrue(index1 < index0);
    }

    @Test(description = "Test cycle find algorithm with 2 nodes")
    public void cycleFindAlgorithmTwoNodes() {
        int n = 2;
        TarjanSccSolverAdjacencyList graph = TarjanSccSolverAdjacencyList.createGraph(n);

        graph.addEdge(0, 1);
        graph.addEdge(1, 0);

        Map<Integer, List<Integer>> multimap = graph.getSCCs();
        Assert.assertEquals(graph.sccCount(), 1);
        Assert.assertEquals(multimap.get(0), Arrays.asList(0, 1));
    }
}
