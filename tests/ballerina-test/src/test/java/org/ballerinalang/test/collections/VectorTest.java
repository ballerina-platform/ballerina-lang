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

package org.ballerinalang.test.collections;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test cases for Vectors
 */
public class VectorTest {

    private CompileResult compileResult;

    @BeforeTest
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/collections/vector-test.bal");
    }

    @Test(description = "Test case for testing addition of elements to a vector.")
    public void testAdd() {
        String[] args = new String[]{"Foo", "Bar", "Ballerina"};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAdd", new BValue[]{buildStringArray(args)});

        Assert.assertNotNull(returns);

        BStruct vector = (BStruct) returns[0];
        BRefValueArray vectorEntries = (BRefValueArray) vector.getRefField(0);
        long vectorSize = vector.getIntField(0);

        Assert.assertEquals(vectorSize, args.length);
//        Assert.assertEquals(vectorEntries.size(), args.length);

        for (int i = 0; i < args.length; i++) {
            Assert.assertEquals(vectorEntries.get(i).value(), args[i]);
        }
    }

    @Test(description = "Test case for testing retrieval of elements from a vector.")
    public void testGet() {
        long[] expectedVals = new long[]{10, 30, 50, 70};
        long vectorSize = 10;
        BValue[] returns = BRunUtil.invoke(compileResult, "testGet", new BValue[]{new BInteger(vectorSize)});

        Assert.assertNotNull(returns);

        for (int i = 0; i < expectedVals.length; i++) {
            Assert.assertEquals(((BInteger) returns[i]).intValue(), expectedVals[i]);
        }
    }

    @Test(description = "Test case for testing insertion of elements to a vector.")
    public void testInsert() {
        long[] values = new long[]{100, 110, 120};
        long[] indices = new long[]{3, 4, 5};
        int vectorSize = 10;
        BValue[] returns = BRunUtil.invoke(compileResult, "testInsert",
                                           new BValue[]{buildIntArray(values), buildIntArray(indices),
                                                   new BInteger(vectorSize)});

        Assert.assertNotNull(returns);

        BStruct vector = (BStruct) returns[0];
        BRefValueArray vectorEntries = (BRefValueArray) vector.getRefField(0);
        long finalVectorSize = vector.getIntField(0);

        Assert.assertEquals(finalVectorSize, vectorSize + values.length);
//        Assert.assertEquals(vectorEntries.size(), vectorSize + values.length);

        for (int i = 0; i < indices.length; i++) {
            Assert.assertEquals(vectorEntries.get(indices[i]).value(), values[i]);
        }
    }

    @Test(description = "Test case for testing removal of elements from a vector.")
    public void testRemove() {
        long[] expectedVals = new long[]{20, 30, 50, 60, 80, 90, 100};
        long[] removedVals = new long[]{10, 40, 70};
        long vectorSize = 10;
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemove", new BValue[]{new BInteger(vectorSize)});

        Assert.assertNotNull(returns);

        BStruct vector = (BStruct) returns[0];
        BRefValueArray vectorEntries = (BRefValueArray) vector.getRefField(0);
        long finalVectorSize = vector.getIntField(0);

        Assert.assertEquals(finalVectorSize, expectedVals.length);
//        Assert.assertEquals(vectorEntries.size(), expectedVals.length);

        for (int i = 0; i < removedVals.length; i++) {
            Assert.assertEquals(((BInteger) returns[i + 1]).intValue(), removedVals[i]);
        }

        for (int i = 0; i < finalVectorSize; i++) {
            Assert.assertEquals(vectorEntries.get(i).value(), expectedVals[i]);
        }
    }

    private BStringArray buildStringArray(String[] args) {
        BStringArray valueArray = new BStringArray();

        for (int i = 0; i < args.length; i++) {
            valueArray.add(i, args[i]);
        }

        return valueArray;
    }

    private BIntArray buildIntArray(long[] args) {
        BIntArray valueArray = new BIntArray();

        for (int i = 0; i < args.length; i++) {
            valueArray.add(i, args[i]);
        }

        return valueArray;
    }
}
