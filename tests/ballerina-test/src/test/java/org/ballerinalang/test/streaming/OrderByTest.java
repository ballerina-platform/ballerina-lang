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
package org.ballerinalang.test.streaming;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This contains methods to test ordering behaviour of Ballerina Streaming.
 *
 * @since 0.970.0
 */
public class OrderByTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/streaming/orderby-streaming-test.bal");
    }

    @Test(description = "Test order by in streaming query - a simple one")
    public void testOrderBy() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startOrderBy");
        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertEquals(outputEmployeeEvents.length, 10, "Expected events are not received");
        for (int i = 1; i < outputEmployeeEvents.length; i++) {
            BStruct currentTeacher = (BStruct) outputEmployeeEvents[i];
            BStruct previousTeacher = (BStruct) outputEmployeeEvents[i - 1];
            Assert.assertTrue(currentTeacher.getIntField(0) >= previousTeacher.getIntField(0));
        }
    }

    @Test(description = "Test order by streaming query with ascending")
    public void testOrderBy2() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startOrderBy2");
        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertEquals(outputEmployeeEvents.length, 10, "Expected events are not received");
        for (int i = 1; i < outputEmployeeEvents.length; i++) {
            BStruct currentTeacher = (BStruct) outputEmployeeEvents[i];
            BStruct previousTeacher = (BStruct) outputEmployeeEvents[i - 1];
            Assert.assertTrue(currentTeacher.getIntField(0) >= previousTeacher.getIntField(0));
        }
    }

    @Test(description = "Test order by streaming query with descending")
    public void testOrderBy3() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startOrderBy3");
        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertEquals(outputEmployeeEvents.length, 10, "Expected events are not received");
        for (int i = 1; i < outputEmployeeEvents.length; i++) {
            BStruct currentTeacher = (BStruct) outputEmployeeEvents[i];
            BStruct previousTeacher = (BStruct) outputEmployeeEvents[i - 1];
            Assert.assertTrue(currentTeacher.getIntField(0) <= previousTeacher.getIntField(0));
        }
    }

    @Test(description = "Test order by streaming query with multiple attributes")
    public void testOrderBy4() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startOrderBy4");
        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertEquals(outputEmployeeEvents.length, 10, "Expected events are not received");
        for (int i = 1; i < outputEmployeeEvents.length; i++) {
            BStruct currentTeacher = (BStruct) outputEmployeeEvents[i];
            BStruct previousTeacher = (BStruct) outputEmployeeEvents[i - 1];
            Assert.assertTrue(currentTeacher.getStringField(1).compareToIgnoreCase(previousTeacher.getStringField(1))
                              >= 0);
            if (currentTeacher.getStringField(1).equalsIgnoreCase(previousTeacher.getStringField(1))) {
                Assert.assertTrue(currentTeacher.getIntField(0) >= previousTeacher.getIntField(0));
            }
        }
    }

    @Test(description = "Test order by streaming query with multiple attributes with multiple order types")
    public void testOrderBy5() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startOrderBy5");
        Assert.assertNotNull(outputEmployeeEvents);
        Assert.assertEquals(outputEmployeeEvents.length, 10, "Expected events are not received");
        for (int i = 1; i < outputEmployeeEvents.length; i++) {
            BStruct currentTeacher = (BStruct) outputEmployeeEvents[i];
            BStruct previousTeacher = (BStruct) outputEmployeeEvents[i - 1];
            Assert.assertTrue(currentTeacher.getStringField(1).compareToIgnoreCase(previousTeacher.getStringField(1))
                              >= 0);
            if (currentTeacher.getStringField(1).equalsIgnoreCase(previousTeacher.getStringField(1))) {
                Assert.assertTrue(currentTeacher.getIntField(0) <= previousTeacher.getIntField(0));
            }
        }
    }
}
