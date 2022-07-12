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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test forward variable definitions not allowed.
 *
 * @since 0.990.3
 */
public class ForwardReferencingGlobalDefinitionTest {

    @Test(description = "Test compiler rejecting cyclic references in global variable definitions")
    public void globalDefinitionsWithCyclicReferences() {
        CompileResult resultNegativeCycleFound = BCompileUtil.compile(
                "test-src/statements/variabledef/globalcycle/simpleProject");
        Diagnostic[] diagnostics = resultNegativeCycleFound.getDiagnostics();
        Assert.assertTrue(diagnostics.length > 0);
        BAssertUtil.validateError(resultNegativeCycleFound, 0, "illegal cyclic reference '[person, employee]'", 17, 1);
        BAssertUtil.validateError(resultNegativeCycleFound, 1, "illegal cyclic reference '[dep2, dep1]'", 24, 1);
    }

    @Test(description = "Test re-ordering global variable initializations to satisfy dependency order")
    @SuppressWarnings("unchecked")
    public void globalDefinitionsReOrdering() {
        CompileResult resultReOrdered = BCompileUtil.
                compile("test-src/statements/variabledef/multiFileReferenceProject");
        Diagnostic[] diagnostics = resultReOrdered.getDiagnostics();
        Assert.assertEquals(diagnostics.length, 0);

        Object employee = BRunUtil.invoke(resultReOrdered, "getEmployee");
        String employeeName = ((BMap) employee).get(StringUtils.fromString("name")).toString();
        Assert.assertEquals(employeeName, "Sumedha");
    }

    @Test
    public void moduleVarReOrderingViaTypes() {
        CompileResult resultReOrdered =
                BCompileUtil.compile("test-src/statements/variabledef/moduleVarReOrderingViaTypes");
        Diagnostic[] diagnostics = resultReOrdered.getDiagnostics();
        Assert.assertEquals(diagnostics.length, 0);

        BRunUtil.invoke(resultReOrdered, "testModuleVariables");
    }

    @Test(description = "Test global variable reference in function")
    @SuppressWarnings("unchecked")
    public void inFunctionGlobalReference() {
        CompileResult resultReOrdered = BCompileUtil.
                compile("test-src/statements/variabledef/inFunctionGlobalRefProject");

        Object employee = BRunUtil.invoke(resultReOrdered, "getEmployee");
        String employeeName = ((BMap) employee).get(StringUtils.fromString("name")).toString();
        Assert.assertEquals(employeeName, "Sumedha");

        Object employee2 = BRunUtil.invoke(resultReOrdered, "getfromFuncA");
        String employee2Name = ((BMap) employee2).get(StringUtils.fromString("name")).toString();
        Assert.assertEquals(employee2Name, "Sumedha");
    }

    @Test(description = "Test conservative global variable re-ordering")
    @SuppressWarnings("unchecked")
    public void doNotPerformUnwantedReOrderings() {
        CompileResult resultReOrdered = BCompileUtil.compile("test-src/statements/variabledef/global-var-order.bal");

        BArray valI = (BArray) BRunUtil.invoke(resultReOrdered, "getIJK");
        Assert.assertEquals(valI.get(0), 2L); // i = 2
        Assert.assertEquals(valI.get(1), 1L); // j = 1
        Assert.assertEquals(valI.get(2), 2L); // k = 2
    }

    @Test(description = "Test global variable reference cycle via function")
    public void inFunctionGlobalReferenceCauseCycle() {
        CompileResult cycle = BCompileUtil.compile("test-src/statements/variabledef/globalcycle/viaFuncProject");

        Assert.assertTrue(cycle.getDiagnostics().length > 0);
        BAssertUtil.validateError(cycle, 0, "illegal cyclic reference '[fromFuncA, fromFunc, getPersonOuter, " +
                "getPersonInner, getfromFuncA]'", 22, 1);
    }

    @Test
    public void recordDefaultValueCausingModuleLevelVariableCycle() {
        CompileResult cycle = BCompileUtil.compile("test-src/statements/variabledef/globalcycle/viaRecordFieldDefault");
        int i = 0;
        BAssertUtil.validateError(cycle, i++,
                "illegal cyclic reference '[gVarNested, nestedRec, $anonType$_2, $anonType$_1]'", 26, 1);
        BAssertUtil.validateError(cycle, i++, "illegal cyclic reference '[name, person, Person]'", 17, 1);
        BAssertUtil.validateError(cycle, i++, "illegal cyclic reference '[gVar, p, $anonType$_0]'", 19, 1);
        BAssertUtil.validateError(cycle, i++,
                "illegal cyclic reference '[nillableNestedRecordGVar, nillableNestedRec, $anonType$_4, $anonType$_3]'",
                21, 1);
        BAssertUtil.validateError(cycle, i++,
                "illegal cyclic reference '[nestedRecordFieldDefaultValue, A, $anonType$_5]'", 23, 1);
        Assert.assertEquals(cycle.getDiagnostics().length, i);
    }

    @Test
    public void moduleVariableWithLetExprCausingCycle() {
        CompileResult cycle = BCompileUtil.compile("test-src/statements/variabledef/globalcycle/viaModuleLevelLetExpr");
        int i = 0;
        BAssertUtil.validateError(cycle, i++, "illegal cyclic reference '[fieldOne, conf1]'", 21, 1);
        // This error should go away and cycling error should come here when #21382 is fixed.
        BAssertUtil.validateError(cycle, i++, "let expressions are not yet supported for record fields", 30, 16);
        BAssertUtil.validateError(cycle, i++, "illegal cyclic reference '[letRef, modVar]'", 17, 1);
        BAssertUtil.validateError(cycle, i++,
                "illegal cyclic reference '[modVarQueryLet1, queryRef, modVarQuery]'", 19, 1);
        BAssertUtil.validateError(cycle, i++, "illegal cyclic reference '[modVarQueryLet2, queryRef2]'", 20, 1);

        Assert.assertEquals(cycle.getDiagnostics().length, i);
    }

    @Test(description = "Test compiler rejecting cyclic references in global variable definitions via object def")
    public void globalDefinitionsListenerDef() {
        CompileResult resultNegativeCycleFound = BCompileUtil.
                compile("test-src/statements/variabledef/globalcycle/viaServiceProject");
        Assert.assertEquals(resultNegativeCycleFound.getDiagnostics().length, 2);
        BAssertUtil.validateError(resultNegativeCycleFound, 0, "illegal cyclic reference '[port, o, Obj]'", 20, 1);
        BAssertUtil.validateHint(resultNegativeCycleFound, 1, "concurrent calls will not be made to this method " +
                "since the method is not an 'isolated' method", 32, 5);
    }
}
