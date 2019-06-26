/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.streams;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * This contains methods to test select behaviour in Ballerina Streaming V2.
 *
 * @since 0.980.0
 */
public class BallerinaStreamsV2SelectorTest {

    private CompileResult result, resultForSelectAll, resultWithComplexExpressions, resultWithLibraryFunction,
            resultWithObjectReferences;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/streamingv2-select-test.bal");
        resultForSelectAll = BCompileUtil.compile("test-src/streamingv2-select-all-test.bal");
        resultWithComplexExpressions = BCompileUtil.compile(
                "test-src/streamingv2-select-with-mathematical-and-logical-operators-test.bal");
        resultWithLibraryFunction = BCompileUtil.compile(
                "test-src/streamingv2-select-with-library-function-test.bal");
        resultWithObjectReferences = BCompileUtil.compile(
                "test-src/streamingv2-select-with-object-reference-test.bal");
    }

    @Test(description = "Test streaming selector query")
    public void testSelectQuery() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startSelectQuery");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("teacherName").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 25);
        Assert.assertEquals(employee1.get("teacherName").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 45);
        Assert.assertEquals(employee2.get("teacherName").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee2.get("age")).intValue(), 50);
    }

    @Test(description = "Test select all streaming query")
    public void testSelectAllQuery() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultForSelectAll, "startSelectQuery");

        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 25);
        Assert.assertEquals(employee1.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 45);
        Assert.assertEquals(employee2.get("name").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee2.get("age")).intValue(), 50);
    }

    @Test(description = "Test select streaming query with complex expressions")
    public void testSelectQueryWithComplexExpressions() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultWithComplexExpressions, "startSelectQuery");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("teacherName").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("netSalary")).intValue(), 9000);
        Assert.assertEquals(((BBoolean) employee0.get("isOlder")).booleanValue(), false);
        Assert.assertEquals(((BBoolean) employee0.get("isNotOlder")).booleanValue(), true);
        Assert.assertEquals(((BInteger) employee0.get("fakeAge")).intValue(), 24);
        Assert.assertEquals(((BBoolean) employee0.get("isMarried")).booleanValue(), false);
        Assert.assertEquals(employee0.get("isYounger").stringValue(), "younger");
        Assert.assertEquals(employee0.get("school").stringValue(), "Ananda College");
        Assert.assertEquals(((BBoolean) employee0.get("isString")).booleanValue(), true);
        Assert.assertEquals(employee0.get("companyName").stringValue(), "wso2");
        Assert.assertEquals(employee0.get("concat").stringValue(), "Raja 25");

        Assert.assertEquals(employee2.get("teacherName").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee2.get("netSalary")).intValue(), 9000);
        Assert.assertEquals(((BBoolean) employee2.get("isOlder")).booleanValue(), true);
        Assert.assertEquals(((BBoolean) employee2.get("isNotOlder")).booleanValue(), false);
        Assert.assertEquals(((BInteger) employee2.get("fakeAge")).intValue(), 49);
        Assert.assertEquals(((BBoolean) employee2.get("isMarried")).booleanValue(), true);
        Assert.assertEquals(employee2.get("isYounger").stringValue(), "older");
        Assert.assertEquals(employee2.get("school").stringValue(), "School not found");
        Assert.assertEquals(((BBoolean) employee2.get("isString")).booleanValue(), true);
        Assert.assertEquals(employee2.get("companyName").stringValue(), "microsoft");
        Assert.assertEquals(employee2.get("concat").stringValue(), "Shareek 50");
    }

    @Test(description = "Test filter streaming query with library functions")
    public void testSelectQueryWithLibraryFunctions() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultWithLibraryFunction, "startSelectQuery");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("upperCaseName").stringValue(), "RAJA");
        Assert.assertEquals(((BInteger) employee0.get("absoluteAge")).intValue(), 25);
        Assert.assertEquals(employee1.get("upperCaseName").stringValue(), "MOHAN");
        Assert.assertEquals(((BInteger) employee1.get("absoluteAge")).intValue(), 45);
        Assert.assertEquals(employee2.get("upperCaseName").stringValue(), "SHAREEK");
        Assert.assertEquals(((BInteger) employee2.get("absoluteAge")).intValue(), 50);
    }

    @Test(description = "Test filter streaming query with object references")
    public void testSelectQueryWithObjectReferences() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultWithObjectReferences, "startSelectQuery");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];
        BMap<String, BValue> employee2 = (BMap<String, BValue>) outputEmployeeEvents[2];

        //timestamp may get varied, hence a delta is introduced
        Date currTime = new Date();
        int delta = 10000;
        long lowerBound = currTime.getTime() - delta;
        long upperBound = currTime.getTime() + delta;

        Assert.assertEquals(employee0.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("marksOfStudent")).intValue(), 45);
        long eventTime0  = ((BInteger) employee0.get("currTime")).intValue();
        Assert.assertEquals(lowerBound < eventTime0 && eventTime0 < upperBound, true);

        Assert.assertEquals(employee1.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee1.get("marksOfStudent")).intValue(), 45);
        long eventTime1  = ((BInteger) employee1.get("currTime")).intValue();
        Assert.assertEquals(lowerBound < eventTime1 && eventTime1 < upperBound, true);

        Assert.assertEquals(employee2.get("name").stringValue(), "Shareek");
        Assert.assertEquals(((BInteger) employee2.get("marksOfStudent")).intValue(), 45);
        long eventTime2  = ((BInteger) employee2.get("currTime")).intValue();
        Assert.assertEquals(lowerBound < eventTime2 && eventTime2 < upperBound, true);
    }
}
