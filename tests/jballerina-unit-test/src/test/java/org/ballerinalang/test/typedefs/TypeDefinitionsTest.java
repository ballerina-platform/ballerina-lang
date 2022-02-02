/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.typedefs;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Type definition test cases.
 */
public class TypeDefinitionsTest {

    private CompileResult compileResult;
//    private CompileResult recordFieldRes;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/typedefs/type-definitions.bal");
//        recordFieldRes = BCompileUtil.compile("test-src/typedefs/record-type-field-resolving.bal");
    }

    @Test
    public void testArray() {
        Object returns = JvmRunUtil.invoke(compileResult, "testArray");
        Assert.assertNotNull(returns);
        Assert.assertEquals(((BArray) returns).getInt(0), 1);
        Assert.assertEquals(((BArray) returns).getInt(1), 2);
        Assert.assertEquals(((BArray) returns).getInt(2), 3);
    }

    @Test
    public void testSimpleTuple() {
        BArray returns = (BArray) JvmRunUtil.invoke(compileResult, "testSimpleTuple");
        Assert.assertEquals(returns.get(0), 10L);
        Assert.assertEquals(returns.get(1).toString(), "Ten");
    }

    @Test
    public void testMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testMap");
        Assert.assertEquals(returns.toString(), "{\"Five\":5}");
    }

    @Test
    public void testValueType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testValueType");
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testRecord() {
        Object returns = JvmRunUtil.invoke(compileResult, "testRecord");
        Assert.assertEquals(returns.toString(), "{\"f\":\"Ballerina\"}");
    }

    @Test
    public void testObject() {
        Object returns = JvmRunUtil.invoke(compileResult, "testObject");
        Assert.assertEquals(returns.toString(), "{}");
    }

    @Test
    public void testUnion() {
        Object returns = JvmRunUtil.invoke(compileResult, "testUnion");
        Assert.assertEquals(returns.toString(), "{g:}");
    }

    @Test
    public void testComplexTuple() {
        BArray returns = (BArray) JvmRunUtil.invoke(compileResult, "testComplexTuple");
        Assert.assertEquals(returns.get(0).toString(), "[1,2]");
        Assert.assertEquals(returns.get(1).toString(), "[3,4]");
        Assert.assertEquals(returns.get(2).toString(), "[2,\"Two\"]");
        Assert.assertEquals(returns.get(3).toString(), "{\"k\":\"v\"}");
        Assert.assertEquals(returns.get(4).toString(), "{\"k\":1}");
        Assert.assertEquals(returns.get(5).toString(), "Ballerina");
        Assert.assertEquals(returns.get(6).toString(), "10");
        Assert.assertEquals(returns.get(7).toString(), "{\"f\":\"Ballerina\"}");
        Assert.assertEquals(returns.get(8).toString(), "{g:}");
        Assert.assertEquals(returns.get(9).toString(), "error Err (\"reason\")");
    }

    @Test
    public void testComplexUnion() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexUnion");
        Assert.assertNotNull(returns);
        Assert.assertEquals(((BArray) returns).getInt(0), 4);
        Assert.assertEquals(((BArray) returns).getInt(1), 5);
        Assert.assertEquals(((BArray) returns).getInt(2), 6);
    }

    @Test
    public void testUnionInTuple() {
        BArray returns = (BArray) JvmRunUtil.invoke(compileResult, "testUnionInTuple");
        Assert.assertEquals(returns.get(0).toString(), "[4,5,6]");
        Assert.assertEquals(returns.get(1).toString(), "[10,20]");
    }

    @Test
    public void testXml() {
        Object returns = JvmRunUtil.invoke(compileResult, "testXml");
        Assert.assertEquals(returns.toString(), "<name>ballerina</name>");
    }

    @Test
    public void testAnonRecordUnionTypeDef() {
        Object returns = JvmRunUtil.invoke(compileResult, "testAnonRecordUnionTypeDef");
    }

    @Test
    public void testAnonObjectUnionTypeDef() {
        Object returns = JvmRunUtil.invoke(compileResult, "testAnonObjectUnionTypeDef");
    }

    @Test
    public void testAnonExclusiveRecordUnionTypeDef() {
        Object returns = JvmRunUtil.invoke(compileResult, "testAnonExclusiveRecordUnionTypeDef");
    }

    @Test
    public void testIntArrayTypeDef() {
        JvmRunUtil.invoke(compileResult, "testIntArrayTypeDef");
    }

    @Test
    public void testTupleTypeDef() {
        JvmRunUtil.invoke(compileResult, "testTupleTypeDef");
    }

    @Test
    public void testTypeDefReferringToTypeDefDefinedAfter() {
        JvmRunUtil.invoke(compileResult, "testTypeDefReferringToTypeDefDefinedAfter");
    }

    @Test
    public void testFuncInvocationWithinTypeDef() {
        JvmRunUtil.invoke(compileResult, "testFuncInvocation");
    }

    @Test
    public void testClassDefinition() {
        JvmRunUtil.invoke(compileResult, "testClassDefn");
    }

    @Test
    public void testBinaryExprAssignments() {
        JvmRunUtil.invoke(compileResult, "testBinaryExprAssignments");
    }

//    @Test
//    public void testRecordTypeResolving() {
//        JvmRunUtil.invoke(recordFieldRes, "testRecordTypeResolving");
//    }
//
//    @Test
//    public void testRecordTypeResolvingWithTypeInclusion() {
//        JvmRunUtil.invoke(recordFieldRes, "testRecordTypeResolvingWithTypeInclusion");
//    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
//        recordFieldRes = null;
    }
}
