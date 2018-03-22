/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.connectors;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test the functionality of filter connectors.
 */
@Test (enabled = false)
public class FilterConnectorTest {

    private CompileResult result;

    @BeforeClass()
    public void setup() {
//        result = BTestUtils.compile("test-src/connectors/filter-connector-test-workers.bal");
    }

    @Test(description = "Test TestConnector action1", enabled = false)
    public void testConnectorAction1() {
        result = BCompileUtil.compile("test-src/connectors/filter-connector-test-workers.bal");
        BValue[] returns = BRunUtil.invoke(result, "testAction1");
        Assert.assertEquals(returns.length, 1);
        BJSON actionReturned = (BJSON) returns[0];
        String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(actionReturned.stringValue(), expected);
    }

    @Test(description = "Test passing arguments for filter connectors", enabled = false)
    public void testFilterConnectorArgumentPassing() {
        result = BCompileUtil.compile("test-src/connectors/filter-connector-ap-test.bal");
        BValue[] args = {new BString("WSO2")};
        BValue[] returns = BRunUtil.invoke(result, "testArgumentPassing", args);
        Assert.assertEquals(returns.length, 1);
        BInteger actionReturned = (BInteger) returns[0];
        int expected = 500;
        Assert.assertEquals(actionReturned.intValue(), expected);
    }

    @Test(description = "Test multiple filters", enabled = false)
    public void testMultipleFilterConnector() {
        result = BCompileUtil.compile("test-src/connectors/filter-connector-multiple-test.bal");
        BValue[] args = {new BString("WSO2")};
        BValue[] returns = BRunUtil.invoke(result, "testArgumentPassing", args);
        Assert.assertEquals(returns.length, 1);
        BInteger actionReturned = (BInteger) returns[0];
        int expected = 500;
        Assert.assertEquals(actionReturned.intValue(), expected);
    }

    @Test(description = "Test filter connectors negative syntax when filter base type is not compatible"
            , enabled = false)
    public void testFilterConnectorNegativeIncompatibleFilterType() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/connectors/filter-connector-negative-incompatible-filter.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0
                , "connector types 'FilterConnector' and 'TestConnector' are not equivalent", 13, 32);
    }

    @Test(description = "Test filter connectors negative syntax when filter base type is not defined")
    public void testFilterConnectorNegativeFilterBaseTypeNotDefined() {
        CompileResult resultNegative = BCompileUtil
                .compile("test-src/connectors/filter-connector-negative-base-type-undefined.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 4);
        BAssertUtil.validateError(resultNegative, 0, "undefined symbol 'TestConnector2'", 6, 23);
        BAssertUtil.validateError(resultNegative, 1, "unknown type 'TestConnector'", 17, 5);
        BAssertUtil.validateError(resultNegative, 2, "undefined connector 'TestConnector'", 18, 9);
        //TODO change error msg to a better one
        BAssertUtil.validateError(resultNegative, 3, "undefined connector '><'", 22, 13);
    }

    @Test(description = "Test filter connectors negative syntax when filter connector input types are incompatible",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*message: connector input types are not equivalent in " +
                    "connectors 'TestConnector2' and 'TestConnector'.*", enabled = false)
    public void testFilterConnectorNegativeInputTypesIncompatible() {
        result = BCompileUtil.compile("test-src/connectors/filter-connector-negative-test.bal");
        BValue[] args = {new BString("WSO2")};
        BRunUtil.invoke(result, "testArgumentPassing", args);
    }

}
