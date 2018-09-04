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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for record iteration and iterable operations on records.
 *
 * @since 0.981.1
 */
public class RecordIterationTest {

    private CompileResult result, openRecNegatives, closedRecNegatives;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/record/record_iteration.bal");
        openRecNegatives = BCompileUtil.compile("test-src/record/record_iteration_negative.bal");
        closedRecNegatives = BCompileUtil.compile("test-src/record/closed_record_iteration_negative.bal");
    }

    @Test
    public void testNegativesWithOpenRecords() {
        int index = 0;
        Assert.assertEquals(openRecNegatives.getErrorCount(), 15);
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
        BAssertUtil.validateError(openRecNegatives, index++, "iterable type 'Person' requires '2' variables", 34, 20);

        // Test invalid foreach iterable operation
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "not enough variables are defined for iterable type 'Person', require at least '2' " +
                                          "variables", 41, 15);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "too many variables are defined for iterable type 'Person'",
                                  44, 15);

        // Test invalid map iterable operation
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "not enough variables are defined for iterable type 'Person', require at least '2' " +
                                          "variables", 49, 25);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "too many variables are defined for iterable type 'Person'",
                                  53, 18);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "incompatible types: expected 'Person', found '(any) collection'", 57, 18);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "incompatible types: expected 'Person', found '(string,any,string) collection'",
                                  61, 18);

        // Test invalid filter iterable operation
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "not enough variables are defined for iterable type 'Person', require at least '2' " +
                                          "variables", 67, 28);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "too many variables are defined for iterable type 'Person'",
                                  71, 21);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "incompatible lambda function types: expected 'boolean', found 'string'", 75, 21);
        BAssertUtil.validateError(openRecNegatives, index++,
                                  "too many return arguments are defined for operation 'filter'",
                                  79, 21);
    }

    @Test
    public void testNegativesWithClosedRecords() {
        int index = 0;
        Assert.assertEquals(closedRecNegatives.getErrorCount(), 15);
        BAssertUtil.validateError(closedRecNegatives, index++, "operation 'sum' does not support given collection type",
                                  15, 15);
        BAssertUtil.validateError(closedRecNegatives, index++,
                                  "operation 'average' does not support given collection type",
                                  19, 15);
        BAssertUtil.validateError(closedRecNegatives, index++, "operation 'max' does not support given collection type",
                                  23, 15);
        BAssertUtil.validateError(closedRecNegatives, index++, "operation 'min' does not support given collection type",
                                  27, 15);

        // Test invalid no. of args with foreach loop
        BAssertUtil.validateError(closedRecNegatives, index++, "iterable type 'ClosedPerson' requires '2' variables",
                                  34, 20);

        // Test invalid foreach iterable operation
        BAssertUtil.validateError(closedRecNegatives, index++,
                                  "not enough variables are defined for iterable type 'ClosedPerson', require at " +
                                          "least '2' variables", 41, 15);
        BAssertUtil.validateError(closedRecNegatives, index++,
                                  "too many variables are defined for iterable type 'ClosedPerson'", 44, 15);

        // Test invalid map iterable operation
        BAssertUtil.validateError(closedRecNegatives, index++,
                                  "not enough variables are defined for iterable type 'ClosedPerson', require at " +
                                          "least '2' variables", 49, 31);
        BAssertUtil.validateError(closedRecNegatives, index++,
                                  "too many variables are defined for iterable type 'ClosedPerson'", 53, 18);
        BAssertUtil.validateError(closedRecNegatives, index++,
                                  "incompatible types: expected 'ClosedPerson', found '(any) collection'", 57, 18);
        BAssertUtil.validateError(closedRecNegatives, index++,
                                  "incompatible types: expected 'ClosedPerson', found '(string,any,string) collection'",
                                  61, 18);

        // Test invalid filter iterable operation
        BAssertUtil.validateError(closedRecNegatives, index++,
                                  "not enough variables are defined for iterable type 'ClosedPerson', require at " +
                                          "least '2' variables", 67, 34);
        BAssertUtil.validateError(closedRecNegatives, index++,
                                  "too many variables are defined for iterable type 'ClosedPerson'", 71, 21);
        BAssertUtil.validateError(closedRecNegatives, index++,
                                  "incompatible lambda function types: expected 'boolean', found 'string'", 75, 21);
        BAssertUtil.validateError(closedRecNegatives, index++,
                                  "too many return arguments are defined for operation 'filter'", 79, 21);
    }

    // TEST CASES FOR OPEN RECORDS

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

    // TEST CASES FOR CLOSED RECORDS
    @Test
    public void testForeachWithClosedRecords() {
        String[] expectedFields = new String[]{"name", "age", "address"};
        BValue[] returns = BRunUtil.invoke(result, "testForeachWithClosedRecords");

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

    // disabled due to https://github.com/ballerina-platform/ballerina-lang/issues/10074
    @Test(description = "Tests foreach iterable operation on closed records", enabled = false)
    public void testForeachOpWithClosedRecords() {
        String[] expectedFields = new String[]{"name", "age", "address"};
        BValue[] returns = BRunUtil.invoke(result, "testForeachOpWithClosedRecords");

        BMap rec2map = (BMap) returns[0];
        Assert.assertEquals(rec2map.get(expectedFields[0]).stringValue(), "John Doe");
        Assert.assertEquals(((BInteger) rec2map.get(expectedFields[1])).intValue(), 25);
        Assert.assertTrue(rec2map.get(expectedFields[2]) instanceof BMap);
        Assert.assertEquals(((BFloat) rec2map.get(expectedFields[3])).floatValue(), 5.9);

        BMap addressRecord = (BMap) rec2map.get(expectedFields[2]);
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

    @Test(description = "Tests count iterable operation on closed records")
    public void testCountOpWithClosedRecords() {
        BValue[] returns = BRunUtil.invoke(result, "testCountOpWithClosedRecords");
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
}
