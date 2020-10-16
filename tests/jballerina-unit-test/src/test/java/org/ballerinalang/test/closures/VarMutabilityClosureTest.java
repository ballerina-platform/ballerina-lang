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
package org.ballerinalang.test.closures;

import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.model.values.BXMLSequence;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Test cases for variable mutability in closure related scenarios in ballerina.
 *
 * @since 0.995.0
 */
public class VarMutabilityClosureTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/closures/var-mutability-closure.bal");
    }

    @Test(description = "Test basic closure operations with variable mutability")
    public void testBasicClosure() {
        BValue[] returns = BRunUtil.invoke(compileResult, "test1");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 49);
    }

    @Test(description = "Test two level closure operations with variable mutability")
    public void testTwoLevelClosure() {
        BValue[] returns = BRunUtil.invoke(compileResult, "test2");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 152);
    }

    @Test(description = "Test three level operations with variable mutability")
    public void testThreeLevelClosure1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "test3");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 388);
    }

    @Test(description = "Test with if block with variable mutability")
    public void testClosureWithIfBlock() {
        BValue[] returns = BRunUtil.invoke(compileResult, "test4");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 246);
    }

    @Test(description = "Test multi level function pointer call with variable mutability")
    public void testMultiLevelFPCallWithClosure() {
        BValue[] returns = BRunUtil.invoke(compileResult, "test5");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 250);
    }

    @Test(description = "Test variable mutability with tuples")
    public void testVarMutabilityWithTuples() {
        BValue[] returns = BRunUtil.invoke(compileResult, "test6");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 80);
        Assert.assertEquals(returns[1].stringValue(), "one-two-three-four-five-three-six");
    }

    @Test(description = "Test with arrays")
    public void testVarMutabilityWithArrays() {
        BValue[] returns = BRunUtil.invoke(compileResult, "test7");
        Assert.assertEquals(((BValueArray) returns[0]).size(), 9);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "a");
        Assert.assertEquals(((BValueArray) returns[0]).getString(1), "B");
        Assert.assertEquals(((BValueArray) returns[0]).getString(2), "e");
        Assert.assertEquals(((BValueArray) returns[0]).getString(3), "D");
        Assert.assertEquals(((BValueArray) returns[0]).getString(4), "i");
        Assert.assertEquals(((BValueArray) returns[0]).getString(5), "F");
        Assert.assertEquals(((BValueArray) returns[0]).getString(6), "o");
        Assert.assertEquals(((BValueArray) returns[0]).getString(7), "");
        Assert.assertEquals(((BValueArray) returns[0]).getString(8), "u");
    }


    @Test(description = "Test variable mutability with maps")
    public void testVarMutabilityWithMaps() {
        BValue[] returns = BRunUtil.invoke(compileResult, "test8");
        Assert.assertEquals(((BMap) returns[0]).size(), 8);
        Assert.assertEquals(((BMap) returns[0]).getMap().get("a").toString(), "AAAA");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("b").toString(), "BB");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("c").toString(), "C");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("d").toString(), "D");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("e").toString(), "EE");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("x").toString(), "XXXX");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("y").toString(), "YY");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("z").toString(), "ZZ");
    }


    @Test(description = "Test variable mutability with objects")
    public void testVarMutabilityWithObjects() {
        BValue[] returns = BRunUtil.invoke(compileResult, "test9");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("age").toString(), "25");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("name").toString(), "Adam");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("fullName").toString(), "Papadam Page");
    }

    @Test(description = "Test variable mutability with records")
    public void testVarMutabilityWithRecords() {
        BValue[] returns = BRunUtil.invoke(compileResult, "test10");

        LinkedHashMap resMap = ((BMap) returns[0]).getMap();
        Assert.assertEquals(resMap.get("age").toString(), "17");
        Assert.assertEquals(resMap.get("name").toString(), "Adam Page");
        Assert.assertEquals(resMap.get("email").toString(), "adamp@wso2.com");

        LinkedHashMap gradesMap = ((BMap) resMap.get("grades")).getMap();
        Assert.assertEquals(gradesMap.get("bio").toString(), "22");
        Assert.assertEquals(gradesMap.get("maths").toString(), "80");
        Assert.assertEquals(gradesMap.get("physics").toString(), "100");
        Assert.assertEquals(gradesMap.get("chemistry").toString(), "65");
    }

    @Test(description = "Test variable mutability with json objects")
    public void testVarMutabilityWithJSONObjects() {
        BValue[] returns = BRunUtil.invoke(compileResult, "test11");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("f1").toString(), "{\"name\":\"apple\", " +
                "\"color\":\"red\", \"price\":12.48}");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("f2").toString(), "{\"name\":\"orange\", " +
                "\"color\":\"orange\", \"price\":5.36}");
        Assert.assertEquals(((BMap) returns[0]).getMap().get("f3").toString(), "{\"name\":\"cherry\", " +
                "\"color\":\"red\", \"price\":5.36}");
    }

    @Test(description = "Test variable mutability with xml")
    public void testVarMutabilityWithXML() {
        BValue[] returns = BRunUtil.invoke(compileResult, "test12");
        Assert.assertEquals(returns[0].size(), 3);
        Assert.assertEquals(((BXMLSequence) returns[0]).getItem(0).toString(), "<book>The Princess Diaries" +
                "</book>");
        Assert.assertEquals(((BXMLSequence) returns[0]).getItem(1).toString(), "Hello, Princess!! :) ");
        Assert.assertEquals(((BXMLSequence) returns[0]).getItem(2).toString(), "<!--I am a comment-->");
    }

    @Test(description = "Test variable mutability with error types")
    public void testVarMutabilityWithError() {
        BValue[] returns = BRunUtil.invoke(compileResult, "test13");
        Assert.assertEquals(((BError) returns[0]).getReason(), "Account Not Found");
        String s = ((BError) returns[0]).getDetails().stringValue();
        Assert.assertEquals(s, "{accountID:222}");
    }

}
