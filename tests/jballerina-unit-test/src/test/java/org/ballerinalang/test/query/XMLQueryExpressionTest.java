/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.query;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test query expression with xml result.
 *
 * @since 1.3.0
 */
public class XMLQueryExpressionTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/xml-query-expression.bal");
    }

    @Test(description = "Test simple query expression for XMLs - #1")
    public void testSimpleQueryExprForXML() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<name>Sherlock Holmes</name><name>The Da Vinci Code</name>");
    }

    @Test(groups = { "disableOnOldParser" }, description = "Test simple query expression for XMLs - #2")
    public void testSimpleQueryExprForXML2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXML2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<book>the book</book>bit of text\\u2702\\u2705");
    }

    @Test(description = "Test simple query expression for XMLs - #3")
    public void testSimpleQueryExprForXML3() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXML3");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<price>30.00</price><price>29.99</price><price>49.99</price><price>39.95</price>");
    }

    @Test(description = "Test simple query expression with limit clause for XMLs")
    public void testQueryExprWithLimitForXML() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithLimitForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<author>Sir Arthur Conan Doyle</author><author>Dan Brown</author>");
    }

    @Test(description = "Test simple query expression with where, let clauses for XMLs")
    public void testQueryExprWithWhereLetClausesForXML() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithWhereLetClausesForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<author>Enid Blyton</author>");
    }

    @Test(description = "Test simple query expression with multiple from clauses for XMLs")
    public void testQueryExprWithMultipleFromClausesForXML() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithMultipleFromClausesForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<name>Sir Arthur Conan Doyle</name><name>Dan Brown</name>" +
                        "<name>Sir Arthur Conan Doyle</name><name>Dan Brown</name>");
    }

    @Test(description = "Test simple query expression for xml? - #1")
    public void testSimpleQueryExprForXMLOrNilResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXMLOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<name>Sherlock Holmes</name><name>The Da Vinci Code</name>");
    }

    @Test(groups = { "disableOnOldParser" }, description = "Test simple query expression for xml? - #2")
    public void testSimpleQueryExprForXMLOrNilResult2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXMLOrNilResult2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<book>the book</book>bit of text\\u2702\\u2705");
    }

    @Test(description = "Test simple query expression for xml? - #3")
    public void testSimpleQueryExprForXMLOrNilResult3() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXMLOrNilResult3");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<price>30.00</price><price>29.99</price><price>49.99</price><price>39.95</price>");
    }

    @Test(description = "Test simple query expression with limit clause for xml?")
    public void testQueryExprWithLimitForXMLOrNilResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithLimitForXMLOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<author>Sir Arthur Conan Doyle</author><author>Dan Brown</author>");
    }

    @Test(description = "Test simple query expression with where, let clauses for xml?")
    public void testQueryExprWithWhereLetClausesForXMLOrNilResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithWhereLetClausesForXMLOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<author>Enid Blyton</author>");
    }

    @Test(description = "Test simple query expression with multiple from clauses for xml?")
    public void testQueryExprWithMultipleFromClausesForXMLOrNilResult() {
        BValue[] returnValues = BRunUtil.invoke(result, "testQueryExprWithMultipleFromClausesForXMLOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<name>Sir Arthur Conan Doyle</name><name>Dan Brown</name>" +
                        "<name>Sir Arthur Conan Doyle</name><name>Dan Brown</name>");
    }

    @Test(description = "Test simple query expression with var for XML")
    public void testSimpleQueryExprWithVarForXML() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprWithVarForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "<name>Sherlock Holmes</name><name>The Da Vinci " +
                "Code</name>");
    }

    @Test(description = "Test simple query expression with list for XML")
    public void testSimpleQueryExprWithListForXML() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprWithListForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "<name>Sherlock Holmes</name>");
        Assert.assertEquals(returnValues[1].stringValue(), "<name>The Da Vinci Code</name>");
    }

    @Test(description = "Test simple query expression with union type for XML - #1")
    public void testSimpleQueryExprWithUnionTypeForXML() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprWithUnionTypeForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "<name>Sherlock Holmes</name>" +
                "<name>The Da Vinci Code</name>");
    }

    @Test(description = "Test simple query expression with union type for XML - #2")
    public void testSimpleQueryExprWithUnionTypeForXML2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprWithUnionTypeForXML2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(), "<name>Sherlock Holmes</name>");
        Assert.assertEquals(returnValues[1].stringValue(), "<name>The Da Vinci Code</name>");
    }

    @Test(description = "Test simple query expression with a XML Element Literal")
    public void testSimpleQueryExprWithXMLElementLiteral() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprWithXMLElementLiteral");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues[0].stringValue(), "<entry>Value</entry>");
    }

    @Test(description = "Test simple query expression with nested XML Elements")
    public void testSimpleQueryExprWithNestedXMLElements() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprWithNestedXMLElements");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues[0].stringValue(), "<doc> <entry>Value</entry> </doc>");
    }
}
