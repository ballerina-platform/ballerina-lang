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
package org.ballerinalang.test.expressions.seal;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BAnyType;
import org.ballerinalang.model.types.BAnydataType;
import org.ballerinalang.model.types.BXMLType;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for sealing XML type variables.
 *
 * @since 0.983.0
 */
public class XMLSealInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/seal/xml-seal-expr-test.bal");
    }

    //----------------------------- XML Seal Test cases ------------------------------------------------------

    @Test
    public void testSealXMLToAny() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealXMLToAny");
        BValue anyValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anyValue.stringValue(), "<book>The Lost World</book>");
        Assert.assertEquals(anyValue.getType().getClass(), BAnyType.class);
    }

    @Test
    public void testSealXMLToXML() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealXMLToXML");
        BValue anyValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anyValue.stringValue(), "<book>The Lost World</book>");
        Assert.assertEquals(anyValue.getType().getClass(), BXMLType.class);
    }

    @Test
    public void testSealXMLToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealXMLToAnydata");
        BValue anyValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anyValue.stringValue(), "<book>The Lost World</book>");
        Assert.assertEquals(anyValue.getType().getClass(), BAnydataType.class);
    }
}

