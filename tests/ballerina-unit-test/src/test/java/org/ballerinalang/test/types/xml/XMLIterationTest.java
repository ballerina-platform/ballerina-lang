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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for XML iteration.
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
        BAssertUtil.validateError(negative, index++, "too many variables are defined for iterable type 'xml'", 11, 24);
        BAssertUtil.validateError(negative, index++, "too many variables are defined for iterable type 'xml'", 16, 19);
    }

    @Test
    public void testXMLForeach() {
        BValue[] returns = BRunUtil.invoke(result, "foreachTest");

        Assert.assertNotNull(returns);

        for (int i = 0; i < returns.length; i++) {
            BRefValueArray tuple = (BRefValueArray) returns[i];
            Assert.assertEquals(((BInteger) tuple.get(0)).intValue(), i);
            Assert.assertEquals(tuple.get(1).stringValue(), titles[i]);
        }
    }

    // Disabled due to https://github.com/ballerina-platform/ballerina-lang/issues/10149
    @Test(enabled = false)
    public void testXMLForeachOp() {
        String[] titles = new String[]{"Everyday Italian", "Harry Potter", "XQuery Kick Start", "Learning XML"};
        BValue[] returns = BRunUtil.invoke(result, "foreachOpTest");

        Assert.assertNotNull(returns);

        for (int i = 0; i < returns.length; i++) {
            BRefValueArray tuple = (BRefValueArray) returns[i];
            Assert.assertEquals(((BInteger) tuple.get(0)).intValue(), i);
            Assert.assertEquals(tuple.get(1).stringValue(), titles[i]);
        }
    }

    @Test
    public void testXMLMapOp() {
        BValue[] returns = BRunUtil.invoke(result, "mapOpTest");

        Assert.assertNotNull(returns);

        for (int i = 0; i < returns.length; i++) {
            BRefValueArray retAuthors = ((BXMLSequence) returns[i]).value();
            long size = retAuthors.size();

            for (int j = 0; j < size; j++) {
                Assert.assertEquals(((BXMLItem) retAuthors.get(j)).getTextValue().stringValue(), authors[i][j]);
            }
        }
    }

    @Test
    public void testXMLFilterOp() {
        BValue[] returns = BRunUtil.invoke(result, "filterOpTest");

        Assert.assertNotNull(returns);

        Assert.assertEquals(((BXMLItem) returns[0]).children().getItem(1).getTextValue().stringValue(), titles[0]);
        Assert.assertEquals(((BXMLItem) returns[0]).children().getItem(3).getTextValue().stringValue(), authors[0][0]);
        Assert.assertEquals(((BXMLItem) returns[0]).children().getItem(5).getTextValue().intValue(), 2005);
        Assert.assertEquals(((BXMLItem) returns[0]).children().getItem(7).getTextValue().floatValue(), 30.00);

        Assert.assertEquals(((BXMLItem) returns[1]).children().getItem(1).getTextValue().stringValue(), titles[1]);
        Assert.assertEquals(((BXMLItem) returns[1]).children().getItem(3).getTextValue().stringValue(), authors[1][0]);
        Assert.assertEquals(((BXMLItem) returns[1]).children().getItem(5).getTextValue().intValue(), 2005);
        Assert.assertEquals(((BXMLItem) returns[1]).children().getItem(7).getTextValue().floatValue(), 29.99);
    }

    @Test
    public void testXMLChainedIterableOps() {
        BValue[] returns = BRunUtil.invoke(result, "chainedIterableOps");

        Assert.assertNotNull(returns);

        BRefValueArray retAuthors = ((BXMLSequence) returns[0]).value();
        Assert.assertEquals(((BXMLItem) retAuthors.get(0)).getTextValue().stringValue(), authors[0][0]);

        retAuthors = ((BXMLSequence) returns[1]).value();
        Assert.assertEquals(((BXMLItem) retAuthors.get(0)).getTextValue().stringValue(), authors[1][0]);
    }
}
