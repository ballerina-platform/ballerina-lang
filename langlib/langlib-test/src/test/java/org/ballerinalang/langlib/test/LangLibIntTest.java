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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.constants.RuntimeConstants.INT_LANG_LIB;
import static io.ballerina.runtime.api.utils.TypeUtils.getType;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.NUMBER_PARSING_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
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

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }

    @Test(dataProvider = "MaxNumList")
    public void testMax(long n, BArray ns, long expectedMax) {
        Object returns = BRunUtil.invoke(compileResult, "testMax", new Object[]{n, ns});
        assertEquals(returns, expectedMax);
    }

    @Test(dataProvider = "MinNumList")
    public void testMin(long n, BArray ns, long expectedMin) {
        Object returns = BRunUtil.invoke(compileResult, "testMin", new Object[]{n, ns});
        assertEquals(returns, expectedMin);
    }

    @Test
    public void testFromString() {
        Object returns = BRunUtil.invoke(compileResult, "testFromString");
        BArray result = (BArray) returns;
        assertEquals(result.get(0), 123L);
        assertEquals(getType(result.get(1)).getTag(), TypeTags.ERROR_TAG);

        BError err = (BError) result.get(1);
        assertEquals(err.getErrorMessage().getValue(),
                getModulePrefixedReason(INT_LANG_LIB, NUMBER_PARSING_ERROR_IDENTIFIER).getValue());
        assertEquals(err.getDetails().toString(), "{\"message\":\"'string' value '12invalid34' " +
                "cannot be converted to 'int'\"}");
    }

    @Test
    public void testSum() {
        Object returns = BRunUtil.invoke(compileResult, "testSum");
        assertEquals(returns, 110L);
    }

    @Test
    public void testAbs() {
        Object returns = BRunUtil.invoke(compileResult, "testAbs");
        BArray result = (BArray) returns;
        assertEquals(result.get(0), 123L);
        assertEquals(result.get(1), 234L);
    }

    @Test
    public void testToHexString() {
        Object returns = BRunUtil.invoke(compileResult, "testToHexString");
        BArray result = (BArray) returns;
        assertEquals(result.get(0).toString(), "75bcd15");
        assertEquals(result.get(1).toString(), "-3039");
        assertEquals(result.get(2).toString(), "-2dfd5533a");
    }

    @Test
    public void testFromHexString() {
        Object returns = BRunUtil.invoke(compileResult, "testFromHexString");
        BArray result = (BArray) returns;
        assertEquals(result.get(0), 11259205L);
        assertEquals(getType(result.get(1)).getTag(), TypeTags.ERROR_TAG);

        BError err = (BError) result.get(1);
        assertEquals(err.getErrorMessage().getValue(),
                getModulePrefixedReason(INT_LANG_LIB, NUMBER_PARSING_ERROR_IDENTIFIER).getValue());
        assertEquals(err.getDetails().toString(), "{\"message\":\"For input string: \"12invalid34\"\"}");
    }

    @DataProvider(name = "MaxNumList")
    public Object[][] getMaxNumList() {
        return new Object[][]{
                {54, ValueCreator.createArrayValue(new long[]{23, 34}), 54},
                {23, ValueCreator.createArrayValue(new long[]{34, 47}), 47},
                {-1, ValueCreator.createArrayValue(new long[]{-20, -4}), -1},
        };
    }

    @DataProvider(name = "MinNumList")
    public Object[][] getMinNumList() {
        return new Object[][]{
                {54, ValueCreator.createArrayValue(new long[]{23, 34}), 23},
                {23, ValueCreator.createArrayValue(new long[]{34, 47}), 23},
                {-1, ValueCreator.createArrayValue(new long[]{-20, -4}), -20},
        };
    }

    @Test
    public void testChainedIntFunctions() {
        Object returns = BRunUtil.invoke(compileResult, "testChainedIntFunctions");
        assertEquals(returns, 1L);
    }

    @Test(dataProvider = "functionProvider")
    public void testIntFunctions(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @DataProvider
    public Object[] functionProvider() {
        return new String[] {"testToHexStringNonPositives", "testLangLibCallOnIntSubTypes",
                "testLangLibCallOnFiniteType", "testIntOverflow", "testIntOverflowWithSum",
                "testIntNonOverflowWithSum"};
    }

}
