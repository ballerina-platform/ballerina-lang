/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.bir;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.optimizer.ControlFlowGraph;
import org.wso2.ballerinalang.compiler.bir.optimizer.LivenessAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test to confirm the functionality of the {@link LivenessAnalyzer}.
 */
public class LivenessAnalysisTest {
    @Test(description = "Test the liveness analysis on a simple function")
    public void testBIRGen() {
        CompileResult result = BCompileUtil.compileAndGetBIR("test-src/bir/liveness.bal");
        Assert.assertEquals(result.getErrorCount(), 0);

        BIRNode.BIRFunction birFunction = ((BLangPackage) result.getAST()).symbol.bir.functions.stream().filter(
                func -> func.name.value.equals("testLiveness")).findFirst().get();

        ControlFlowGraph graph = new ControlFlowGraph(birFunction);
        LivenessAnalyzer analyzer = new LivenessAnalyzer(graph.getNodes());
        Map<BIRAbstractInstruction, Set<BIRNode.BIRVariableDcl>> liveOuts = analyzer.getInstructionLiveOuts();
        List<BIRNonTerminator> ls = birFunction.basicBlocks.get(0).instructions;
        int i = 0;
        BIRAbstractInstruction ins = ls.get(i++);
        assertLiveOut(liveOuts, ins, 1, ins.lhsOp.variableDcl);
        ins = ls.get(i++);
        assertLiveOut(liveOuts, ins, 2, ins.lhsOp.variableDcl);
        ins = ls.get(i++);
        assertLiveOut(liveOuts, ins, 1, ins.lhsOp.variableDcl);
        ins = ls.get(i++);
        assertLiveOut(liveOuts, ins, 2, ins.lhsOp.variableDcl);
        ins = ls.get(i++);
        assertLiveOut(liveOuts, ins, 0);
        ins = ls.get(i);
        assertLiveOut(liveOuts, ins, 0);
    }

    private void assertLiveOut(Map<BIRAbstractInstruction, Set<BIRNode.BIRVariableDcl>> liveOuts,
                               BIRAbstractInstruction ins, int size, BIRNode.BIRVariableDcl... vars) {
        Set<BIRNode.BIRVariableDcl> set = liveOuts.get(ins);
        Assert.assertEquals(set.size(), size);
        for (BIRNode.BIRVariableDcl var : vars) {
            Assert.assertTrue(set.contains(var));
        }
    }
}
