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

        // Test invalid no. of args with foreach loop
        BAssertUtil.validateError(openRecNegatives, index++,
                "invalid list binding pattern: attempted to infer a list type, " +
                        "but found 'anydata'",
                34, 13);

        // Test invalid foreach iterable operation
        BAssertUtil.validateError(openRecNegatives, index++,
                "incompatible types: expected '[string,any]', found 'anydata'", 41, 15);
        BAssertUtil.validateError(openRecNegatives, index++,
                "incompatible types: expected '(string|int|Address)', found 'anydata'",
                44, 15);

        // Test invalid map iterable operation
        BAssertUtil.validateError(openRecNegatives, index++,
                "incompatible types: expected '[string,any]', found 'anydata'",
                52, 28);
        BAssertUtil.validateError(openRecNegatives, index++,
                "incompatible types: expected 'map', found 'map<(any|error)>'", 60, 12);
        BAssertUtil.validateError(openRecNegatives, index++,
                "incompatible types: expected 'Person', found 'map<anydata>'",
                68, 21);

        // Test invalid filter iterable operation
        BAssertUtil.validateError(openRecNegatives, index++,
                "incompatible types: expected '[string,any]', found 'anydata'",
                74, 30);
        BAssertUtil.validateError(openRecNegatives, index++,
                "incompatible types: expected " +
                        "'function (ballerina/lang.map:0.0.0:Type) returns (boolean)', " +
                        "found 'function (anydata) returns (string)'",
                82, 21);
        BAssertUtil.validateError(openRecNegatives, index++,
                "incompatible types: expected " +
                        "'function (ballerina/lang.map:0.0.0:Type) returns (boolean)', found " +
                        "'function (anydata) returns ([string,any,string])'",
                86, 21);
        BAssertUtil.validateError(openRecNegatives, index++,
                "incompatible types: expected 'Person', found 'map<anydata>'",
                90, 21);

        // Test mismatching chained iterable op return values
        BAssertUtil.validateError(openRecNegatives, index++,
                "incompatible types: expected 'map<int>', found 'map<float>'",
                107, 18);
        BAssertUtil.validateError(openRecNegatives, index++,
                "incompatible types: expected 'int[]', found 'map<float>'",
                133, 16);

        Assert.assertEquals(openRecNegatives.getErrorCount(), index);
    }

    @Test
    public void testForeachWithOpenRecords() {
        String[] expectedFields = new String[]{"name", "age", "address"};
        BArray returns = (BArray) BRunUtil.invoke(result, "testForeachWithOpenRecords");

        BArray fields = (BArray) returns.get(0);
        for (int i = 0; i < fields.size(); i++) {
            Assert.assertEquals(fields.getString(i), expectedFields[i]);
        }

        BArray values = (BArray) returns.get(1);
        Assert.assertEquals(values.getRefValue(0).toString(), "John Doe");
        Assert.assertEquals((values.getRefValue(1)), 25L);
        Assert.assertTrue(values.getRefValue(2) instanceof BMap);

        BMap addressRecord = (BMap) values.getRefValue(2);
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("street")).toString(), "Palm Grove");
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("city")).toString(), "Colombo 3");
    }

    @Test
    public void testForeachWithOpenRecords2() {
        String[] expectedFields = new String[]{"name", "age", "address", "height"};
        BArray returns = (BArray) BRunUtil.invoke(result, "testForeachWithOpenRecords2");

        BArray fields = (BArray) returns.get(0);
        for (int i = 0; i < fields.size(); i++) {
            Assert.assertEquals(fields.getString(i), expectedFields[i]);
        }

        BArray values = (BArray) returns.get(1);
        Assert.assertEquals(values.getRefValue(0).toString(), "John Doe");
        Assert.assertEquals((values.getRefValue(1)), 25L);
        Assert.assertTrue(values.getRefValue(2) instanceof BMap);
        Assert.assertEquals((values.getRefValue(3)), 5.9);

        BMap addressRecord = (BMap) values.getRefValue(2);
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("street")).toString(), "Palm Grove");
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("city")).toString(), "Colombo 3");
    }

    @Test
    public void testForeachWithOpenRecords3() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testForeachWithOpenRecords3");

        Assert.assertEquals(returns.get(0).toString(), "John Doe");
        Assert.assertEquals(returns.get(1), 25L);
        Assert.assertTrue(returns.get(2) instanceof BMap);

        BMap addressRecord = (BMap) returns.get(2);
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("street")).toString(), "Palm Grove");
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("city")).toString(), "Colombo 3");
    }

    @Test(description = "Tests foreach iterable operation on open records")
    public void testForeachOpWithOpenRecords() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testForeachOpWithOpenRecords");

        Assert.assertEquals(returns.get(0).toString(), "John Doe");
        Assert.assertEquals(returns.get(1), 25L);
        Assert.assertTrue(returns.get(2) instanceof BMap);

        BMap addressRecord = (BMap) returns.get(2);
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("street")).toString(), "Palm Grove");
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("city")).toString(), "Colombo 3");

        Assert.assertEquals(returns.get(3), 5.9);
    }

    @Test(description = "Tests map iterable operation on open records")
    public void testMapOpWithOpenRecords() {
        String[] expectedFields = new String[]{"name", "age", "address", "profession"};
        Object returns = BRunUtil.invoke(result, "testMapOpWithOpenRecords");

        BMap person = (BMap) returns;
        Assert.assertEquals(person.get(StringUtils.fromString(expectedFields[0])).toString(), "john doe");
        Assert.assertEquals((person.get(StringUtils.fromString(expectedFields[1]))), 25L);
        Assert.assertTrue(person.get(StringUtils.fromString(expectedFields[2])) instanceof BMap);
        Assert.assertEquals(person.get(StringUtils.fromString(expectedFields[3])).toString(), "software engineer");

        BMap addressRecord = (BMap) person.get(StringUtils.fromString(expectedFields[2]));
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("street")).toString(), "Palm Grove");
        Assert.assertEquals(addressRecord.get(StringUtils.fromString("city")).toString(), "Colombo 3");
    }

    @Test(description = "Tests filter iterable operation on open records")
    public void testFilterOpWithOpenRecords() {
        Object returns = BRunUtil.invoke(result, "testFilterOpWithOpenRecords");
        BMap foo = (BMap) returns;

        Assert.assertNull(foo.get(StringUtils.fromString("a")));
        Assert.assertEquals(foo.get(StringUtils.fromString("b")).toString(), "B");
        Assert.assertEquals(foo.get(StringUtils.fromString("c")).toString(), "C");
        Assert.assertEquals(foo.get(StringUtils.fromString("d")).toString(), "D");
        Assert.assertNull(foo.get(StringUtils.fromString("e")));
        Assert.assertEquals(foo.get(StringUtils.fromString("f")).toString(), "F");
    }

    @Test(description = "Tests count iterable operation on open records")
    public void testCountOpWithOpenRecords() {
        Object returns = BRunUtil.invoke(result, "testCountOpWithOpenRecords");
        Assert.assertEquals(returns, 6L);
    }

    @Test(description = "Test case for chained iterable operations on open records")
    public void testChainedOpsWithOpenRecords() {
        Object returns = BRunUtil.invoke(result, "testChainedOpsWithOpenRecords");
        BMap foo = (BMap) returns;

        Assert.assertNull(foo.get(StringUtils.fromString("a")));
        Assert.assertEquals(foo.get(StringUtils.fromString("b")).toString(), "bb");
        Assert.assertEquals(foo.get(StringUtils.fromString("c")).toString(), "cc");
        Assert.assertEquals(foo.get(StringUtils.fromString("d")).toString(), "dd");
        Assert.assertNull(foo.get(StringUtils.fromString("e")));
        Assert.assertEquals(foo.get(StringUtils.fromString("f")).toString(), "ff");
    }

    @Test(description = "Test case for map op on open records with all int fields")
    public void testMapWithAllStringOpenRecord() {
        Object returns = BRunUtil.invoke(result, "testMapWithAllStringOpenRecord");

        BMap foo = (BMap) returns;
        Assert.assertEquals(foo.get(StringUtils.fromString("a")).toString(), "aa");
        Assert.assertEquals(foo.get(StringUtils.fromString("b")).toString(), "bb");
        Assert.assertEquals(foo.get(StringUtils.fromString("c")).toString(), "cc");
        Assert.assertEquals(foo.get(StringUtils.fromString("d")).toString(), "dd");
        Assert.assertEquals(foo.get(StringUtils.fromString("e")).toString(), "ee");
    }

    @Test(description = "Test case for map op on open records with all int fields")
    public void testMapWithAllIntOpenRecord() {
        Object[] args = new Object[]{(80), (75), (65), (78)};
        Object returns = BRunUtil.invoke(result, "testMapWithAllIntOpenRecord", args);

        BMap gradesMap = (BMap) returns;
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("maths"))), 90L);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("physics"))), 85L);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("chemistry"))), 75L);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("english"))), 88L);
    }

    @Test(description = "Test case for map op on open records with all float fields")
    public void testMapWithAllFloatOpenRecord() {
        final double a = 20.15, b = 7.89, c = 5.15, p = 9.9;
        Object[] args = new Object[]{(a), (b), (c)};
        Object returns = BRunUtil.invoke(result, "testMapWithAllFloatOpenRecord", args);

        BMap gradesMap = (BMap) returns;
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("x"))), a + 10);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("y"))), b + 10);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("z"))), c + 10);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("p"))), p + 10);
    }

    @Test(description = "Test case for filter op on open records with all string fields")
    public void testFilterWithAllStringOpenRecord() {
        final String a = "AA", e = "EE", f = "FF";
        Object returns = BRunUtil.invoke(result, "testFilterWithAllStringOpenRecord");

        BMap foo = (BMap) returns;
        Assert.assertEquals(foo.get(StringUtils.fromString("a")).toString(), a);
        Assert.assertNull(foo.get(StringUtils.fromString("b")));
        Assert.assertNull(foo.get(StringUtils.fromString("c")));
        Assert.assertNull(foo.get(StringUtils.fromString("d")));
        Assert.assertEquals(foo.get(StringUtils.fromString("e")).toString(), e);
        Assert.assertEquals(foo.get(StringUtils.fromString("f")).toString(), f);
    }

    @Test(description = "Test case for filter op on open records with all int fields")
    public void testFilterWithAllIntOpenRecord() {
        final long m = 80, p = 75, e = 78;
        Object returns = BRunUtil.invoke(result, "testFilterWithAllIntOpenRecord");

        BMap gradesMap = (BMap) returns;
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("maths"))), m);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("physics"))), p);
        Assert.assertNull(gradesMap.get(StringUtils.fromString("chemistry")));
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("english"))), e);
    }

    @Test(description = "Test case for map op on open records with all float fields")
    public void testFilterWithAllFloatOpenRecord() {
        final double a = 20.15, b = 7.89, c = 5.15, p = 9.9;
        Object[] args = new Object[]{(a), (b), (c)};
        Object returns = BRunUtil.invoke(result, "testFilterWithAllFloatOpenRecord", args);

        BMap gradesMap = (BMap) returns;
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("x"))), a);
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("y"))), b);
        Assert.assertNull(gradesMap.get(StringUtils.fromString("z")));
        Assert.assertEquals((gradesMap.get(StringUtils.fromString("p"))), p);
    }

    @Test(description = "Test case for terminal ops on open records with all int fields")
    public void testTerminalOpsOnAllIntOpenRecord() {
        long[] marks = new long[]{80, 75, 65, 78};
        Object[] args = new Object[]
                {(marks[0]), (marks[1]), (marks[2]), (marks[3])};
        BArray returns = (BArray) BRunUtil.invoke(result, "testTerminalOpsOnAllIntOpenRecord", args);

        Assert.assertEquals(returns.get(0), stream(marks).count());
        Assert.assertEquals(returns.get(1), stream(marks).max().getAsLong());
        Assert.assertEquals(returns.get(2), stream(marks).min().getAsLong());
        Assert.assertEquals(returns.get(3), stream(marks).sum());
        Assert.assertEquals(returns.get(4), stream(marks).average().getAsDouble());
    }

    @Test(description = "Test case for chained iterable operations on open records")
    public void testChainedOpsWithOpenRecords2() {
        Object returns = BRunUtil.invoke(result, "testChainedOpsWithOpenRecords2");
        BMap grades = (BMap) returns;

        Assert.assertEquals((grades.get(StringUtils.fromString("maths"))), 4.2);
        Assert.assertEquals((grades.get(StringUtils.fromString("physics"))), 4.2);
        Assert.assertEquals((grades.get(StringUtils.fromString("english"))), 4.2);
        Assert.assertNull(grades.get(StringUtils.fromString("chemistry")));
    }

    @Test(description = "Test case for chained iterable operations on open records")
    public void testChainedOpsWithOpenRecords3() {
        Object returns = BRunUtil.invoke(result, "testChainedOpsWithOpenRecords3");
        BMap grades = (BMap) returns;

        Assert.assertEquals((grades.get(StringUtils.fromString("maths"))), 4.2);
        Assert.assertEquals((grades.get(StringUtils.fromString("physics"))), 4.2);
        Assert.assertEquals((grades.get(StringUtils.fromString("english"))), 4.2);
        Assert.assertNull(grades.get(StringUtils.fromString("chemistry")));
    }

    @Test(description = "Test case for terminal ops on open records with all int fields")
    public void testTerminalOpsOnAllIntOpenRecord2() {
        long[] marks = new long[]{80, 75, 0, 78};
        Object[] args = new Object[]
                {(marks[0]), (marks[1]), (marks[3])};
        BArray returns = (BArray) BRunUtil.invoke(result, "testTerminalOpsOnAllIntOpenRecord2", args);

        Assert.assertEquals(returns.get(0), stream(marks).count());
        Assert.assertEquals(returns.get(1), stream(marks).max().getAsLong());
        Assert.assertEquals(returns.get(2), stream(marks).min().getAsLong());
        Assert.assertEquals(returns.get(3), stream(marks).sum());
        Assert.assertEquals(returns.get(4), stream(marks).average().getAsDouble());
    }

    @Test(description = "Test case for iterable op chains ending with a terminal op")
    public void testOpChainsWithTerminalOps() {
        long[] grades = new long[]{80, 76, 78};
        BArray returns = (BArray) BRunUtil.invoke(result, "testOpChainsWithTerminalOps",
                new Object[]{(grades[0]), (grades[1]),
                        (65)});
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
        Assert.assertEquals(grades.size(), 4);
        Assert.assertEquals(grades.get(StringUtils.fromString("maths")), 80L);
        Assert.assertEquals(grades.get(StringUtils.fromString("physics")), 75L);
        Assert.assertEquals(grades.get(StringUtils.fromString("chemistry")), 65L);
        Assert.assertEquals(grades.get(StringUtils.fromString("english")), 78L);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        openRecNegatives = null;
    }
}
