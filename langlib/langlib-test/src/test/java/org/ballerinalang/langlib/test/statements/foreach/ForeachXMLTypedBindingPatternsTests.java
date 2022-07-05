/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.test.statements.foreach;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for typed binding patterns in foreach.
 *
 * @since 0.985.0
 */
public class ForeachXMLTypedBindingPatternsTests {

    private CompileResult program;
    private String expectedXml1 = "0:<p:person xmlns:p=\"foo\" xmlns:q=\"bar\">\n" +
            "        <p:name>bob</p:name>\n" +
            "        <p:address>\n" +
            "            <p:city>NY</p:city>\n" +
            "            <q:country>US</q:country>\n" +
            "        </p:address>\n" +
            "        <q:ID>1131313</q:ID>\n" +
            "    </p:person> ";
    private String expectedXml2 = "0:<p:name xmlns:p=\"foo\">bob</p:name> " +
            "1:<p:address xmlns:p=\"foo\">\n" +
            "            <p:city>NY</p:city>\n" +
            "            <q:country xmlns:q=\"bar\">US</q:country>\n" +
            "        </p:address> 2:<q:ID xmlns:q=\"bar\">1131313</q:ID> ";

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-xml-typed-binding-patterns.bal");
    }

    @AfterClass
    public void tearDown() {
        program = null;
    }

    @Test
    public void testXmlWithRootWithSimpleVariableWithoutType() {
        Object returns = BRunUtil.invoke(program, "testXmlWithRootWithSimpleVariableWithoutType");
        Assert.assertEquals(returns.toString(), expectedXml1);
    }

    @Test
    public void testXmlWithRootWithSimpleVariableWithType() {
        Object returns = BRunUtil.invoke(program, "testXmlWithRootWithSimpleVariableWithType");
        Assert.assertEquals(returns.toString(), expectedXml1);
    }

    @Test
    public void testXmlInnerElementsWithSimpleVariableWithoutType() {
        Object returns = BRunUtil.invoke(program, "testXmlInnerElementsWithSimpleVariableWithoutType");
        Assert.assertEquals(returns.toString(), expectedXml2);
    }

    @Test
    public void testXmlInnerElementsWithSimpleVariableWithType() {
        Object returns = BRunUtil.invoke(program, "testXmlInnerElementsWithSimpleVariableWithType");
        Assert.assertEquals(returns.toString(), expectedXml2);
    }

    @Test
    public void testEmptyXmlIteration() {
        Object returns = BRunUtil.invoke(program, "testEmptyXmlIteration");
        Assert.assertEquals(returns.toString(), "");
    }
}
