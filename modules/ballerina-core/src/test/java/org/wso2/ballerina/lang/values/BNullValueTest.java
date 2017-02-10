/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.lang.values;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BNull;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * This test class will test the behaviour of null values.
 */
public class BNullValueTest {
    private BallerinaFile bFile;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/values/null-value.bal");
    }

    @Test(description = "Test null value assignment")
    public void testNullValueAssignment() {
        BValue[] returns = Functions.invoke(bFile, "nullAssignment1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test null value assignment at the variable definition")
    public void testNullValueAssignmentInDefinition() {
        BValue[] returns = Functions.invoke(bFile, "nullAssignment2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test null equel expression")
    public void testNullCheckExpression() {
        BValue[] returns = Functions.invoke(bFile, "nullCheck");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        BBoolean isNull = (BBoolean) returns[0];
        Assert.assertTrue(isNull.booleanValue(), "Null check false");
    }

    @Test(description = "Test null return statement")
    public void testNullReturnStatement() {
        BValue[] returns = Functions.invoke(bFile, "getNull");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test null value assignment for struct")
    public void testNullValueAssignmentForStruct() {
        BValue[] returns = Functions.invoke(bFile, "setStructNull");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BNull.class);
        BNull nullValue = (BNull) returns[0];
        Assert.assertNotNull(nullValue);
    }

    @Test(description = "Test accessing a element of null assigned struct",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = ".* Symbol p is null")
    public void testAccessingAElementOfNullStruct() {
        Functions.invoke(bFile, "accessElementOfNullStruct");
    }

    @Test(description = "Test setting a value to a element of null assigned struct",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = ".* Symbol p is null")
    public void testSettingAElementOfNullStruct() {
        Functions.invoke(bFile, "settingElementOfNullStruct");
    }

    @Test(description = "Test accessing a value of a struct element of null assigned struct",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = ".* Symbol family is null")
    public void testAccessingAStructElementOfNullStruct() {
        Functions.invoke(bFile, "accessingStructElementOfNullStruct");
    }

    @Test(description = "Test setting a value to a element of struct of null assigned struct",
          expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = ".* Symbol family is null")
    public void testSettingAStructElementOfNullStruct() {
        Functions.invoke(bFile, "settingStructElementOfNullStruct");
    }
}
