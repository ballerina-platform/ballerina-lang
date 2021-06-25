/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.syntaxapicallsgen.test.unit;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.syntaxapicallsgen.segment.CodeSegment;
import io.ballerina.syntaxapicallsgen.segment.NodeFactorySegment;
import io.ballerina.syntaxapicallsgen.segment.StringSegment;
import io.ballerina.syntaxapicallsgen.segment.SyntaxKindSegment;
import io.ballerina.syntaxapicallsgen.segment.factories.SegmentFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test Segment creation factory methods.
 *
 * @since 2.0.0
 */
public class SegmentTest {
    @Test
    public void codeSegmentTest() {
        Assert.assertEquals(new CodeSegment("code").toString(), "code");
        Assert.assertEquals(new CodeSegment("Class.method").toString(), "Class.method");
        Assert.assertEquals(new CodeSegment("").toString(), "");
        Assert.assertEquals(new CodeSegment("(ABC) PQR").toString(), "(ABC) PQR");
        Assert.assertEquals(new CodeSegment("null").toString(), "null");
    }

    @Test
    public void syntaxKindSegmentTest() {
        Assert.assertEquals(new SyntaxKindSegment(SyntaxKind.VAR_KEYWORD).toString(),
                "SyntaxKind.VAR_KEYWORD");
        Assert.assertEquals(new SyntaxKindSegment(SyntaxKind.ACTION_STATEMENT).toString(),
                "SyntaxKind.ACTION_STATEMENT");
        Assert.assertEquals(new SyntaxKindSegment(SyntaxKind.DISTINCT_KEYWORD).toString(),
                "SyntaxKind.DISTINCT_KEYWORD");
        Assert.assertEquals(new SyntaxKindSegment(SyntaxKind.SEMICOLON_TOKEN).toString(),
                "SyntaxKind.SEMICOLON_TOKEN");
        Assert.assertEquals(new SyntaxKindSegment(SyntaxKind.ROLLBACK_KEYWORD).toString(),
                "SyntaxKind.ROLLBACK_KEYWORD");
        Assert.assertEquals(new SyntaxKindSegment(SyntaxKind.XML_ELEMENT).toString(),
                "SyntaxKind.XML_ELEMENT");
    }

    @Test
    public void stringSegmentTest() {
        Assert.assertEquals(new StringSegment("").toString(), "\"\"");
        Assert.assertEquals(new StringSegment("Hello").toString(), "\"Hello\"");
        Assert.assertEquals(new StringSegment("\"Hello\"").toString(), "\"\\\"Hello\\\"\"");
        Assert.assertEquals(new StringSegment("A\nB").toString(), "\"A\\nB\"");
    }

    @Test
    public void nodeFactorySegmentTest1() {
        String expected = "NodeFactory.createNodeList(" +
                "NodeFactory.createToken(SyntaxKind.PUBLIC_KEYWORD," +
                "NodeFactory.createEmptyMinutiaeList()," +
                "NodeFactory.createMinutiaeList(" +
                "NodeFactory.createWhitespaceMinutiae(\" \")" +
                ")" +
                ")" +
                ")";

        NodeFactorySegment createWhitespaceMinutiae =
                SegmentFactory.createNodeFactorySegment("createWhitespaceMinutiae");
        createWhitespaceMinutiae.addParameter(SegmentFactory.createStringSegment(" "));
        NodeFactorySegment createMinutiaeList =
                SegmentFactory.createNodeFactorySegment("createMinutiaeList");
        createMinutiaeList.addParameter(createWhitespaceMinutiae);
        NodeFactorySegment createToken = SegmentFactory.createNodeFactorySegment("createToken");
        createToken.addParameter(SegmentFactory.createSyntaxKindSegment(SyntaxKind.PUBLIC_KEYWORD));
        createToken.addParameter(SegmentFactory.createNodeFactorySegment("createEmptyMinutiaeList"));
        createToken.addParameter(createMinutiaeList);
        NodeFactorySegment createNodeList = SegmentFactory.createNodeFactorySegment("createNodeList");
        createNodeList.addParameter(createToken);

        Assert.assertEquals(createNodeList.toString(), expected);
    }

    @Test
    public void nodeFactorySegmentTest2() {
        String expected = "NodeFactory.createABCNode(" +
                "null," +
                "NodeFactory.createTypedBindingPatternNode(" +
                "NodeFactory.createSimpleNameReferenceNode(" +
                "NodeFactory.createToken(SyntaxKind.VAR_KEYWORD)" +
                ")," +
                "NodeFactory.createCaptureBindingPatternNode(" +
                "NodeFactory.createIdentifierToken(\"x\")" +
                ")" +
                ")" +
                ")";

        NodeFactorySegment createIdentifierToken =
                SegmentFactory.createNodeFactorySegment("createIdentifierToken");
        createIdentifierToken.addParameter(SegmentFactory.createStringSegment("x"));
        NodeFactorySegment createCaptureBindingPatternNode =
                SegmentFactory.createNodeFactorySegment("createCaptureBindingPatternNode");
        createCaptureBindingPatternNode.addParameter(createIdentifierToken);
        NodeFactorySegment createToken = SegmentFactory.createNodeFactorySegment("createToken");
        createToken.addParameter(SegmentFactory.createSyntaxKindSegment(SyntaxKind.VAR_KEYWORD));
        NodeFactorySegment createSimpleNameReferenceNode =
                SegmentFactory.createNodeFactorySegment("createSimpleNameReferenceNode");
        createSimpleNameReferenceNode.addParameter(createToken);
        NodeFactorySegment createTypedBindingPatternNode =
                SegmentFactory.createNodeFactorySegment("createTypedBindingPatternNode");
        createTypedBindingPatternNode.addParameter(createSimpleNameReferenceNode);
        createTypedBindingPatternNode.addParameter(createCaptureBindingPatternNode);
        NodeFactorySegment abcNode = SegmentFactory.createNodeFactorySegment("createABCNode");
        abcNode.addParameter(SegmentFactory.createCodeSegment("null"));
        abcNode.addParameter(createTypedBindingPatternNode);

        Assert.assertEquals(abcNode.toString(), expected);
    }
}
