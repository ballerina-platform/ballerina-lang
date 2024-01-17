/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.typedesc;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains typedesc type negative tests.
 */
public class TypedescNegativeRuntimeTests {

    private CompileResult negativeRuntimeResult;

    @BeforeClass
    public void setup() {
        negativeRuntimeResult = BCompileUtil.compile("test-src/types/typedesc/typedesc_negative_runtime.bal");
    }

    @Test
    public void testJsonTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "jsonTypeDesc");
    }

    @Test
    public void mapTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "mapTypeDesc");
    }

    @Test
    public void tableTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "tableTypeDesc");
    }

    @Test
    public void mapJsonTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "mapJsonTypeDesc");
    }

    @Test
    public void stringTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "stringTypeDesc");
    }

    @Test
    public void intTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "intTypeDesc");
    }

    @Test
    public void floatTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "floatTypeDesc");
    }

    @Test
    public void booleanTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "booleanTypeDesc");
    }

    @Test
    public void decimalTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "decimalTypeDesc");
    }

    @Test
    public void xmlTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "xmlTypeDesc");
    }

    @Test
    public void bytesTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "bytesTypeDesc");
    }

    @Test
    public void anydataTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "anydataTypeDesc");
    }

    @Test
    public void anyTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "anyTypeDesc");
    }

    @Test
    public void nullTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "nullTypeDesc");
    }

    @Test
    public void objectTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "objectTypeDesc");
    }

    @Test
    public void arrayTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "arrayTypeDesc");
    }

    @Test
    public void unionTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "unionTypeDesc");
    }

    @Test
    public void intersectionTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "intersectionTypeDesc");
    }

    @Test
    public void tupleTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "tupleTypeDesc");
    }

    @Test
    public void singletonTypeDesc() {
        BRunUtil.invoke(negativeRuntimeResult, "singletonTypeDesc");
    }
    
    @Test
    public void jsonTypeDescCloneWithType() {
        BRunUtil.invoke(negativeRuntimeResult, "jsonTypeDescCloneWithType");
    }
    
    @Test
    public void jsonTypeDescFromJsonWithType() {
        BRunUtil.invoke(negativeRuntimeResult, "jsonTypeDescFromJsonWithType");
    }

    @AfterClass
    public void tearDown() {
        negativeRuntimeResult = null;
    }
}
