/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.parser.test.tree.nodeparser;

import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test {@code parseBindingPattern} method.
 *
 * @since 2.0.0
 */
public class ParseBindingPatternTest {

    // Valid syntax tests

    @Test
    public void testCaptureBindingPattern() {
        BindingPatternNode bindingPatternNode = NodeParser.parseBindingPattern("num");
        Assert.assertEquals(bindingPatternNode.kind(), SyntaxKind.CAPTURE_BINDING_PATTERN);
        Assert.assertFalse(bindingPatternNode.hasDiagnostics());
    }

    @Test
    public void testWildCardBindingPattern() {
        BindingPatternNode bindingPatternNode = NodeParser.parseBindingPattern("_");
        Assert.assertEquals(bindingPatternNode.kind(), SyntaxKind.WILDCARD_BINDING_PATTERN);
        Assert.assertFalse(bindingPatternNode.hasDiagnostics());
    }

    @Test
    public void testListBindingPattern() {
        BindingPatternNode bindingPatternNode = NodeParser.parseBindingPattern("[[s, i], a, b, ...c]");
        Assert.assertEquals(bindingPatternNode.kind(), SyntaxKind.LIST_BINDING_PATTERN);
        Assert.assertFalse(bindingPatternNode.hasDiagnostics());
    }

    @Test
    public void testMappingBindingPattern() {
        BindingPatternNode bindingPatternNode =
                NodeParser.parseBindingPattern("{a: {x: a1, y: {m: a2, n: {p: a3, q: a4}}}}");
        Assert.assertEquals(bindingPatternNode.kind(), SyntaxKind.MAPPING_BINDING_PATTERN);
        Assert.assertFalse(bindingPatternNode.hasDiagnostics());
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        BindingPatternNode bindingPatternNode = NodeParser.parseBindingPattern("");
        Assert.assertEquals(bindingPatternNode.kind(), SyntaxKind.CAPTURE_BINDING_PATTERN);
        Assert.assertTrue(bindingPatternNode.hasDiagnostics());

        Assert.assertEquals(bindingPatternNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(bindingPatternNode.trailingInvalidTokens().size(), 0);
        Token variableName = ((CaptureBindingPatternNode) bindingPatternNode).variableName();
        Assert.assertTrue(variableName.isMissing());
    }

    @Test
    public void testWithAStmt() {
        BindingPatternNode bindingPatternNode = NodeParser.parseBindingPattern("int a = 3;");
        Assert.assertEquals(bindingPatternNode.kind(), SyntaxKind.CAPTURE_BINDING_PATTERN);
        Assert.assertTrue(bindingPatternNode.hasDiagnostics());

        List<Token> tokens = bindingPatternNode.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.INT_KEYWORD);

        tokens = bindingPatternNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 3);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.EQUAL_TOKEN);
        Assert.assertEquals(tokens.get(1).kind(), SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN);
        Assert.assertEquals(tokens.get(2).kind(), SyntaxKind.SEMICOLON_TOKEN);
        Assert.assertEquals(bindingPatternNode.toString(), " INVALID[int] a  INVALID[=]  INVALID[3] INVALID[;]");
    }
}
