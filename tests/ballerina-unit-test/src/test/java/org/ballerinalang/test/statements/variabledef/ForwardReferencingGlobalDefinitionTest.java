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
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.Assert;
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

    @Test(description = "Test compiler rejecting cyclic references in global variable definitions")
    public void globalDefinitionsWithCyclicReferences() {
        CompileResult resultNegativeCycleFound = BCompileUtil.compile(this,
                "test-src/statements/variabledef/globalcycle", "simple");
        Diagnostic[] diagnostics = resultNegativeCycleFound.getDiagnostics();
        Assert.assertTrue(diagnostics.length > 0);
        BAssertUtil.validateError(resultNegativeCycleFound, 0, "illegal cyclic reference '[employee, person]'", 19, 1);
        BAssertUtil.validateError(resultNegativeCycleFound, 1, "illegal cyclic reference '[dep1, dep2]'", 38, 1);
    }

    @Test(description = "Test re-ordering global variable initializations to satisfy dependency order")
    public void globalDefinitionsReOrdering() {
        CompileResult resultReOrdered = BCompileUtil.compile(this, "test-src/statements/variabledef",
                "multiFileReference");
        Diagnostic[] diagnostics = resultReOrdered.getDiagnostics();
        Assert.assertEquals(diagnostics.length, 0);

        BValue[] employee = BRunUtil.invoke(resultReOrdered, "getEmployee");
        String employeeName = ((BMap) employee[0]).get("name").stringValue();
        Assert.assertEquals(employeeName, "Sumedha");
    }

    @Test(description = "Test global variable reference in function")
    public void inFunctionGlobalReference() {
        CompileResult resultReOrdered = BCompileUtil.compile(this, "test-src/statements/variabledef",
                "inFunctionGlobalRef");

        BValue[] employee = BRunUtil.invoke(resultReOrdered, "getEmployee");
        String employeeName = ((BMap) employee[0]).get("name").stringValue();
        Assert.assertEquals(employeeName, "Sumedha");

        BValue[] employee2 = BRunUtil.invoke(resultReOrdered, "getfromFuncA");
        String employee2Name = ((BMap) employee2[0]).get("name").stringValue();
        Assert.assertEquals(employee2Name, "Sumedha");

        BValue[] valI = BRunUtil.invoke(resultReOrdered, "getIJK");
        Assert.assertEquals(((BInteger) valI[0]).intValue(), 2);
        Assert.assertEquals(((BInteger) valI[1]).intValue(), 1);
        Assert.assertEquals(((BInteger) valI[2]).intValue(), 2);
    }

    @Test(description = "Test global variable reference cycle via function")
    public void inFunctionGlobalReferenceCauseCycle() {
        CompileResult cycle = BCompileUtil.compile(this, "test-src/statements/variabledef/globalcycle",
                "viafunc");

        Assert.assertTrue(cycle.getDiagnostics().length > 0);
        BAssertUtil.validateError(cycle, 0, "illegal cyclic reference '[fromFuncA, fromFunc, getPersonOuter, " +
                "getPersonInner, getfromFuncA]'", 22, 1);
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
