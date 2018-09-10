/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.record;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static java.util.Arrays.stream;

/**
 * Test cases for record iteration and iterable operations on open records.
 *
 * @since 0.982.0
 */
public class OpenRecordIterationTest {

    private CompileResult result, openRecNegatives;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/record/open_record_iteration.bal");
        openRecNegatives = BCompileUtil.compile("test-src/record/open_record_iteration_negative.bal");
    }

    @Test
    public void testNegativesWithOpenRecords() {
        int index = 0;
        Assert.assertEquals(openRecNegatives.getErrorCount(), 16);
        BAssertUtil.validateError(openRecNegatives, index++, "operation 'sum' does not support given collection type",
                                  15, 15);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "operation 'average' does not support given collection type",
                                  19, 15);
        BAssertUtil.validateError(openRecNegatives, index++, "operation 'max' does not support given collection type",
                                  23, 15);
        BAssertUtil.validateError(openRecNegatives, index++, "operation 'min' does not support given collection type",
                                  27, 15);

        // Test invalid no. of args with foreach loop
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "too many variables are defined for iterable type 'Person'", 34, 26);

        // Test invalid foreach iterable operation
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "too many variables are defined for iterable type 'Person'",
                                  44, 15);

        // Test invalid map iterable operation
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "too many variables are defined for iterable type 'Person'",
                                  53, 18);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "incompatible types: expected 'map', found '(any) collection'", 57, 18);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "incompatible types: expected 'map', found '(string,any,string) collection'",
                                  61, 18);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "incompatible types: expected 'Person', found '(string,any) collection'",
                                  65, 27);

        // Test invalid filter iterable operation
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "too many variables are defined for iterable type 'Person'",
                                  75, 21);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "incompatible lambda function types: expected 'boolean', found 'string'", 79, 21);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "too many return arguments are defined for operation 'filter'",
                                  83, 21);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "incompatible types: expected 'Person', found '(string,any) collection'",
                                  87, 30);

        // Test mismatching chained iterable op return values
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "incompatible types: expected 'map<int>', found 'map<float>'",
                                  122, 10);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "incompatible types: expected 'int[]', found 'float[]'",
                                  149, 10);
    }

    @Test
    public void testForeachWithOpenRecords() {
        String[] expectedFields = new String[]{"name", "age", "address"};
        BValue[] returns = BRunUtil.invoke(result, "testForeachWithOpenRecords");

        BStringArray fields = (BStringArray) returns[0];
        for (int i = 0; i < fields.size(); i++) {
            Assert.assertEquals(fields.get(i), expectedFields[i]);
        }

        BRefValueArray values = (BRefValueArray) returns[1];
        Assert.assertEquals(values.get(0).stringValue(), "John Doe");
        Assert.assertEquals(((BInteger) values.get(1)).intValue(), 25);
        Assert.assertTrue(values.get(2) instanceof BMap);

        BMap addressRecord = (BMap) values.get(2);
        Assert.assertEquals(addressRecord.get("street").stringValue(), "Palm Grove");
        Assert.assertEquals(addressRecord.get("city").stringValue(), "Colombo 3");
    }

    @Test
    public void testForeachWithOpenRecords2() {
        String[] expectedFields = new String[]{"name", "age", "address", "height"};
        BValue[] returns = BRunUtil.invoke(result, "testForeachWithOpenRecords2");

        BStringArray fields = (BStringArray) returns[0];
        for (int i = 0; i < fields.size(); i++) {
            Assert.assertEquals(fields.get(i), expectedFields[i]);
        }

        BRefValueArray values = (BRefValueArray) returns[1];
        Assert.assertEquals(values.get(0).stringValue(), "John Doe");
        Assert.assertEquals(((BInteger) values.get(1)).intValue(), 25);
        Assert.assertTrue(values.get(2) instanceof BMap);
        Assert.assertEquals(((BFloat) values.get(3)).floatValue(), 5.9);

        BMap addressRecord = (BMap) values.get(2);
        Assert.assertEquals(addressRecord.get("street").stringValue(), "Palm Grove");
        Assert.assertEquals(addressRecord.get("city").stringValue(), "Colombo 3");
    }

    @Test
    public void testForeachWithOpenRecords3() {
        BValue[] returns = BRunUtil.invoke(result, "testForeachWithOpenRecords3");

        Assert.assertEquals(returns[0].stringValue(), "John Doe");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
        Assert.assertTrue(returns[2] instanceof BMap);

        BMap addressRecord = (BMap) returns[2];
        Assert.assertEquals(addressRecord.get("street").stringValue(), "Palm Grove");
        Assert.assertEquals(addressRecord.get("city").stringValue(), "Colombo 3");
    }

    // disabled due to https://github.com/ballerina-platform/ballerina-lang/issues/10074
    @Test(description = "Tests foreach iterable operation on open records", enabled = false)
    public void testForeachOpWithOpenRecords() {
        String[] expectedFields = new String[]{"name", "age", "address"};
        BValue[] returns = BRunUtil.invoke(result, "testForeachOpWithOpenRecords");

        BMap rec2map = (BMap) returns[0];
        Assert.assertEquals(rec2map.get(expectedFields[0]).stringValue(), "John Doe");
        Assert.assertEquals(((BInteger) rec2map.get(expectedFields[1])).intValue(), 25);
        Assert.assertTrue(rec2map.get(expectedFields[2]) instanceof BMap);
        Assert.assertEquals(((BFloat) rec2map.get(expectedFields[3])).floatValue(), 5.9);

        BMap addressRecord = (BMap) rec2map.get(expectedFields[2]);
        Assert.assertEquals(addressRecord.get("street").stringValue(), "Palm Grove");
        Assert.assertEquals(addressRecord.get("city").stringValue(), "Colombo 3");
    }

    @Test(description = "Tests map iterable operation on open records")
    public void testMapOpWithOpenRecords() {
        String[] expectedFields = new String[]{"name", "age", "address", "profession"};
        BValue[] returns = BRunUtil.invoke(result, "testMapOpWithOpenRecords");

        BMap person = (BMap) returns[0];
        Assert.assertEquals(person.get(expectedFields[0]).stringValue(), "john doe");
        Assert.assertEquals(((BInteger) person.get(expectedFields[1])).intValue(), 25);
        Assert.assertTrue(person.get(expectedFields[2]) instanceof BMap);
        Assert.assertEquals(person.get(expectedFields[3]).stringValue(), "software engineer");

        BMap addressRecord = (BMap) person.get(expectedFields[2]);
        Assert.assertEquals(addressRecord.get("street").stringValue(), "Palm Grove");
        Assert.assertEquals(addressRecord.get("city").stringValue(), "Colombo 3");
    }

    @Test(description = "Tests filter iterable operation on open records")
    public void testFilterOpWithOpenRecords() {
        BValue[] returns = BRunUtil.invoke(result, "testFilterOpWithOpenRecords");
        BMap foo = (BMap) returns[0];

        Assert.assertNull(foo.get("a"));
        Assert.assertEquals(foo.get("b").stringValue(), "B");
        Assert.assertEquals(foo.get("c").stringValue(), "C");
        Assert.assertEquals(foo.get("d").stringValue(), "D");
        Assert.assertNull(foo.get("e"));
        Assert.assertEquals(foo.get("f").stringValue(), "F");
    }

    @Test(description = "Tests count iterable operation on open records")
    public void testCountOpWithOpenRecords() {
        BValue[] returns = BRunUtil.invoke(result, "testCountOpWithOpenRecords");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 6);
    }

    @Test(description = "Test case for chained iterable operations on open records")
    public void testChainedOpsWithOpenRecords() {
        BValue[] returns = BRunUtil.invoke(result, "testChainedOpsWithOpenRecords");
        BMap foo = (BMap) returns[0];

        Assert.assertNull(foo.get("a"));
        Assert.assertEquals(foo.get("b").stringValue(), "bb");
        Assert.assertEquals(foo.get("c").stringValue(), "cc");
        Assert.assertEquals(foo.get("d").stringValue(), "dd");
        Assert.assertNull(foo.get("e"));
        Assert.assertEquals(foo.get("f").stringValue(), "ff");
    }

    @Test(description = "Test case for map op on open records with all int fields")
    public void testMapWithAllStringOpenRecord() {
        BValue[] returns = BRunUtil.invoke(result, "testMapWithAllStringOpenRecord");

        BMap foo = (BMap) returns[0];
        Assert.assertEquals(foo.get("a").stringValue(), "aa");
        Assert.assertEquals(foo.get("b").stringValue(), "bb");
        Assert.assertEquals(foo.get("c").stringValue(), "cc");
        Assert.assertEquals(foo.get("d").stringValue(), "dd");
        Assert.assertEquals(foo.get("e").stringValue(), "ee");

        BStringArray fooArr = (BStringArray) returns[1];
        Assert.assertEquals(fooArr.get(0), "aa");
        Assert.assertEquals(fooArr.get(1), "bb");
        Assert.assertEquals(fooArr.get(2), "cc");
        Assert.assertEquals(fooArr.get(3), "dd");
        Assert.assertEquals(fooArr.get(4), "ee");
    }

    @Test(description = "Test case for map op on open records with all int fields")
    public void testMapWithAllIntOpenRecord() {
        BValue[] args = new BValue[]{new BInteger(80), new BInteger(75), new BInteger(65), new BInteger(78)};
        BValue[] returns = BRunUtil.invoke(result, "testMapWithAllIntOpenRecord", args);

        BMap gradesMap = (BMap) returns[0];
        Assert.assertEquals(((BInteger) gradesMap.get("maths")).intValue(), 90);
        Assert.assertEquals(((BInteger) gradesMap.get("physics")).intValue(), 85);
        Assert.assertEquals(((BInteger) gradesMap.get("chemistry")).intValue(), 75);
        Assert.assertEquals(((BInteger) gradesMap.get("english")).intValue(), 88);

        BIntArray gradesArr = (BIntArray) returns[1];
        Assert.assertEquals(gradesArr.get(0), 90);
        Assert.assertEquals(gradesArr.get(1), 85);
        Assert.assertEquals(gradesArr.get(2), 75);
        Assert.assertEquals(gradesArr.get(3), 88);
    }

    @Test(description = "Test case for map op on open records with all float fields")
    public void testMapWithAllFloatOpenRecord() {
        final double a = 20.15, b = 7.89, c = 5.15, p = 9.9;
        BValue[] args = new BValue[]{new BFloat(a), new BFloat(b), new BFloat(c)};
        BValue[] returns = BRunUtil.invoke(result, "testMapWithAllFloatOpenRecord", args);

        BMap gradesMap = (BMap) returns[0];
        Assert.assertEquals(((BFloat) gradesMap.get("x")).floatValue(), a + 10);
        Assert.assertEquals(((BFloat) gradesMap.get("y")).floatValue(), b + 10);
        Assert.assertEquals(((BFloat) gradesMap.get("z")).floatValue(), c + 10);
        Assert.assertEquals(((BFloat) gradesMap.get("p")).floatValue(), p + 10);

        BFloatArray gradesArr = (BFloatArray) returns[1];
        Assert.assertEquals(gradesArr.get(0), a + 10);
        Assert.assertEquals(gradesArr.get(1), b + 10);
        Assert.assertEquals(gradesArr.get(2), c + 10);
        Assert.assertEquals(gradesArr.get(3), p + 10);
    }

    @Test(description = "Test case for filter op on open records with all string fields")
    public void testFilterWithAllStringOpenRecord() {
        final String a = "AA", e = "EE", f = "FF";
        BValue[] returns = BRunUtil.invoke(result, "testFilterWithAllStringOpenRecord");

        BMap foo = (BMap) returns[0];
        Assert.assertEquals(foo.get("a").stringValue(), a);
        Assert.assertNull(foo.get("b"));
        Assert.assertNull(foo.get("c"));
        Assert.assertNull(foo.get("d"));
        Assert.assertEquals(foo.get("e").stringValue(), e);
        Assert.assertEquals(foo.get("f").stringValue(), f);

        BStringArray fooArr = (BStringArray) returns[1];
        Assert.assertEquals(fooArr.size(), 3);
        Assert.assertEquals(fooArr.get(0), a);
        Assert.assertEquals(fooArr.get(1), e);
        Assert.assertEquals(fooArr.get(2), f);
    }

    @Test(description = "Test case for filter op on open records with all int fields")
    public void testFilterWithAllIntOpenRecord() {
        final long m = 80, p = 75, e = 78;
        BValue[] returns = BRunUtil.invoke(result, "testFilterWithAllIntOpenRecord");

        BMap gradesMap = (BMap) returns[0];
        Assert.assertEquals(((BInteger) gradesMap.get("maths")).intValue(), m);
        Assert.assertEquals(((BInteger) gradesMap.get("physics")).intValue(), p);
        Assert.assertNull(gradesMap.get("chemistry"));
        Assert.assertEquals(((BInteger) gradesMap.get("english")).intValue(), e);

        BIntArray fooArr = (BIntArray) returns[1];
        Assert.assertEquals(fooArr.size(), 3);
        Assert.assertEquals(fooArr.get(0), m);
        Assert.assertEquals(fooArr.get(1), p);
        Assert.assertEquals(fooArr.get(2), e);
    }

    @Test(description = "Test case for map op on open records with all float fields")
    public void testFilterWithAllFloatOpenRecord() {
        final double a = 20.15, b = 7.89, c = 5.15, p = 9.9;
        BValue[] args = new BValue[]{new BFloat(a), new BFloat(b), new BFloat(c)};
        BValue[] returns = BRunUtil.invoke(result, "testFilterWithAllFloatOpenRecord", args);

        BMap gradesMap = (BMap) returns[0];
        Assert.assertEquals(((BFloat) gradesMap.get("x")).floatValue(), a);
        Assert.assertEquals(((BFloat) gradesMap.get("y")).floatValue(), b);
        Assert.assertNull(gradesMap.get("z"));
        Assert.assertEquals(((BFloat) gradesMap.get("p")).floatValue(), p);

        BFloatArray gradesArr = (BFloatArray) returns[1];
        Assert.assertEquals(gradesArr.size(), 3);
        Assert.assertEquals(gradesArr.get(0), a);
        Assert.assertEquals(gradesArr.get(1), b);
        Assert.assertEquals(gradesArr.get(2), p);
    }

    @Test(description = "Test case for terminal ops on open records with all int fields")
    public void testTerminalOpsOnAllIntOpenRecord() {
        long[] marks = new long[]{80, 75, 65, 78};
        BValue[] args = new BValue[]
                {new BInteger(marks[0]), new BInteger(marks[1]), new BInteger(marks[2]), new BInteger(marks[3])};
        BValue[] returns = BRunUtil.invoke(result, "testTerminalOpsOnAllIntOpenRecord", args);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), stream(marks).count());
        Assert.assertEquals(((BInteger) returns[1]).intValue(), stream(marks).max().getAsLong());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), stream(marks).min().getAsLong());
        Assert.assertEquals(((BInteger) returns[3]).intValue(), stream(marks).sum());
        Assert.assertEquals(((BFloat) returns[4]).floatValue(), stream(marks).average().getAsDouble());
    }

    @Test(description = "Test case for chained iterable operations on open records")
    public void testChainedOpsWithOpenRecords2() {
        BValue[] returns = BRunUtil.invoke(result, "testChainedOpsWithOpenRecords2");
        BMap grades = (BMap) returns[0];

        Assert.assertEquals(((BFloat) grades.get("maths")).floatValue(), 4.2);
        Assert.assertEquals(((BFloat) grades.get("physics")).floatValue(), 4.2);
        Assert.assertEquals(((BFloat) grades.get("english")).floatValue(), 4.2);
        Assert.assertNull(grades.get("chemistry"));
    }

    @Test(description = "Test case for chained iterable operations on open records")
    public void testChainedOpsWithOpenRecords3() {
        BValue[] returns = BRunUtil.invoke(result, "testChainedOpsWithOpenRecords3");
        BMap grades = (BMap) returns[0];

        Assert.assertEquals(((BFloat) grades.get("maths")).floatValue(), 4.2);
        Assert.assertEquals(((BFloat) grades.get("physics")).floatValue(), 4.2);
        Assert.assertEquals(((BFloat) grades.get("english")).floatValue(), 4.2);
        Assert.assertNull(grades.get("chemistry"));
    }

    @Test(description = "Test case for terminal ops on open records with all int fields")
    public void testTerminalOpsOnAllIntOpenRecord2() {
        long[] marks = new long[]{80, 75, 0, 78};
        BValue[] args = new BValue[]
                {new BInteger(marks[0]), new BInteger(marks[1]), new BInteger(marks[3])};
        BValue[] returns = BRunUtil.invoke(result, "testTerminalOpsOnAllIntOpenRecord2", args);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), stream(marks).count());
        Assert.assertEquals(((BInteger) returns[1]).intValue(), stream(marks).max().getAsLong());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), stream(marks).min().getAsLong());
        Assert.assertEquals(((BInteger) returns[3]).intValue(), stream(marks).sum());
        Assert.assertEquals(((BFloat) returns[4]).floatValue(), stream(marks).average().getAsDouble());
    }

    @Test(description = "Test case for iterable op chains ending with a terminal op")
    public void testOpChainsWithTerminalOps() {
        long[] grades = new long[]{80, 75, 78};
        BValue[] returns = BRunUtil.invoke(result, "testOpChainsWithTerminalOps",
                                           new BValue[]{new BInteger(grades[0]), new BInteger(grades[1]),
                                                   new BInteger(65)});
        grades = stream(grades).map(g -> g + 10).toArray();
        Assert.assertEquals(((BInteger) returns[0]).intValue(), stream(grades).count());
        Assert.assertEquals(((BInteger) returns[1]).intValue(), stream(grades).sum());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), stream(grades).max().getAsLong());
        Assert.assertEquals(((BInteger) returns[3]).intValue(), stream(grades).min().getAsLong());
        Assert.assertEquals(((BFloat) returns[4]).floatValue(), stream(grades).average().getAsDouble());
    }

    @Test(description = "Test case for checking whether iterable ops mutate the original record")
    public void testMutability() {
        BValue[] returns = BRunUtil.invoke(result, "testMutability");
        BMap grades = (BMap) returns[0];
        Assert.assertEquals(grades.size(), 4);
        Assert.assertEquals(((BInteger) grades.get("maths")).intValue(), 80);
        Assert.assertEquals(((BInteger) grades.get("physics")).intValue(), 75);
        Assert.assertEquals(((BInteger) grades.get("chemistry")).intValue(), 65);
        Assert.assertEquals(((BInteger) grades.get("english")).intValue(), 78);
    }
}
