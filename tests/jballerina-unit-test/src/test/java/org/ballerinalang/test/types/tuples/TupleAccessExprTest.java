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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] returns = BRunUtil.invoke(compileResult, "tupleAccessTest");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "100:int:string_value:string:true:boolean::()");
    }

    @Test(description = "Test tuple containing behavioral values access with dynamic index")
    public void tupleAccessTestWithBehavioralValues() {
        BValue[] returns = BRunUtil.invoke(compileResult, "tupleAccessTestWithBehavioralValues");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "10:object:8:function:str:string|json:json:string|json:");
    }

    @Test(description = "Test tuple index based access with a function as the index")
    public void tupleIndexAsFunction() {
        BValue[] args = {new BString("0")};
        BValue[] returns = BRunUtil.invoke(compileResult, "tupleIndexAsFunction", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "string");

        BValue[] args2 = {new BString("1")};
        returns = BRunUtil.invoke(compileResult, "tupleIndexAsFunction", args2);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "9.0");

        BValue[] args3 = {new BString("2")};
        returns = BRunUtil.invoke(compileResult, "tupleIndexAsFunction", args3);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test(description = "Test accessing a tuple with an invalid index passed as a dynamic index",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                    "\\{\"message\":\"tuple index out of range: index: -1, size: 3.*")
    public void tupleInvalidIndexAsFunction() {
        BValue[] args = {new BString("-1")};
        BRunUtil.invoke(compileResult, "tupleIndexAsFunction", args);
    }

    @Test(description = "Test accessing tuple which includes a union")
    public void tupleWithUnionType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "tupleWithUnionType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test accessing tuple inside tuple using dynamic indexes")
    public void tupleInsideTupleAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "tupleInsideTupleAccess");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
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
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstTupleIndex", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);

        BValue[] args2 = {new BInteger(1)};
        returns = BRunUtil.invoke(compileResult, "testConstTupleIndex", args2);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test tuple access with a field based access expression as the index expression")
    public void tupleIndexAccessOfSameTypeWithIndexFromMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "tupleIndexAccessOfSameTypeWithIndexFromMap");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 6.6);
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
        BValue[] returns = BRunUtil.invoke(compileResult, "testTupleAccessToAnyAndAnydata");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "string:str:boolean:true");
    }

    @Test(description = "Test assigning a tuple member to any and anydata by accessing with a dynamic index")
    public void testTupleAccessUsingFiniteType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTupleAccessUsingFiniteType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "stringtruestring");
    }

    @Test
    public void testTupleAccessUsingUnionWithFiniteTypes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTupleAccessUsingUnionWithFiniteTypes");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
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
}
