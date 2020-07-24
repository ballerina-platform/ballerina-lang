/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.xml;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BRefType;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.model.values.BXML;
import org.ballerinalang.core.model.values.BXMLItem;
import org.ballerinalang.core.model.values.BXMLSequence;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.StringJoiner;

/**
 * Test cases for XML iteration.
 *
 * @since 0.982.0
 */
public class XMLIterationTest {

    private CompileResult result, negative;

    String[][] authors = new String[][]{{"Giada De Laurentiis"}, {"J. K. Rowling"},
            {"James McGovern", "Per Bothner", "Kurt Cagle", "James Linn", "Vaidyanathan Nagarajan"}, {"Erik T. Ray"}};
    String[] titles = new String[]{"Everyday Italian", "Harry Potter", "XQuery Kick Start", "Learning XML"};

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/xml/xml_iteration.bal");
        negative = BCompileUtil.compile("test-src/types/xml/xml_iteration_negative.bal");
    }

    @Test
    public void testNegative() {
        Assert.assertEquals(negative.getErrorCount(), 2);
        int index = 0;
        BAssertUtil.validateError(negative, index++,
                                  "invalid tuple binding pattern: expected a tuple type, but found '(xml|string)'",
                                  11, 17);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected " +
                "'function ((xml:Element|xml:Comment|xml:ProcessingInstruction|xml:Text)) returns ()'," +
                " found 'function ([int,xml,string]) returns ()'", 16, 19);
    }

    @Test
    public void testXMLForeach() {
        BValue[] returns = BRunUtil.invoke(result, "foreachTest");

        Assert.assertNotNull(returns);

        for (int i = 0; i < returns.length; i++) {
            BValueArray tuple = (BValueArray) returns[i];
            Assert.assertEquals(((BInteger) tuple.getRefValue(0)).intValue(), i);
            Assert.assertEquals(tuple.getRefValue(1).stringValue(), titles[i]);
        }
    }

    @Test
    public void testXMLForeachOp() {
        String[] titles = new String[]{"Everyday Italian", "Harry Potter", "XQuery Kick Start", "Learning XML"};
        BValue[] returns = BRunUtil.invoke(result, "foreachOpTest");

        Assert.assertNotNull(returns);

        for (int i = 0; i < returns.length; i++) {
            BValueArray tuple = (BValueArray) returns[i];
            Assert.assertEquals(((BInteger) tuple.getRefValue(0)).intValue(), i);
            Assert.assertEquals(tuple.getRefValue(1).stringValue(), titles[i]);
        }
    }

    @Test
    public void testXMLMapOp() {
        BValue[] returns = BRunUtil.invoke(result, "mapOpTest");

        Assert.assertNotNull(returns);

        for (int i = 0; i < returns.length; i++) {
            BValueArray retAuthors = ((BXMLSequence) returns[i]).value();
            long size = retAuthors.size();

            for (int j = 0; j < size; j++) {
                String actual = ((BXML<?>) retAuthors.getRefValue(j)).getTextValue().stringValue();
                Assert.assertEquals(actual, concat(authors[j]));
            }
        }
    }

    private String concat(Object [] array) {
        StringJoiner j = new StringJoiner("");
        for (Object elem : array) {
            j.add(elem.toString());
        }
        return j.toString();
    }

    @Test
    public void testXMLFilterOp() {
        BValue[] returns = BRunUtil.invoke(result, "filterOpTest");

        Assert.assertNotNull(returns);
        BRefType<?>[] values = ((BXMLSequence) returns[0]).value().getValues();

        Assert.assertEquals(((BXMLItem) values[0]).children().getItem(1).getTextValue().stringValue(), titles[0]);
        Assert.assertEquals(((BXMLItem) values[0]).children().getItem(3).getTextValue().stringValue(), authors[0][0]);
        Assert.assertEquals(((BXMLItem) values[0]).children().getItem(5).getTextValue().intValue(), 2005);
        Assert.assertEquals(((BXMLItem) values[0]).children().getItem(7).getTextValue().floatValue(), 30.00);

        Assert.assertEquals(((BXMLItem) values[1]).children().getItem(1).getTextValue().stringValue(), titles[1]);
        Assert.assertEquals(((BXMLItem) values[1]).children().getItem(3).getTextValue().stringValue(), authors[1][0]);
        Assert.assertEquals(((BXMLItem) values[1]).children().getItem(5).getTextValue().intValue(), 2005);
        Assert.assertEquals(((BXMLItem) values[1]).children().getItem(7).getTextValue().floatValue(), 29.99);
    }

    @Test
    public void testXMLChainedIterableOps() {
        BValue[] returns = BRunUtil.invoke(result, "chainedIterableOps");

        Assert.assertNotNull(returns);

        BValueArray resArray = ((BXMLSequence) returns[0]).value();
        Assert.assertEquals(((BXMLSequence) resArray.getRefValue(0)).getTextValue().stringValue(), authors[0][0]);
        Assert.assertEquals(((BXMLSequence) resArray.getRefValue(1)).getTextValue().stringValue(), authors[1][0]);
    }

    @Test(groups = { "disableOnOldParser" },
            description = "Test iterating over xml elements where some elements are characters")
    public void testXMLCompoundCharacterSequenceIteration() {
        BValue[] results = BRunUtil.invoke(result, "xmlSequenceIter");
        Assert.assertEquals(result.getDiagnostics().length, 0);
        String str = results[0].stringValue();
        Assert.assertEquals(str, "<book>the book</book>\nbit of text\\u2702\\u2705\n");
    }

    @Test(groups = { "disableOnOldParser" },
            description = "Test iterating over xml sequence where all elements are character items")
    public void testXMLCharacterSequenceIteration() {
        BValue[] results = BRunUtil.invoke(result, "xmlCharItemIter");
        Assert.assertEquals(result.getDiagnostics().length, 0);
        String str = results[0].stringValue();
        Assert.assertEquals(str, "bit of text\\u2702\\u2705\n");
    }
}
