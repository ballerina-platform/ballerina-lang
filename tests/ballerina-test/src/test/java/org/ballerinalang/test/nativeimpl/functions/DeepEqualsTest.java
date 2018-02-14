/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.test.nativeimpl.functions;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Test cases for ballerina.reflect:deepEquals() function.
 */
public class DeepEqualsTest {
    CompileResult compileResultForPrimitives;
    CompileResult compileResultForComplex;
    
    @BeforeClass
    public void setup() {
        compileResultForPrimitives = BCompileUtil.compile("test-src/reflect/deep-equal.bal");
        compileResultForComplex = BCompileUtil.compile("test-src/reflect/deep-equal-complex.bal");
    }
    
    /**
     * Data Provider which provides all FunctionInfo items in the bal file.
     * @return FunctionInfo of the bal file..
     * @throws FileNotFoundException Unable to find test bal file.
     */
    @DataProvider(name = "DeepEqualsValidFunctionInfos")
    public Object[][] functionNames() throws FileNotFoundException {
        Optional<PackageInfo> assertFilePackage = Arrays.stream(
                compileResultForPrimitives.getProgFile().getPackageInfoEntries())
                .filter((packageInfo -> packageInfo.getPkgPath().equals(".")))
                .findFirst();
        
        if (assertFilePackage.isPresent()) {
            PackageInfo packageInfo = assertFilePackage.get();
            return Arrays.stream(packageInfo.getFunctionInfoEntries())
                    .filter((func) -> !func.getName().equals("..<init>"))
                    .map(functionInfo -> new Object[]{functionInfo})
                    .toArray(Object[][]::new);
            
        } else {
            throw new FileNotFoundException("Unable to find test file.");
        }
    }
    
    /**
     * Validating assertEquals functions.
     * @param testFunction The FunctionInfo object.
     */
    @Test(dataProvider = "DeepEqualsValidFunctionInfos")
    public void testPrimitivesDeepEqual(FunctionInfo testFunction) {
        BValue[] returns = BRunUtil.invoke(compileResultForPrimitives, testFunction.getName());
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        if (testFunction.getName().contains("Positive")) {
            Assert.assertTrue(actual, "Expected a TRUE for: " + testFunction.getName() + ". ");
        } else {
            Assert.assertFalse(actual, "Expected a FALSE for: " + testFunction.getName() + ". ");
        }
    }
    
    @Test
    public void testStructsDeepEqual() {
        BValue[] returns = BRunUtil.invoke(compileResultForComplex, "testStructsWithArrays");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
        
        returns = BRunUtil.invoke(compileResultForComplex, "testPrimitiveStructs");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
        
        returns = BRunUtil.invoke(compileResultForComplex, "testNestedStructs");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
    }
    
    @Test
    public void testArraysOfArraysDeepEqual() {
        BValue[] returns = BRunUtil.invoke(compileResultForComplex, "testArraysOfArrays");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
    }
    
    @Test
    public void testMapsDeepEqual() {
        BValue[] returns = BRunUtil.invoke(compileResultForComplex, "testMaps");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
    }
    
    @Test
    public void testAnyTypeDeepEqual() {
        BValue[] returns = BRunUtil.invoke(compileResultForComplex, "testAnyType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
    }
    
    @Test
    public void testJsonStringDeepEqual() {
        BValue[] returns = BRunUtil.invoke(compileResultForComplex, "testJSONString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
    }
    
    @Test
    public void testJsonIntDeepEqual() {
        BValue[] returns = BRunUtil.invoke(compileResultForComplex, "testJSONInt");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
    }
    
    @Test
    public void testJsonBooleanDeepEqual() {
        BValue[] returns = BRunUtil.invoke(compileResultForComplex, "testJSONBoolean");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
    }
    
    @Test
    public void testJsonNullDeepEqual() {
        BValue[] returns = BRunUtil.invoke(compileResultForComplex, "testJSONNull");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
    }
    
    @Test
    public void testJsonEmptyDeepEqual() {
        BValue[] returns = BRunUtil.invoke(compileResultForComplex, "testJSONEmpty");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
    }
    
    @Test
    public void testJsonObjectDeepEqual() {
        BValue[] returns = BRunUtil.invoke(compileResultForComplex, "testJSONObjects");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
    }
    
    @Test
    public void testJsonArrayDeepEqual() {
        BValue[] returns = BRunUtil.invoke(compileResultForComplex, "testJSONArray");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
    }
    
    @Test
    public void testJsonNestedDeepEqual() {
        BValue[] returns = BRunUtil.invoke(compileResultForComplex, "testJSONNested");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertTrue(actual, "Condition should give TRUE");
    }
}
