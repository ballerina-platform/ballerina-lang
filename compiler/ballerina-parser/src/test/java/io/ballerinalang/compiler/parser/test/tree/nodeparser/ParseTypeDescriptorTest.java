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

import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test {@code parseTypeDescriptor} method.
 *
 * @since 2.0.0
 */
public class ParseTypeDescriptorTest {

    // Valid syntax tests

    @Test
    public void testSimpleNameReference() {
        TypeDescriptorNode typeDescriptorNode = NodeParser.parseTypeDescriptor("num");
        Assert.assertEquals(typeDescriptorNode.kind(), SyntaxKind.SIMPLE_NAME_REFERENCE);
        Assert.assertFalse(typeDescriptorNode.hasDiagnostics());
    }

    @Test
    public void testQualifiedIdentifier() {
        TypeDescriptorNode typeDescriptorNode = NodeParser.parseTypeDescriptor("io:println");
        Assert.assertEquals(typeDescriptorNode.kind(), SyntaxKind.QUALIFIED_NAME_REFERENCE);
        Assert.assertFalse(typeDescriptorNode.hasDiagnostics());
    }

    @Test
    public void testRecordTypeDescriptor() {
        String recordTypeDesc = "record {|\n" +
                "    string name;\n" +
                "    boolean married;\n" +
                "|}\n";

        TypeDescriptorNode typeDescriptorNode = NodeParser.parseTypeDescriptor(recordTypeDesc);
        Assert.assertEquals(typeDescriptorNode.kind(), SyntaxKind.RECORD_TYPE_DESC);
        Assert.assertFalse(typeDescriptorNode.hasDiagnostics());
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        TypeDescriptorNode typeDescriptorNode = NodeParser.parseTypeDescriptor("");
        Assert.assertEquals(typeDescriptorNode.kind(), SyntaxKind.SIMPLE_NAME_REFERENCE);
        Assert.assertTrue(typeDescriptorNode.hasDiagnostics());

        Assert.assertEquals(typeDescriptorNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(typeDescriptorNode.trailingInvalidTokens().size(), 0);
        Token name = ((SimpleNameReferenceNode) typeDescriptorNode).name();
        Assert.assertTrue(name.isMissing());
    }

    @Test
    public void testWithRecovery() {
        TypeDescriptorNode typeDescriptorNode = NodeParser.parseTypeDescriptor("% int| ;");
        Assert.assertEquals(typeDescriptorNode.kind(), SyntaxKind.UNION_TYPE_DESC);
        Assert.assertTrue(typeDescriptorNode.hasDiagnostics());

        List<Token> tokens = typeDescriptorNode.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.PERCENT_TOKEN);

        tokens = typeDescriptorNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.SEMICOLON_TOKEN);

        UnionTypeDescriptorNode unionTypeDescriptorNode = (UnionTypeDescriptorNode) typeDescriptorNode;
        SimpleNameReferenceNode simpleNameRef = (SimpleNameReferenceNode) unionTypeDescriptorNode.rightTypeDesc();
        Assert.assertTrue(simpleNameRef.name().isMissing());

        Assert.assertEquals(typeDescriptorNode.toString(), " INVALID[%] int|  MISSING[ INVALID[;]]");
    }
}
