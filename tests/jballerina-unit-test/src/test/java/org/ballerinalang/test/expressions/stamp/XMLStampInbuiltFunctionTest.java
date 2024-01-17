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

import io.ballerina.runtime.internal.types.BXmlType;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

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

        Object results = BRunUtil.invoke(compileResult, "stampXMLToXML");
        Object xmlValue = results;

        Assert.assertEquals(xmlValue.toString(), "<book>The Lost World</book>");
        Assert.assertEquals(getType(xmlValue).getClass(), BXmlType.class);
    }

    @Test
    public void testStampXMLToAnydata() {

        Object results = BRunUtil.invoke(compileResult, "stampXMLToAnydata");
        Object anydataValue = results;

        Assert.assertEquals(anydataValue.toString(), "<book>The Lost World</book>");
        Assert.assertEquals(getType(anydataValue).getClass(), BXmlType.class);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}

