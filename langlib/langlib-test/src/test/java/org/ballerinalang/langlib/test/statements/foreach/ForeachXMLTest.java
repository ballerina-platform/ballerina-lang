/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.langlib.test.statements.foreach;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for foreach with XMLs.
 *
 * @since 0.96.0
 */
public class ForeachXMLTest {

    private CompileResult program;
    private final String payload = "<p:person xmlns:p=\"foo\" xmlns:q=\"bar\">\n" +
            "        <p:name>bob</p:name>\n" +
            "        <p:address>\n" +
            "            <p:city>NY</p:city>\n" +
            "            <q:country>US</q:country>\n" +
            "        </p:address>\n" +
            "        <q:ID>1131313</q:ID>\n" +
            "    </p:person>";

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-xml.bal");
    }

    @Test
    public void testXMLWithArityOne() {
        BValue[] returns = BRunUtil.invoke(program, "testXMLWithArityOne");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), payload + " ");
    }

    @Test
    public void testXMLWithArityTwo() {
        BValue[] returns = BRunUtil.invoke(program, "testXMLWithArityTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "0:" + payload + " ");
    }

    @Test
    public void testXMLWithArityChildren() {
        String payload = "0:<p:name xmlns:p=\"foo\">bob</p:name> 1:<p:address xmlns:p=\"foo\">\n" +
                "            <p:city>NY</p:city>\n" +
                "            <q:country xmlns:q=\"bar\">US</q:country>\n" +
                "        </p:address> 2:<q:ID xmlns:q=\"bar\">1131313</q:ID> ";
        BValue[] returns = BRunUtil.invoke(program, "testXMLWithArityChildren");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), payload);
    }
}
