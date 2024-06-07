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

import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlSequence;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test cases for XML iteration.
 *
 * @since 0.982.0
 */
public class XMLIterationTest {

    private CompileResult result, negative;

    String[][] authors = new String[][]{{"Giada De Laurentiis"}, {"J. K. Rowling"},
            {"James McGovern", "Per Bothner", "Kurt Cagle", "James Linn", "Vaidyanathan Nagarajan"}, {"Erik T. Ray"}};

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/xml/xml_iteration.bal");
        negative = BCompileUtil.compile("test-src/types/xml/xml_iteration_negative.bal");
    }

    @Test
    public void testNegative() {

        int index = 0;
        BAssertUtil.validateError(negative, index++,
                "invalid list binding pattern: attempted to infer a list type, but found 'xml'",
                13, 13);
        BAssertUtil.validateError(negative, index++, "incompatible types: " +
                "expected 'function (ballerina/lang.xml:0.0.0:ItemType) returns ()', " +
                "found 'function ([int,xml,string]) returns ()'", 18, 19);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: expected 'other', found 'xml:Element'",
                29, 13);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: 'xml:Element' is not an iterable collection",
                29, 34);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: expected 'record {| xml:Comment value; |}?', found " +
                        "'record {| xml:Element value; |}?'", 33, 54);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: expected 'other', found 'xml:Comment'",
                40, 13);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: 'xml:Comment' is not an iterable collection",
                40, 34);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: expected 'record {| xml:Element value; |}?', found " +
                        "'record {| xml:Comment value; |}?'", 44, 54);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: expected 'other', found 'xml:ProcessingInstruction'",
                51, 13);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: 'xml:ProcessingInstruction' is not an iterable collection",
                51, 48);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: expected 'record {| xml:Comment value; |}?', found " +
                        "'record {| xml:ProcessingInstruction value; |}?'", 55, 49);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: expected '(xml:Element|xml:Text)', found 'xml'",
                59, 34);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: expected '(xml<xml:Element>|xml<xml:Text>)', found 'xml'",
                60, 44);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: expected 'other', found '(xml:Element|xml:Text)'",
                63, 13);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: '(xml:Element|xml:Text)' is not an iterable collection",
                63, 44);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: expected 'other', found '(xml:Element|xml:Text)'",
                68, 13);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: '(xml<xml:Element>|xml<xml:Text>)' is not an iterable collection",
                68, 44);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: expected 'record {| (xml:Element|xml:Text) value; |}?', " +
                        "found 'record {| ballerina/lang.xml:0.0.0:ItemType value; |}?'",
                72, 68);
        BAssertUtil.validateError(negative, index++,
                "xml langlib functions does not support union types as their arguments",
                72, 68);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: expected 'record {| (xml:Element|xml:Text) value; |}?', " +
                        "found 'record {| ballerina/lang.xml:0.0.0:ItemType value; |}?'",
                73, 68);
        BAssertUtil.validateError(negative, index++,
                "xml langlib functions does not support union types as their arguments",
                73, 68);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: 'xml' cannot be constrained with 'xml:Element[]'",
                77, 5);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: 'xml' cannot be constrained with '(xml:Element[] & readonly)'",
                85, 5);
        BAssertUtil.validateError(negative, index++,
                "incompatible types: 'xml' cannot be constrained with '[int,string]'",
                93, 5);
    }

    @Test
    public void testXMLForeach() {
        BRunUtil.invoke(result, "foreachTest");
    }

    @Test
    public void testXMLForeachOp() {
        BRunUtil.invoke(result, "foreachOpTest");
    }

    @Test(dataProvider = "xmlForeachTests")
    public void testXMLTypesForeachOp(String testFunction) {
        BRunUtil.invoke(result, testFunction);
    }

    @DataProvider
    public Object[] xmlForeachTests() {
        return new String[] {
            "testXmlElementSequenceIteration",
            "testXmlTextSequenceIteration",
            "testXmlCommentSequenceIteration",
            "testXmlPISequenceIteration",
            "testXmlUnionSequenceIteration",
            "testXmlSequenceIteration",
            "xmlTypeParamCommentIter",
            "xmlTypeParamElementIter",
            "xmlTypeParamPIIter",
            "testSequenceOfSequenceOfXmlElementIteration",
            "testSequenceOfSequenceOfReadonlyXmlElementIteration",
            "testSequenceOfReadOnlyXmlSubTypeUnionIteration",
            "testSequenceOfReadOnlyXmlSubTypeUnionIteration2"
        };
    }

    @Test
    public void testXMLMapOp() {
        BRunUtil.invoke(result, "mapOpTest");
    }

    @Test
    public void testXMLFilterOp() {
        Object returns = BRunUtil.invoke(result, "filterOpTest");
    }

    @Test
    public void testXMLChainedIterableOps() {
        Object returns = BRunUtil.invoke(result, "chainedIterableOps");

        Assert.assertNotNull(returns);

        List<BXml> childrenList = ((BXmlSequence) returns).getChildrenList();
        Assert.assertEquals(childrenList.get(0).getTextValue(), authors[0][0]);
        Assert.assertEquals(childrenList.get(1).getTextValue(), authors[1][0]);
    }

    @Test(description = "Test iterating over xml elements where some elements are characters")
    public void testXMLCompoundCharacterSequenceIteration() {
        Object results = BRunUtil.invoke(result, "xmlSequenceIter");
        Assert.assertEquals(result.getDiagnostics().length, 0);
        String str = results.toString();
        Assert.assertEquals(str, "<book>the book</book>\nbit of text\\u2702\\u2705\n");
    }

    @Test(description = "Test iterating over xml sequence where all elements are character items")
    public void testXMLCharacterSequenceIteration() {
        Object results = BRunUtil.invoke(result, "xmlCharItemIter");
        Assert.assertEquals(result.getDiagnostics().length, 0);
        String str = results.toString();
        Assert.assertEquals(str, "bit of text\\u2702\\u2705\n");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negative = null;
    }
}
