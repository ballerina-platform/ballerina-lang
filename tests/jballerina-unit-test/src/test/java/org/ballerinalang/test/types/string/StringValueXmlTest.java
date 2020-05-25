/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.string;

import org.ballerinalang.test.util.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for the generateNewXML* functions for StringValue.
 */
public class StringValueXmlTest extends BStringTestCommons {

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/string/string-value-xml-test.bal");
    }

    @Test
    public void testXmlComment() {
        testAndAssert("testXmlComment", 12);
    }

    @Test
    public void testXmlQName() {
        testAndAssert("testXmlQName", 13);
    }

    @Test
    public void testXmlText() {
        testAndAssert("testXmlText", 19);
    }

    @Test
    public void testXmlProcessingIns() {
        testAndAssert("testXmlProcessingIns", 12);
    }

    @Test
    public void testXmlStr() {
        testAndAssert("testXmlStr", 8);
    }

    @Test
    public void testComplexXml() {
        testAndAssert("testComplexXml", 202);
    }

    @Test
    public void testXmlNamespace() {
        testAndAssert("testXmlNamespace", 334);
    }

    @Test
    public void testXmlInterpolation() {
        testAndAssert("testXmlInterpolation", 249);
    }
}
