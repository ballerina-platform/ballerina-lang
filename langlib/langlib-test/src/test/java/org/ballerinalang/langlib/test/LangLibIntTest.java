/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.test;


import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.runtime.util.BLangConstants.INT_LANG_LIB;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.NUMBER_PARSING_ERROR_IDENTIFIER;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static org.testng.Assert.assertEquals;

/**
 * This class tests integer lang module functionality.
 *
 * @since 1.0
 */
public class LangLibIntTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/intlib_test.bal");
    }

    @Test(dataProvider = "MaxNumList")
    public void testMax(BInteger n, BValueArray ns, long expectedMax) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMax", new BValue[]{n, ns});
        assertEquals(((BInteger) returns[0]).intValue(), expectedMax);
    }

    @Test(dataProvider = "MinNumList")
    public void testMin(BInteger n, BValueArray ns, long expectedMin) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMin", new BValue[]{n, ns});
        assertEquals(((BInteger) returns[0]).intValue(), expectedMin);
    }

    @Test
    public void testFromString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFromString");
        assertEquals(((BInteger) returns[0]).intValue(), 123);
        assertEquals(returns[1].getType().getTag(), TypeTags.ERROR_TAG);

        BError err = (BError) returns[1];
        assertEquals(err.getReason(), getModulePrefixedReason(INT_LANG_LIB, NUMBER_PARSING_ERROR_IDENTIFIER)
                .getValue());
        assertEquals(err.getDetails().toString(), "{\"message\":\"'string' value '12invalid34' " +
                "cannot be converted to 'int'\"}");
    }

    @Test
    public void testSum() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSum");
        assertEquals(((BInteger) returns[0]).intValue(), 110);
    }

    @Test
    public void testAbs() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAbs");
        assertEquals(((BInteger) returns[0]).intValue(), 123);
        assertEquals(((BInteger) returns[1]).intValue(), 234);
    }

    @Test
    public void testToHexString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testToHexString");
        assertEquals(returns[0].stringValue(), "75bcd15");
        // TODO: 7/6/19 Verify the representation of negative numbers
//        assertEquals(returns[1].stringValue(), "ffffffffffffcfc7");
    }

    @Test
    public void testFromHexString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFromHexString");
        assertEquals(((BInteger) returns[0]).intValue(), 11259205);
        assertEquals(returns[1].getType().getTag(), TypeTags.ERROR_TAG);

        BError err = (BError) returns[1];
        assertEquals(err.getReason(), getModulePrefixedReason(INT_LANG_LIB, NUMBER_PARSING_ERROR_IDENTIFIER)
                .getValue());
        assertEquals(err.getDetails().toString(), "{\"message\":\"For input string: \"12invalid34\"\"}");
    }

    @DataProvider(name = "MaxNumList")
    public Object[][] getMaxNumList() {
        return new Object[][]{
                {new BInteger(54), new BValueArray(new long[]{23, 34}), 54},
                {new BInteger(23), new BValueArray(new long[]{34, 47}), 47},
                {new BInteger(-1), new BValueArray(new long[]{-20, -4}), -1},
        };
    }

    @DataProvider(name = "MinNumList")
    public Object[][] getMinNumList() {
        return new Object[][]{
                {new BInteger(54), new BValueArray(new long[]{23, 34}), 23},
                {new BInteger(23), new BValueArray(new long[]{34, 47}), 23},
                {new BInteger(-1), new BValueArray(new long[]{-20, -4}), -20},
        };
    }
}
