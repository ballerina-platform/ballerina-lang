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

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "<name>Sherlock Holmes</name><name>The Da Vinci Code</name>");
    }

    @Test(groups = {"disableOnOldParser"}, description = "Test simple query expression for XMLs - #2")
    public void testSimpleQueryExprForXML2() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXML2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "<book>the book</book>bit of text\\u2702\\u2705");
    }

    @Test(description = "Test simple query expression for XMLs - #3")
    public void testSimpleQueryExprForXML3() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXML3");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "<price>30.00</price><price>29.99</price><price>49.99</price><price>39.95</price>");
    }

    @Test(description = "Test simple query expression with limit clause for XMLs")
    public void testQueryExprWithLimitForXML() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithLimitForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "<author>Sir Arthur Conan Doyle</author><author>Dan Brown</author>");
    }

    @Test(description = "Test simple query expression with where, let clauses for XMLs")
    public void testQueryExprWithWhereLetClausesForXML() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithWhereLetClausesForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "<author>Enid Blyton</author>");
    }

    @Test(description = "Test simple query expression with multiple from clauses for XMLs")
    public void testQueryExprWithMultipleFromClausesForXML() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithMultipleFromClausesForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "<name>Sir Arthur Conan Doyle</name><name>Dan Brown</name>" +
                        "<name>Sir Arthur Conan Doyle</name><name>Dan Brown</name>");
    }

    @Test(description = "Test simple query expression for xml? - #1")
    public void testSimpleQueryExprForXMLOrNilResult() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXMLOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "<name>Sherlock Holmes</name><name>The Da Vinci Code</name>");
    }

    @Test(groups = {"disableOnOldParser"}, description = "Test simple query expression for xml? - #2")
    public void testSimpleQueryExprForXMLOrNilResult2() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXMLOrNilResult2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "<book>the book</book>bit of text\\u2702\\u2705");
    }

    @Test(description = "Test simple query expression for xml? - #3")
    public void testSimpleQueryExprForXMLOrNilResult3() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXMLOrNilResult3");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "<price>30.00</price><price>29.99</price><price>49.99</price><price>39.95</price>");
    }

    @Test(description = "Test simple query expression with limit clause for xml?")
    public void testQueryExprWithLimitForXMLOrNilResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithLimitForXMLOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "<author>Sir Arthur Conan Doyle</author><author>Dan Brown</author>");
    }

    @Test(description = "Test simple query expression with where, let clauses for xml?")
    public void testQueryExprWithWhereLetClausesForXMLOrNilResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithWhereLetClausesForXMLOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "<author>Enid Blyton</author>");
    }

    @Test(description = "Test simple query expression with multiple from clauses for xml?")
    public void testQueryExprWithMultipleFromClausesForXMLOrNilResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExprWithMultipleFromClausesForXMLOrNilResult");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(),
                "<name>Sir Arthur Conan Doyle</name><name>Dan Brown</name>" +
                        "<name>Sir Arthur Conan Doyle</name><name>Dan Brown</name>");
    }

    @Test(description = "Test simple query expression with var for XML")
    public void testSimpleQueryExprWithVarForXML() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryExprWithVarForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "<name>Sherlock Holmes</name><name>The Da Vinci " +
                "Code</name>");
    }

    @Test(description = "Test simple query expression with list for XML")
    public void testSimpleQueryExprWithListForXML() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleQueryExprWithListForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.get(0).toString(), "<name>Sherlock Holmes</name>");
        Assert.assertEquals(returnValues.get(1).toString(), "<name>The Da Vinci Code</name>");
    }

    @Test(description = "Test simple query expression with union type for XML - #1")
    public void testSimpleQueryExprWithUnionTypeForXML() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryExprWithUnionTypeForXML");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.toString(), "<name>Sherlock Holmes</name>" +
                "<name>The Da Vinci Code</name>");
    }

    @Test(description = "Test simple query expression with union type for XML - #2")
    public void testSimpleQueryExprWithUnionTypeForXML2() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleQueryExprWithUnionTypeForXML2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues.get(0).toString(), "<name>Sherlock Holmes</name>");
        Assert.assertEquals(returnValues.get(1).toString(), "<name>The Da Vinci Code</name>");
    }

    @Test(description = "Test simple query expression with a XML Element Literal")
    public void testSimpleQueryExprWithXMLElementLiteral() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryExprWithXMLElementLiteral");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<entry>Value</entry>");
    }

    @Test(description = "Test simple query expression with nested XML Elements")
    public void testSimpleQueryExprWithNestedXMLElements() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryExprWithNestedXMLElements");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<doc> <entry>Value</entry> </doc>");
    }

    @Test(description = "Test query expression iterating over xml in from clause")
    public void testQueryExpressionIteratingOverXMLInFrom() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExpressionIteratingOverXMLInFrom");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<foo>Hello<bar>World</bar></foo>");
    }

    @Test(description = "Test query expression iterating over xml:Text in from clause")
    public void testQueryExpressionIteratingOverXMLTextInFrom() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExpressionIteratingOverXMLTextInFrom");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "hello text");
    }

    @Test(description = "Test query expression iterating over xml<xml:Element> in from clause")
    public void testQueryExpressionIteratingOverXMLElementInFrom() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExpressionIteratingOverXMLElementInFrom");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<foo>Hello<bar>World</bar></foo>");
    }

    @Test(description = "Test query expression iterating over xml<xml:ProcessingInstruction> in from clause")
    public void testQueryExpressionIteratingOverXMLPIInFrom() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExpressionIteratingOverXMLPIInFrom");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>");
    }

    @Test(description = "Test query expression iterating over xml<xml:Element> in from clause with other clauses")
    public void testQueryExpressionIteratingOverXMLWithOtherClauses() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExpressionIteratingOverXMLWithOtherClauses");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<author>Dan Brown</author><author>Enid Blyton</author>");
    }

    @Test(description = "Test query expression iterating over xml<xml:Comment> in from clause with xml or nil result")
    public void testQueryExpressionIteratingOverXMLInFromWithXMLOrNilResult() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExpressionIteratingOverXMLInFromWithXMLOrNilResult");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<!-- this is a comment text -->");
    }

    @Test(description = "Test query expression iterating over xml<xml:Element> in from clause in inner queries")
    public void testQueryExpressionIteratingOverXMLInFromInInnerQueries() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExpressionIteratingOverXMLInFromInInnerQueries");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<author>Enid Blyton</author>" +
                "<author>Sir Arthur Conan Doyle</author><author>Dan Brown</author>");
    }

    @Test(description = "Test XML template with query expression returning xml:Element")
    public void testXMLTemplateWithQueryExpression() {
        Object returnValues = BRunUtil.invoke(result, "testXMLTemplateWithQueryExpression");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<data><person country=\"Germany\">Mike</person>" +
                "<person country=\"France\">Anne</person><person country=\"Russia\">John</person></data>");
    }

    @Test(description = "Test XML template with query expression returning xml:Comment")
    public void testXMLTemplateWithQueryExpression2() {
        Object returnValues = BRunUtil.invoke(result, "testXMLTemplateWithQueryExpression2");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<data>MikeAnneJohn</data>");
    }

    @Test(description = "Test XML template with query expression iterating over a stream returning xml")
    public void testXMLTemplateWithQueryExpression3() {
        Object returnValues = BRunUtil.invoke(result, "testXMLTemplateWithQueryExpression3");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<data><person country=\"Germany\">Mike</person>" +
                "<person country=\"France\">Anne</person><person country=\"Russia\">John</person></data>");
    }

    @Test(description = "Test XML template with query expression iterating over a table returning xml")
    public void testXMLTemplateWithQueryExpression4() {
        Object returnValues = BRunUtil.invoke(result, "testXMLTemplateWithQueryExpression4");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<data><person country=\"Russia\">John</person>" +
                "<person country=\"Germany\">Mike</person></data>");
    }

    @Test(description = "Test XML template with query expression iterating over xml with namespaces")
    public void testQueryExpressionIteratingOverXMLWithNamespaces() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExpressionIteratingOverXMLWithNamespaces");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<fname xmlns:ns=\"foo\" " +
                "ns:status=\"active\">Mike</fname>");
    }

    @Test(description = "Test XML template with query expression iterating over xml with namespaces")
    public void testQueryExpressionIteratingOverTableReturningXML() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExpressionIteratingOverTableReturningXML");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<person country=\"Russia\">John</person>" +
                "<person country=\"Germany\">Mike</person>");
    }

    @Test(description = "Test XML template with query expression iterating over xml with namespaces")
    public void testQueryExpressionIteratingOverStreamReturningXML() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExpressionIteratingOverStreamReturningXML");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.toString(), "<person country=\"Russia\">John</person>" +
                "<person country=\"Germany\">Mike</person>");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
