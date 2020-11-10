/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test forward variable definitions not allowed.
 *
 * @since 0.990.3
 */
public class ForwardReferencingGlobalDefinitionTest {

    @Test(description = "Test compiler rejecting cyclic references in global variable definitions",
            groups = "brokenOnErrorChange")
    public void globalDefinitionsWithCyclicReferences() {
        CompileResult resultNegativeCycleFound = BCompileUtil.compile("test-src/statements/variabledef/globalcycle/",
                "simple");
        Diagnostic[] diagnostics = resultNegativeCycleFound.getDiagnostics();
        Assert.assertTrue(diagnostics.length > 0);
        BAssertUtil.validateError(resultNegativeCycleFound, 0, "illegal cyclic reference '[employee, person]'", 35, 1);
        BAssertUtil.validateError(resultNegativeCycleFound, 1, "illegal cyclic reference '[dep1, dep2]'", 54, 1);
    }

    @Test(description = "Test re-ordering global variable initializations to satisfy dependency order")
    @SuppressWarnings("unchecked")
    public void globalDefinitionsReOrdering() {
        CompileResult resultReOrdered = BCompileUtil.compile(this, "test-src/statements/variabledef/TestProj/",
                "multiFileReference");
        Diagnostic[] diagnostics = resultReOrdered.getDiagnostics();
        Assert.assertEquals(diagnostics.length, 0);

        BValue[] employee = BRunUtil.invoke(resultReOrdered, "getEmployee");
        String employeeName = ((BMap) employee[0]).get("name").stringValue();
        Assert.assertEquals(employeeName, "Sumedha");
    }

    @Test(description = "Test global variable reference in function")
    @SuppressWarnings("unchecked")
    public void inFunctionGlobalReference() {
        CompileResult resultReOrdered = BCompileUtil.compile(this, "test-src/statements/variabledef/TestProj/",
                "inFunctionGlobalRef");

        BValue[] employee = BRunUtil.invoke(resultReOrdered, "getEmployee");
        String employeeName = ((BMap) employee[0]).get("name").stringValue();
        Assert.assertEquals(employeeName, "Sumedha");

        BValue[] employee2 = BRunUtil.invoke(resultReOrdered, "getfromFuncA");
        String employee2Name = ((BMap) employee2[0]).get("name").stringValue();
        Assert.assertEquals(employee2Name, "Sumedha");
    }

    @Test(description = "Test conservative global variable re-ordering")
    @SuppressWarnings("unchecked")
    public void doNotPerformUnwantedReOrderings() {
        CompileResult resultReOrdered = BCompileUtil.compile("test-src/statements/variabledef/global-var-order.bal");

        BValue[] valI = BRunUtil.invoke(resultReOrdered, "getIJK");
        Assert.assertEquals(((BInteger) valI[0]).intValue(), 2); // i = 2
        Assert.assertEquals(((BInteger) valI[1]).intValue(), 1); // j = 1
        Assert.assertEquals(((BInteger) valI[2]).intValue(), 2); // k = 2
    }

    @Test(description = "Test global variable reference cycle via function")
    public void inFunctionGlobalReferenceCauseCycle() {
        CompileResult cycle = BCompileUtil.compile("test-src/statements/variabledef/globalcycle/",
                                                   "viafunc");

        Assert.assertTrue(cycle.getDiagnostics().length > 0);
        BAssertUtil.validateError(cycle, 0, "illegal cyclic reference '[fromFuncA, fromFunc, getPersonOuter, " +
                "getPersonInner, getfromFuncA]'", 22, 1);
    }

    @Test(description = "Test compiler rejecting cyclic references in global variable definitions via object def",
            groups = "brokenOnErrorChange")
    public void globalDefinitionsListenerDef() {
        CompileResult resultNegativeCycleFound = BCompileUtil.compile(this,
                "test-src/statements/variabledef/globalcycle/", "viaservice");
        Assert.assertEquals(resultNegativeCycleFound.getDiagnostics().length, 1);
        BAssertUtil.validateError(resultNegativeCycleFound, 0, "illegal cyclic reference '[port, o, Obj]'", 22, 1);
    }
}
