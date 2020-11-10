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

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static java.util.Arrays.stream;

/**
 * Test cases for record iteration and iterable operations on closed records.
 *
 * @since 0.982.0
 */
public class ClosedRecordIterationTest {

    private CompileResult result, closedRecNegatives;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/record/closed_record_iteration.bal");
        closedRecNegatives = BCompileUtil.compile("test-src/record/closed_record_iteration_negative.bal");
    }

    @Test
    public void testNegativesWithClosedRecords() {
        int index = 0;

        // Test invalid no. of args with foreach loop
        BAssertUtil.validateError(closedRecNegatives, index++,
                "invalid tuple binding pattern: expected a tuple type, but found '(string|int|ClosedAddress)'",
                40, 17);
        // Test invalid foreach iterable operation
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected '[string,any]', found '(string|int|ClosedAddress)'",
                47, 15);

        // Test invalid map iterable operation
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected '[string,any]', found '(string|int|ClosedAddress)'",
                52, 28);
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected '[string,any]', found '(string|int|ClosedAddress)'",
                56, 19);
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected '[string,any]', found '(string|int|ClosedAddress)'",
                60, 19);
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected 'ClosedPerson', found 'map<[string,any]>'",
                64, 27);
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected '[string,any]', found '(string|int|ClosedAddress)'",
                64, 34);

        // Test invalid filter iterable operation
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected '[string,any]', found '(string|int|ClosedAddress)'",
                70, 30);
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected 'function ((any|error)) returns (boolean)', found " +
                        "'function ([string,any]) returns (string)'", 74, 21);
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected 'ClosedPerson', " +
                        "found 'map<(string|int|ClosedAddress)>'", 78, 27);
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected '[string,any]', found '(string|int|ClosedAddress)'", 78,
                36);

        // Test mismatching chained iterable op return values
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected 'map<int>', found 'map<[string,float]>'",
                86, 18);
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected '[string,int]', found 'int'",
                86, 25);
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected 'int[]', found 'map<float>'",
                116, 16);
        BAssertUtil.validateError(closedRecNegatives, index++,
                "incompatible types: expected '[string,int]', found 'int'",
                116, 23);

        Assert.assertEquals(closedRecNegatives.getErrorCount(), index);
    }

    @Test
    public void testForeachWithClosedRecords() {
        String[] expectedFields = new String[]{"name", "age", "address"};
        BValue[] returns = BRunUtil.invoke(result, "testForeachWithClosedRecords");

        BValueArray fields = (BValueArray) returns[0];
        for (int i = 0; i < fields.size(); i++) {
            Assert.assertEquals(fields.getString(i), expectedFields[i]);
        }

        BValueArray values = (BValueArray) returns[1];
        Assert.assertEquals(values.getRefValue(0).stringValue(), "John Doe");
        Assert.assertEquals(((BInteger) values.getRefValue(1)).intValue(), 25);
        Assert.assertTrue(values.getRefValue(2) instanceof BMap);

        BMap addressRecord = (BMap) values.getRefValue(2);
        Assert.assertEquals(addressRecord.get("street").stringValue(), "Palm Grove");
        Assert.assertEquals(addressRecord.get("city").stringValue(), "Colombo 3");
    }

    @Test
    public void testForeachWithOpenRecords2() {
        BValue[] returns = BRunUtil.invoke(result, "testForeachWithOpenRecords2");

        Assert.assertEquals(returns[0].stringValue(), "John Doe");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
        Assert.assertTrue(returns[2] instanceof BMap);

        BMap addressRecord = (BMap) returns[2];
        Assert.assertEquals(addressRecord.get("street").stringValue(), "Palm Grove");
        Assert.assertEquals(addressRecord.get("city").stringValue(), "Colombo 3");
    }

    @Test(description = "Tests foreach iterable operation on closed records")
    public void testForeachOpWithClosedRecords() {
        BValue[] returns = BRunUtil.invoke(result, "testForeachOpWithClosedRecords");

        Assert.assertEquals(returns[0].stringValue(), "John Doe");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
        Assert.assertTrue(returns[2] instanceof BMap);

        BMap addressRecord = (BMap) returns[2];
        Assert.assertEquals(addressRecord.get("street").stringValue(), "Palm Grove");
        Assert.assertEquals(addressRecord.get("city").stringValue(), "Colombo 3");
    }

    @Test(description = "Tests map iterable operation on closed records")
    public void testMapOpWithClosedRecords() {
        String[] expectedFields = new String[]{"name", "age", "address"};
        BValue[] returns = BRunUtil.invoke(result, "testMapOpWithClosedRecords");

        BMap person = (BMap) returns[0];
        Assert.assertEquals(person.get(expectedFields[0]).stringValue(), "john doe");
        Assert.assertEquals(((BInteger) person.get(expectedFields[1])).intValue(), 25);
        Assert.assertTrue(person.get(expectedFields[2]) instanceof BMap);

        BMap addressRecord = (BMap) person.get(expectedFields[2]);
        Assert.assertEquals(addressRecord.get("street").stringValue(), "Palm Grove");
        Assert.assertEquals(addressRecord.get("city").stringValue(), "Colombo 3");
    }

    @Test(description = "Tests filter iterable operation on closed records")
    public void testFilterOpWithClosedRecords() {
        String[] expectedFields = new String[]{"a", "b", "c", "d", "e"};
        BValue[] returns = BRunUtil.invoke(result, "testFilterOpWithClosedRecords");

        BMap person = (BMap) returns[0];
        Assert.assertEquals(person.get(expectedFields[0]).stringValue(), "A");
        Assert.assertNull(person.get(expectedFields[1]));
        Assert.assertNull(person.get(expectedFields[2]));
        Assert.assertNull(person.get(expectedFields[3]));
        Assert.assertEquals(person.get(expectedFields[4]).stringValue(), "E");
    }

    @Test(description = "Tests reduce iterable operation on closed records")
    public void testReduceOpWithClosedRecords() {
        BValue[] returns = BRunUtil.invoke(result, "testReduceOpWithClosedRecords");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test(description = "Test case for chained iterable operations on closed records")
    public void testChainedOpsWithClosedRecords() {
        BValue[] returns = BRunUtil.invoke(result, "testChainedOpsWithClosedRecords");
        BMap foo = (BMap) returns[0];

        Assert.assertNull(foo.get("a"));
        Assert.assertEquals(foo.get("b").stringValue(), "bb");
        Assert.assertEquals(foo.get("c").stringValue(), "cc");
        Assert.assertEquals(foo.get("d").stringValue(), "dd");
        Assert.assertNull(foo.get("e"));
    }

    @Test(description = "Test case for map op on closed records with all string fields")
    public void testMapWithAllStringClosedRecord() {
        BValue[] returns = BRunUtil.invoke(result, "testMapWithAllStringClosedRecord");

        BMap foo = (BMap) returns[0];
        Assert.assertEquals(foo.get("a").stringValue(), "aa");
        Assert.assertEquals(foo.get("b").stringValue(), "bb");
        Assert.assertEquals(foo.get("c").stringValue(), "cc");
        Assert.assertEquals(foo.get("d").stringValue(), "dd");
        Assert.assertEquals(foo.get("e").stringValue(), "ee");
    }

    @Test(description = "Test case for map op on closed records with all int fields")
    public void testMapWithAllIntClosedRecord() {
        BValue[] args = new BValue[]{new BInteger(80), new BInteger(75), new BInteger(65)};
        BValue[] returns = BRunUtil.invoke(result, "testMapWithAllIntClosedRecord", args);

        BMap gradesMap = (BMap) returns[0];
        Assert.assertEquals(((BInteger) gradesMap.get("maths")).intValue(), 90);
        Assert.assertEquals(((BInteger) gradesMap.get("physics")).intValue(), 85);
        Assert.assertEquals(((BInteger) gradesMap.get("chemistry")).intValue(), 75);
    }

    @Test(description = "Test case for map op on closed records with all float fields")
    public void testMapWithAllFloatClosedRecord() {
        final double a = 20.15, b = 7.89, c = 5.15;
        BValue[] args = new BValue[]{new BFloat(a), new BFloat(b), new BFloat(c)};
        BValue[] returns = BRunUtil.invoke(result, "testMapWithAllFloatClosedRecord", args);

        BMap gradesMap = (BMap) returns[0];
        Assert.assertEquals(((BFloat) gradesMap.get("x")).floatValue(), a + 10);
        Assert.assertEquals(((BFloat) gradesMap.get("y")).floatValue(), b + 10);
        Assert.assertEquals(((BFloat) gradesMap.get("z")).floatValue(), c + 10);
    }

    @Test(description = "Test case for filter op on closed records with all string fields")
    public void testFilterWithAllStringClosedRecord() {
        final String a = "AA", e = "EE";
        BValue[] returns = BRunUtil.invoke(result, "testFilterWithAllStringClosedRecord");

        BMap foo = (BMap) returns[0];
        Assert.assertEquals(foo.get("a").stringValue(), a);
        Assert.assertNull(foo.get("b"));
        Assert.assertNull(foo.get("c"));
        Assert.assertNull(foo.get("d"));
        Assert.assertEquals(foo.get("e").stringValue(), e);
    }

    @Test(description = "Test case for filter op on closed records with all int fields")
    public void testFilterWithAllIntClosedRecord() {
        final long m = 80, p = 75;
        BValue[] returns = BRunUtil.invoke(result, "testFilterWithAllIntClosedRecord");

        BMap gradesMap = (BMap) returns[0];
        Assert.assertEquals(((BInteger) gradesMap.get("maths")).intValue(), m);
        Assert.assertEquals(((BInteger) gradesMap.get("physics")).intValue(), p);
        Assert.assertNull(gradesMap.get("chemistry"));
    }

    @Test(description = "Test case for map op on closed records with all float fields")
    public void testFilterWithAllFloatClosedRecord() {
        final double a = 20.15, b = 7.89, c = 5.15;
        BValue[] args = new BValue[]{new BFloat(a), new BFloat(b), new BFloat(c)};
        BValue[] returns = BRunUtil.invoke(result, "testFilterWithAllFloatClosedRecord", args);

        BMap gradesMap = (BMap) returns[0];
        Assert.assertEquals(((BFloat) gradesMap.get("x")).floatValue(), a);
        Assert.assertEquals(((BFloat) gradesMap.get("y")).floatValue(), b);
        Assert.assertNull(gradesMap.get("z"));
    }

    @Test(description = "Test case for terminal ops on closed records with all int fields")
    public void testTerminalOpsOnAllIntClosedRecord() {
        long[] marks = new long[]{80, 76, 66};
        BValue[] args = new BValue[]
                {new BInteger(marks[0]), new BInteger(marks[1]), new BInteger(marks[2])};
        BValue[] returns = BRunUtil.invoke(result, "testTerminalOpsOnAllIntClosedRecord", args);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), stream(marks).count());
        Assert.assertEquals(((BInteger) returns[1]).intValue(), stream(marks).max().getAsLong());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), stream(marks).min().getAsLong());
        Assert.assertEquals(((BInteger) returns[3]).intValue(), stream(marks).sum());
        Assert.assertEquals(((BFloat) returns[4]).floatValue(), stream(marks).average().getAsDouble());
    }

    @Test(description = "Test case for terminal ops on closed records with all int fields")
    public void testTerminalOpsOnAllIntClosedRecord2() {
        long[] marks = new long[]{80, 76, 0};
        BValue[] args = new BValue[]
                {new BInteger(marks[0]), new BInteger(marks[1])};
        BValue[] returns = BRunUtil.invoke(result, "testTerminalOpsOnAllIntClosedRecord2", args);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), stream(marks).count());
        Assert.assertEquals(((BInteger) returns[1]).intValue(), stream(marks).max().getAsLong());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), stream(marks).min().getAsLong());
        Assert.assertEquals(((BInteger) returns[3]).intValue(), stream(marks).sum());
        Assert.assertEquals(((BFloat) returns[4]).floatValue(), stream(marks).average().getAsDouble());
    }

    @Test(description = "Test case for chained iterable operations on closed records")
    public void testChainedOpsWithClosedRecords2() {
        BValue[] returns = BRunUtil.invoke(result, "testChainedOpsWithClosedRecords2");
        BMap grades = (BMap) returns[0];

        Assert.assertEquals(((BFloat) grades.get("maths")).floatValue(), 4.2);
        Assert.assertEquals(((BFloat) grades.get("physics")).floatValue(), 4.2);
        Assert.assertNull(grades.get("chemistry"));
    }

    @Test(description = "Test case for iterable op chains ending with a terminal op")
    public void testOpChainsWithTerminalOps() {
        long[] grades = new long[]{80, 75};
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
        Assert.assertEquals(grades.size(), 3);
        Assert.assertEquals(((BInteger) grades.get("maths")).intValue(), 80);
        Assert.assertEquals(((BInteger) grades.get("physics")).intValue(), 75);
        Assert.assertEquals(((BInteger) grades.get("chemistry")).intValue(), 65);
    }
}
