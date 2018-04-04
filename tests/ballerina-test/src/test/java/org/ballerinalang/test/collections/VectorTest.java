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
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for Vectors.
 */
public class VectorTest {

    private CompileResult compileResult;

    @BeforeClass
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
        long[] values = new long[]{0, 100, 110, 120, 130};
        long[] indices = new long[]{0, 3, 4, 5, 13};
        int vectorSize = 10;
        BValue[] returns = BRunUtil.invoke(compileResult, "testInsert",
                                           new BValue[]{buildIntArray(values), buildIntArray(indices),
                                                   new BInteger(vectorSize)});

        Assert.assertNotNull(returns);

        BStruct vector = (BStruct) returns[0];
        BRefValueArray vectorEntries = (BRefValueArray) vector.getRefField(0);
        long finalVectorSize = vector.getIntField(0);

        Assert.assertEquals(finalVectorSize, vectorSize + values.length);

        for (int i = 0; i < indices.length; i++) {
            Assert.assertEquals(vectorEntries.get(indices[i]).value(), values[i]);
        }
    }

    @Test(description = "Test case for testing removal of elements from a vector.")
    public void testRemove() {
        long[] expectedVals = new long[]{20, 30, 50, 60, 70, 80, 90};
        long[] removedVals = new long[]{10, 40, 100};
        long vectorSize = 10;
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemove", new BValue[]{new BInteger(vectorSize)});

        Assert.assertNotNull(returns);

        BStruct vector = (BStruct) returns[0];
        BRefValueArray vectorEntries = (BRefValueArray) vector.getRefField(0);
        long finalVectorSize = vector.getIntField(0);

        Assert.assertEquals(finalVectorSize, expectedVals.length);

        for (int i = 0; i < removedVals.length; i++) {
            Assert.assertEquals(((BInteger) returns[i + 1]).intValue(), removedVals[i]);
        }

        for (int i = 0; i < finalVectorSize; i++) {
            Assert.assertEquals(vectorEntries.get(i).value(), expectedVals[i]);
        }

        for (int i = (int) finalVectorSize; i < vectorEntries.size(); i++) {
            Assert.assertNull(vectorEntries.get(i));
        }
    }

    @Test(description = "Test case for testing clearing of the vector.")
    public void testClear() {
        long initialSize = 10;
        BValue[] returns = BRunUtil.invoke(compileResult, "testClear", new BValue[]{new BInteger(initialSize)});

        Assert.assertNotNull(returns);

        BStruct vector = (BStruct) returns[0];
        BRefValueArray vectorEntries = (BRefValueArray) vector.getRefField(0);
        long vectorSize = vector.getIntField(0);

        Assert.assertEquals(vectorSize, 0);
        // Since the array is reinitialized in clear(), this should be 0
        Assert.assertEquals(vectorEntries.size(), 0);

        BStruct vectorRef = (BStruct) returns[1];
        BRefValueArray vectorEntriesRef = (BRefValueArray) vectorRef.getRefField(0);
        long vectorRefSize = vectorRef.getIntField(0);

        Assert.assertEquals(vectorRefSize, 0);
        // Since the second return value is a reference to the original vector, this should also be empty
        Assert.assertEquals(vectorEntriesRef.size(), 0);
    }

    @Test(description = "Test case for testing replacement of elements in a vector.")
    public void testReplace() {
        long[] values = new long[]{100, 110, 120};
        long[] indices = new long[]{3, 4, 9};
        int vectorSize = 10;
        long[] expectedFinalValues = new long[]{10, 20, 30, 100, 110, 60, 70, 80, 90, 120};

        BValue[] returns = BRunUtil.invoke(compileResult, "testReplace",
                                           new BValue[]{buildIntArray(values), buildIntArray(indices),
                                                   new BInteger(vectorSize)});

        Assert.assertNotNull(returns);

        BStruct vector = (BStruct) returns[0];
        BRefValueArray vectorEntries = (BRefValueArray) vector.getRefField(0);
        long finalVectorSize = vector.getIntField(0);

        Assert.assertEquals(finalVectorSize, vectorSize);

        for (int i = 0; i < expectedFinalValues.length; i++) {
            Assert.assertEquals(vectorEntries.get(i).value(), expectedFinalValues[i]);
        }

        BRefValueArray replacedVals = (BRefValueArray) returns[1];

        Assert.assertEquals(replacedVals.size(), values.length);
        Assert.assertEquals(((BInteger) replacedVals.get(0)).intValue(), 40);
        Assert.assertEquals(((BInteger) replacedVals.get(1)).intValue(), 50);
        Assert.assertEquals(((BInteger) replacedVals.get(2)).intValue(), 100);
    }

    @Test(description = "Test case for testing size() function")
    public void testSize() {
        long[] addElems = new long[]{11, 12, 13, 14, 15};
        long[] insertElems = new long[]{21, 23, 25};
        long[] replaceElems = new long[]{32, 34, 36, 38};
        long nRemoveElems = 9;
        long vectorSize = 10;

        BValue[] returns = BRunUtil.invoke(compileResult, "testSize",
                                           new BValue[]{buildIntArray(addElems), buildIntArray(insertElems),
                                                   buildIntArray(replaceElems), new BInteger(nRemoveElems),
                                                   new BInteger(vectorSize)});

        Assert.assertNotNull(returns);

        BIntArray vecSizes = (BIntArray) returns[0];

        Assert.assertEquals(vecSizes.get(0), (vectorSize += addElems.length));
        Assert.assertEquals(vecSizes.get(1), (vectorSize += insertElems.length));
        Assert.assertEquals(vecSizes.get(2), vectorSize);
        Assert.assertEquals(vecSizes.get(3), (vectorSize -= nRemoveElems));

    }

    @Test(description = "Test case for testing isEmpty() function")
    public void testIsEmpty() {
        final int booleanTrue = 1;
        int vectorSize = 10;
        boolean[] expectedVals = new boolean[]{false, true, true};

        BValue[] returns = BRunUtil.invoke(compileResult, "testIsEmpty", new BValue[]{new BInteger(vectorSize)});

        Assert.assertNotNull(returns);

        BBooleanArray isEmptyVals = (BBooleanArray) returns[0];

        for (int i = 0; i < expectedVals.length; i++) {
            Assert.assertEquals(isEmptyVals.get(i) == booleanTrue, expectedVals[i]);
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
