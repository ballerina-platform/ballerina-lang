/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.expressions.stamp;

import org.ballerinalang.core.model.types.BXMLType;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for stamping XML type variables.
 *
 * @since 0.985.0
 */
public class XMLStampInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/stamp/xml-stamp-expr-test.bal");
    }

    //----------------------------- XML Stamp Test cases ------------------------------------------------------

    @Test
    public void testStampXMLToXML() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampXMLToXML");
        BValue xmlValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(xmlValue.stringValue(), "<book>The Lost World</book>");
        Assert.assertEquals(xmlValue.getType().getClass(), BXMLType.class);
    }

    @Test
    public void testStampXMLToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampXMLToAnydata");
        BValue anydataValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anydataValue.stringValue(), "<book>The Lost World</book>");
        Assert.assertEquals(anydataValue.getType().getClass(), BXMLType.class);
    }
}

