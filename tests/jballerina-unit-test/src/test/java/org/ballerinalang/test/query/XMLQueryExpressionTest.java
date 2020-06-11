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

import org.ballerinalang.model.values.BValue;
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

    @Test(description = "Test simple query expression for XMLs - #2")
    public void testSimpleQueryExprForXML2() {
        BValue[] returnValues = BRunUtil.invoke(result, "testSimpleQueryExprForXML2");
        Assert.assertNotNull(returnValues);

        Assert.assertEquals(returnValues[0].stringValue(),
                "<book>the book</book>bit of text✂✅");
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
}
