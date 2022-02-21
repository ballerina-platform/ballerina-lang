/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.types.tuples;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tuple access with dynamic indexes test.
 *
 * @since 0.990.4
 */
public class TupleAccessExprTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/tuples/tuple_access_expr.bal");
    }

    @Test(description = "Test tuple access expression with dynamic index")
    public void testTupleAccessExpr() {
        Object returns = BRunUtil.invoke(compileResult, "tupleAccessTest");

        Assert.assertEquals(returns.toString(), "100:int:string_value:string:true:boolean::()");
    }

    @Test(description = "Test tuple containing behavioral values access with dynamic index")
    public void tupleAccessTestWithBehavioralValues() {
        Object returns = BRunUtil.invoke(compileResult, "tupleAccessTestWithBehavioralValues");

        Assert.assertEquals(returns.toString(), "10:object:8:function:str:string|json:json:string|json:");
    }

    @Test(description = "Test tuple index based access with a function as the index")
    public void tupleIndexAsFunction() {
        Object[] args = {StringUtils.fromString("0")};
        Object returns = BRunUtil.invoke(compileResult, "tupleIndexAsFunction", args);

        Assert.assertEquals(returns.toString(), "string");

        Object[] args2 = {StringUtils.fromString("1")};
        returns = BRunUtil.invoke(compileResult, "tupleIndexAsFunction", args2);

        Assert.assertEquals(returns.toString(), "9.0");

        Object[] args3 = {StringUtils.fromString("2")};
        returns = BRunUtil.invoke(compileResult, "tupleIndexAsFunction", args3);

        Assert.assertEquals(returns.toString(), "true");
    }

    @Test(description = "Test accessing a tuple with an invalid index passed as a dynamic index",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                    "\\{\"message\":\"tuple index out of range: index: -1, size: 3.*")
    public void tupleInvalidIndexAsFunction() {
        Object[] args = {StringUtils.fromString("-1")};
        BRunUtil.invoke(compileResult, "tupleIndexAsFunction", args);
    }

    @Test(description = "Test accessing tuple which includes a union")
    public void tupleWithUnionType() {
        Object returns = BRunUtil.invoke(compileResult, "tupleWithUnionType");

        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test accessing tuple inside tuple using dynamic indexes")
    public void tupleInsideTupleAccess() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "tupleInsideTupleAccess");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertTrue((Boolean) returns.get(1));
    }

    @Test(description = "Test index out of bounds due to a dynamic index",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                    "\\{\"message\":\"tuple index out of range: index: 2, size: 2.*")
    public void tupleIndexOutOfBoundTest1() {
        BRunUtil.invoke(compileResult, "tupleIndexOutOfBoundTest1");
    }

    @Test(description = "Test index out of bounds due to a dynamic index",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                    "\\{\"message\":\"tuple index " +
                    "out of range: index: -1, size: 2.*")
    public void tupleIndexOutOfBoundTest2() {
        BRunUtil.invoke(compileResult, "tupleIndexOutOfBoundTest2");
    }

    @Test(description = "Test index out of bounds due to a dynamic index",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                    "\\{\"message\":\"tuple index " +
                    "out of range: index: 2, size: 2.*")
    public void tupleIndexOutOfBoundTest3() {
        BRunUtil.invoke(compileResult, "tupleIndexOutOfBoundTest3");
    }

    @Test(description = "Test index out of bounds due to a dynamic index",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                    "\\{\"message\":\"tuple index " +
                    "out of range: index: -1, size: 2.*")
    public void tupleIndexOutOfBoundTest4() {
        BRunUtil.invoke(compileResult, "tupleIndexOutOfBoundTest4");
    }

    @Test(description = "Test using a const as the index expression")
    public void testConstTupleIndex() {
        Object[] args = {(0)};
        Object returns = BRunUtil.invoke(compileResult, "testConstTupleIndex", args);

        Assert.assertEquals(returns, 12L);

        Object[] args2 = {(1)};
        returns = BRunUtil.invoke(compileResult, "testConstTupleIndex", args2);

        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test tuple access with a field based access expression as the index expression")
    public void tupleIndexAccessOfSameTypeWithIndexFromMap() {
        Object returns = BRunUtil.invoke(compileResult, "tupleIndexAccessOfSameTypeWithIndexFromMap");

        Assert.assertEquals(returns, 6.6);
    }

    @Test(description = "Test invalid type inserted to tuple due to a dynamic index",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}InherentTypeViolation " +
                    "\\{\"message\":\"incompatible types: expected 'string', found 'boolean'.*")
    public void testInvalidInsertionToTuple() {
        BRunUtil.invoke(compileResult, "testInvalidInsertionToTuple");
    }

    @Test(description = "Test assigning a tuple member to any and anydata by accessing with a dynamic index")
    public void testTupleAccessToAnyAndAnydata() {
        Object returns = BRunUtil.invoke(compileResult, "testTupleAccessToAnyAndAnydata");

        Assert.assertEquals(returns.toString(), "string:str:boolean:true");
    }

    @Test(description = "Test assigning a tuple member to any and anydata by accessing with a dynamic index")
    public void testTupleAccessUsingFiniteType() {
        Object returns = BRunUtil.invoke(compileResult, "testTupleAccessUsingFiniteType");

        Assert.assertEquals(returns.toString(), "stringtruestring");
    }

    @Test
    public void testTupleAccessUsingUnionWithFiniteTypes() {
        Object returns = BRunUtil.invoke(compileResult, "testTupleAccessUsingUnionWithFiniteTypes");

        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test invalid type inserted to tuple due to a dynamic index",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                    "\\{\"message\":\"tuple index " +
                    "out of range: index: 4, size: 4.*")
    public void testTupleAccessUsingFiniteTypeNegative() {
        BRunUtil.invoke(compileResult, "testTupleAccessUsingFiniteTypeNegative");
    }

    @Test(description = "Test invalid tuple access using a dynamic index",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                    "\\{\"message\":\"tuple index " +
                    "out of range: index: 6, size: 4.*")
    public void testTupleAccessUsingUnionWithFiniteTypesNegative() {
        BRunUtil.invoke(compileResult, "testTupleAccessUsingUnionWithFiniteTypesNegative");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
