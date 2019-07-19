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


import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * This class tests integer lang module functionality.
 *
 * @since 1.0
 */
public class LangLibXMLTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/xmllib_test.bal");
    }

    @Test(dataProvider = "XMLDataProvider")
    public void testLength(BValue val, long expectedLength) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLength", new BValue[]{val});
        assertEquals(((BInteger) returns[0]).intValue(), expectedLength);
    }

    @DataProvider(name = "XMLDataProvider")
    public Object[][] getXML() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getXML");
        return new Object[][]{
                {returns[0], 1},
                {returns[1], 1},
                {returns[2], 12}
        };
    }

    @Test
    public void testFromXml() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFromString");
        assertEquals(returns[0].stringValue(),
                "<TITLE>Empire Burlesque</TITLE><TITLE>Hide your heart</TITLE><TITLE>Greatest Hits</TITLE>");
    }
}
