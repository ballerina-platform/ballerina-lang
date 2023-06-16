/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.bala.functions;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * BIR test cases for dependently-typed extern functions.
 *
 * @since 2.0.0
 */
public class DependentlyTypedFunctionsBalaTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        result = BCompileUtil.compile("test-src/javainterop/dependently_typed_functions_bir_test.bal");
    }

    @Test(expectedExceptions = BLangTestException.class,
          expectedExceptionsMessageRegExp = ".*error: \\{ballerina}TypeCastError \\{\"message\":\"incompatible types:" +
                  " 'map' cannot be cast to 'map<anydata>'.*")
    public void testRuntimeCastError() {
        BRunUtil.invoke(result, "testRuntimeCastError");
    }

    @Test(expectedExceptions = BLangTestException.class,
          expectedExceptionsMessageRegExp = ".*error: \\{ballerina}TypeCastError \\{\"message\":\"incompatible types:" +
                  " 'Person' cannot be cast to 'int'\"}.*")
    public void testCastingForInvalidValues() {
        BRunUtil.invoke(result, "testCastingForInvalidValues");
    }

    @Test(dataProvider = "FunctionNames")
    public void testVariableTypeAsReturnType(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider(name = "FunctionNames")
    public Object[][] getFuncNames() {
        return new Object[][]{
                {"testRecordVarRef"},
                {"testVarRefInMapConstraint"},
                {"testVarRefUseInMultiplePlaces"},
                {"testSimpleTypes"},
                {"testUnionTypes"},
                {"testArrayTypes"},
                {"testXML"},
                {"testStream"},
                {"testTable"},
                {"testFunctionPointers"},
                {"testTypedesc"},
                {"testFuture"},
                {"testComplexTypes"},
                {"testTypedescInferring"}
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
