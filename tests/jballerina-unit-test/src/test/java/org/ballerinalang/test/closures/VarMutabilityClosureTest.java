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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BXmlSequence;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        Object returns = BRunUtil.invoke(compileResult, "test1");
        Assert.assertEquals(returns, 49L);
    }

    @Test(description = "Test two level closure operations with variable mutability")
    public void testTwoLevelClosure() {
        Object returns = BRunUtil.invoke(compileResult, "test2");
        Assert.assertEquals(returns, 152L);
    }

    @Test(description = "Test three level operations with variable mutability")
    public void testThreeLevelClosure1() {
        Object returns = BRunUtil.invoke(compileResult, "test3");
        Assert.assertEquals(returns, 388L);
    }

    @Test(description = "Test with if block with variable mutability")
    public void testClosureWithIfBlock() {
        Object returns = BRunUtil.invoke(compileResult, "test4");
        Assert.assertEquals(returns, 246L);
    }

    @Test(description = "Test multi level function pointer call with variable mutability")
    public void testMultiLevelFPCallWithClosure() {
        Object returns = BRunUtil.invoke(compileResult, "test5");
        Assert.assertEquals(returns, 250L);
    }

    @Test(description = "Test variable mutability with tuples")
    public void testVarMutabilityWithTuples() {
        Object result = BRunUtil.invoke(compileResult, "test6");
        BArray returns = (BArray) result;
        Assert.assertEquals(returns.get(0), 80L);
        Assert.assertEquals(returns.get(1).toString(), "one-two-three-four-five-three-six");
    }

    @Test(description = "Test with arrays")
    public void testVarMutabilityWithArrays() {
        Object returns = BRunUtil.invoke(compileResult, "test7");
        Assert.assertEquals(((BArray) returns).size(), 9);
        Assert.assertEquals(((BArray) returns).getString(0), "a");
        Assert.assertEquals(((BArray) returns).getString(1), "B");
        Assert.assertEquals(((BArray) returns).getString(2), "e");
        Assert.assertEquals(((BArray) returns).getString(3), "D");
        Assert.assertEquals(((BArray) returns).getString(4), "i");
        Assert.assertEquals(((BArray) returns).getString(5), "F");
        Assert.assertEquals(((BArray) returns).getString(6), "o");
        Assert.assertEquals(((BArray) returns).getString(7), "");
        Assert.assertEquals(((BArray) returns).getString(8), "u");
    }


    @Test(description = "Test variable mutability with maps")
    public void testVarMutabilityWithMaps() {
        Object returns = BRunUtil.invoke(compileResult, "test8");
        Assert.assertEquals(((BMap) returns).size(), 8);
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("a")).toString(), "AAAA");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("b")).toString(), "BB");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("c")).toString(), "C");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("d")).toString(), "D");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("e")).toString(), "EE");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("x")).toString(), "XXXX");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("y")).toString(), "YY");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("z")).toString(), "ZZ");
    }


    @Test(description = "Test variable mutability with objects")
    public void testVarMutabilityWithObjects() {
        Object returns = BRunUtil.invoke(compileResult, "test9");
        Assert.assertEquals(((BObject) returns).get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(((BObject) returns).get(StringUtils.fromString("name")).toString(), "Adam");
        Assert.assertEquals(((BObject) returns).get(StringUtils.fromString("fullName")).toString(), "Papadam Page");
    }

    @Test(description = "Test variable mutability with records")
    public void testVarMutabilityWithRecords() {
        Object returns = BRunUtil.invoke(compileResult, "test10");

        BMap resMap = ((BMap) returns);
        Assert.assertEquals(resMap.get(StringUtils.fromString("age")).toString(), "17");
        Assert.assertEquals(resMap.get(StringUtils.fromString("name")).toString(), "Adam Page");
        Assert.assertEquals(resMap.get(StringUtils.fromString("email")).toString(), "adamp@wso2.com");

        BMap gradesMap = (BMap) resMap.get(StringUtils.fromString("grades"));
        Assert.assertEquals(gradesMap.get(StringUtils.fromString("bio")).toString(), "22");
        Assert.assertEquals(gradesMap.get(StringUtils.fromString("maths")).toString(), "80");
        Assert.assertEquals(gradesMap.get(StringUtils.fromString("physics")).toString(), "100");
        Assert.assertEquals(gradesMap.get(StringUtils.fromString("chemistry")).toString(), "65");
    }

    @Test(description = "Test variable mutability with json objects")
    public void testVarMutabilityWithJSONObjects() {
        Object returns = BRunUtil.invoke(compileResult, "test11");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("f1")).toString(), "{\"name\":\"apple\"," +
                "\"color\":\"red\",\"price\":12.48}");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("f2")).toString(), "{\"name\":\"orange\"," +
                "\"color\":\"orange\",\"price\":5.36}");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("f3")).toString(), "{\"name\":\"cherry\"," +
                "\"color\":\"red\",\"price\":5.36}");
    }

    @Test(description = "Test variable mutability with xml")
    public void testVarMutabilityWithXML() {
        Object returns = BRunUtil.invoke(compileResult, "test12");
        Assert.assertEquals(((BXmlSequence) returns).size(), 3);
        Assert.assertEquals(((BXmlSequence) returns).getItem(0).toString(), "<book>The Princess Diaries" +
                "</book>");
        Assert.assertEquals(((BXmlSequence) returns).getItem(1).toString(), "Hello, Princess!! :) ");
        Assert.assertEquals(((BXmlSequence) returns).getItem(2).toString(), "<!--I am a comment-->");
    }

    @Test(description = "Test variable mutability with error types")
    public void testVarMutabilityWithError() {
        Object returns = BRunUtil.invoke(compileResult, "test13");
        Assert.assertEquals(((BError) returns).getErrorMessage().toString(), "Account Not Found");
        String s = ((BError) returns).getDetails().toString();
        Assert.assertEquals(s, "{\"accountID\":222}");
    }

}
