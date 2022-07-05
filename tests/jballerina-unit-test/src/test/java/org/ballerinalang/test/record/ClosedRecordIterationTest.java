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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
                "invalid list binding pattern: attempted to infer a list type, but found " +
                        "'(string|int|ClosedAddress)'",
                40, 13);
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
                "incompatible types: expected 'function (ballerina/lang.map:0.0.0:Type) returns (boolean)', " +
                        "found 'function ([string,any]) returns (string)'", 74, 21);
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
        BArray returns = (BArray) BRunUtil.invoke(result, "testForeachWithClosedRecords");

        BArray fields = (BArray) returns.get(0);
        for (int i = 0; i < fields.size(); i++) {
            Assert.assertEquals(fields.getString(i), expectedFields[i]);
        }

        BArray values = (BArray) returns.get(1);
        Assert.assertEquals(values.getRefValue(0).toString(), "John Doe");
        Assert.assertEquals(values.getRefValue(1), 25L);
        Assert.assertTrue(values.getRefValue(2) instanceof BMap);

        BMap addressRecord = (BMap) values.getRefValue(2);
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("street")).toString(), "Palm Grove");
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("city")).toString(), "Colombo 3");
    }

    @Test
    public void testForeachWithOpenRecords2() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testForeachWithOpenRecords2");

        Assert.assertEquals(returns.get(0).toString(), "John Doe");
        Assert.assertEquals(returns.get(1), 25L);
        Assert.assertTrue(returns.get(2) instanceof BMap);

        BMap addressRecord = (BMap) returns.get(2);
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("street")).toString(), "Palm Grove");
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("city")).toString(), "Colombo 3");
    }

    @Test(description = "Tests foreach iterable operation on closed records")
    public void testForeachOpWithClosedRecords() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testForeachOpWithClosedRecords");

        Assert.assertEquals(returns.get(0).toString(), "John Doe");
        Assert.assertEquals(returns.get(1), 25L);
        Assert.assertTrue(returns.get(2) instanceof BMap);

        BMap addressRecord = (BMap) returns.get(2);
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("street")).toString(), "Palm Grove");
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("city")).toString(), "Colombo 3");
    }

    @Test(description = "Tests map iterable operation on closed records")
    public void testMapOpWithClosedRecords() {
        String[] expectedFields = new String[]{"name", "age", "address"};
        Object returns = BRunUtil.invoke(result, "testMapOpWithClosedRecords");

        BMap person = (BMap) returns;
        Assert.assertEquals(person.get(StringUtils.fromString(expectedFields[0])).toString(), "john doe");
        Assert.assertEquals((person.get(StringUtils.fromString(expectedFields[1]))), 25L);
        Assert.assertTrue(person.get(StringUtils.fromString(expectedFields[2])) instanceof BMap);

        BMap addressRecord = (BMap) person.get(StringUtils.fromString(expectedFields[2]));
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("street")).toString(), "Palm Grove");
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("city")).toString(), "Colombo 3");
    }

    @Test(description = "Tests filter iterable operation on closed records")
    public void testFilterOpWithClosedRecords() {
        String[] expectedFields = new String[]{"a", "b", "c", "d", "e"};
        Object returns = BRunUtil.invoke(result, "testFilterOpWithClosedRecords");

        BMap person = (BMap) returns;
        Assert.assertEquals(person.get(StringUtils.fromString(expectedFields[0])).toString(), "A");
        Assert.assertNull(person.get(StringUtils.fromString(expectedFields[1])));
        Assert.assertNull(person.get(StringUtils.fromString(expectedFields[2])));
        Assert.assertNull(person.get(StringUtils.fromString(expectedFields[3])));
        Assert.assertEquals(person.get(StringUtils.fromString(expectedFields[4])).toString(), "E");
    }

    @Test(description = "Tests reduce iterable operation on closed records")
    public void testReduceOpWithClosedRecords() {
        Object returns = BRunUtil.invoke(result, "testReduceOpWithClosedRecords");
        Assert.assertEquals(returns, 5L);
    }

    @Test(description = "Test case for chained iterable operations on closed records")
    public void testChainedOpsWithClosedRecords() {
        Object returns = BRunUtil.invoke(result, "testChainedOpsWithClosedRecords");
        BMap foo = (BMap) returns;

        Assert.assertNull(foo.get(StringUtils.fromString("a")));
        Assert.assertEquals(foo.get(StringUtils.fromString("b")).toString(), "bb");
        Assert.assertEquals(foo.get(StringUtils.fromString("c")).toString(), "cc");
        Assert.assertEquals(foo.get(StringUtils.fromString("d")).toString(), "dd");
        Assert.assertNull(foo.get(StringUtils.fromString("e")));
    }

    @Test(description = "Test case for map op on closed records with all string fields")
    public void testMapWithAllStringClosedRecord() {
        Object returns = BRunUtil.invoke(result, "testMapWithAllStringClosedRecord");

        BMap foo = (BMap) returns;
        Assert.assertEquals(foo.get(StringUtils.fromString("a")).toString(), "aa");
        Assert.assertEquals(foo.get(StringUtils.fromString("b")).toString(), "bb");
        Assert.assertEquals(foo.get(StringUtils.fromString("c")).toString(), "cc");
        Assert.assertEquals(foo.get(StringUtils.fromString("d")).toString(), "dd");
        Assert.assertEquals(foo.get(StringUtils.fromString("e")).toString(), "ee");
    }

    @Test(description = "Test case for map op on closed records with all int fields")
    public void testMapWithAllIntClosedRecord() {
        Object[] args = new Object[]{(80), (75), (65)};
        Object returns = BRunUtil.invoke(result, "testMapWithAllIntClosedRecord", args);

        BMap gradesMap = (BMap) returns;
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("maths"))), 90L);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("physics"))), 85L);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("chemistry"))), 75L);
    }

    @Test(description = "Test case for map op on closed records with all float fields")
    public void testMapWithAllFloatClosedRecord() {
        final double a = 20.15, b = 7.89, c = 5.15;
        Object[] args = new Object[]{(a), (b), (c)};
        Object returns = BRunUtil.invoke(result, "testMapWithAllFloatClosedRecord", args);

        BMap gradesMap = (BMap) returns;
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("x"))), a + 10);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("y"))), b + 10);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("z"))), c + 10);
    }

    @Test(description = "Test case for filter op on closed records with all string fields")
    public void testFilterWithAllStringClosedRecord() {
        final String a = "AA", e = "EE";
        Object returns = BRunUtil.invoke(result, "testFilterWithAllStringClosedRecord");

        BMap foo = (BMap) returns;
        Assert.assertEquals(foo.get(StringUtils.fromString("a")).toString(), a);
        Assert.assertNull(foo.get(StringUtils.fromString("b")));
        Assert.assertNull(foo.get(StringUtils.fromString("c")));
        Assert.assertNull(foo.get(StringUtils.fromString("d")));
        Assert.assertEquals(foo.get(StringUtils.fromString("e")).toString(), e);
    }

    @Test(description = "Test case for filter op on closed records with all int fields")
    public void testFilterWithAllIntClosedRecord() {
        final long m = 80, p = 75;
        Object returns = BRunUtil.invoke(result, "testFilterWithAllIntClosedRecord");

        BMap gradesMap = (BMap) returns;
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("maths"))), m);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("physics"))), p);
        Assert.assertNull(gradesMap.get(StringUtils.fromString("chemistry")));
    }

    @Test(description = "Test case for map op on closed records with all float fields")
    public void testFilterWithAllFloatClosedRecord() {
        final double a = 20.15, b = 7.89, c = 5.15;
        Object[] args = new Object[]{(a), (b), (c)};
        Object returns = BRunUtil.invoke(result, "testFilterWithAllFloatClosedRecord", args);

        BMap gradesMap = (BMap) returns;
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("x"))), a);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("y"))), b);
        Assert.assertNull(gradesMap.get(StringUtils.fromString("z")));
    }

    @Test(description = "Test case for terminal ops on closed records with all int fields")
    public void testTerminalOpsOnAllIntClosedRecord() {
        long[] marks = new long[]{80, 76, 66};
        Object[] args = new Object[]
                {(marks[0]), (marks[1]), (marks[2])};
        BArray returns = (BArray) BRunUtil.invoke(result, "testTerminalOpsOnAllIntClosedRecord", args);

        Assert.assertEquals(returns.get(0), stream(marks).count());
        Assert.assertEquals(returns.get(1), stream(marks).max().getAsLong());
        Assert.assertEquals(returns.get(2), stream(marks).min().getAsLong());
        Assert.assertEquals(returns.get(3), stream(marks).sum());
        Assert.assertEquals(returns.get(4), stream(marks).average().getAsDouble());
    }

    @Test(description = "Test case for terminal ops on closed records with all int fields")
    public void testTerminalOpsOnAllIntClosedRecord2() {
        long[] marks = new long[]{80, 76, 0};
        Object[] args = new Object[]{(marks[0]), (marks[1])};
        BArray returns = (BArray) BRunUtil.invoke(result, "testTerminalOpsOnAllIntClosedRecord2", args);

        Assert.assertEquals(returns.get(0), stream(marks).count());
        Assert.assertEquals(returns.get(1), stream(marks).max().getAsLong());
        Assert.assertEquals(returns.get(2), stream(marks).min().getAsLong());
        Assert.assertEquals(returns.get(3), stream(marks).sum());
        Assert.assertEquals(returns.get(4), stream(marks).average().getAsDouble());
    }

    @Test(description = "Test case for chained iterable operations on closed records")
    public void testChainedOpsWithClosedRecords2() {
        Object returns = BRunUtil.invoke(result, "testChainedOpsWithClosedRecords2");
        BMap grades = (BMap) returns;

        Assert.assertEquals((grades.get(StringUtils.fromString("maths"))), 4.2);
        Assert.assertEquals((grades.get(StringUtils.fromString("physics"))), 4.2);
        Assert.assertNull(grades.get(StringUtils.fromString("chemistry")));
    }

    @Test(description = "Test case for iterable op chains ending with a terminal op")
    public void testOpChainsWithTerminalOps() {
        long[] grades = new long[]{80, 75};
        BArray returns = (BArray) BRunUtil.invoke(result, "testOpChainsWithTerminalOps",
                new Object[]{(grades[0]), (grades[1]), (65)});
        grades = stream(grades).map(g -> g + 10).toArray();
        Assert.assertEquals(returns.get(0), stream(grades).count());
        Assert.assertEquals(returns.get(1), stream(grades).sum());
        Assert.assertEquals(returns.get(2), stream(grades).max().getAsLong());
        Assert.assertEquals(returns.get(3), stream(grades).min().getAsLong());
        Assert.assertEquals(returns.get(4), stream(grades).average().getAsDouble());
    }

    @Test(description = "Test case for checking whether iterable ops mutate the original record")
    public void testMutability() {
        Object returns = BRunUtil.invoke(result, "testMutability");
        BMap grades = (BMap) returns;
        Assert.assertEquals(grades.size(), 3);
        Assert.assertEquals(grades.get(StringUtils.fromString("maths")), 80L);
        Assert.assertEquals(grades.get(StringUtils.fromString("physics")), 75L);
        Assert.assertEquals(grades.get(StringUtils.fromString("chemistry")), 65L);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        closedRecNegatives = null;
    }
}
