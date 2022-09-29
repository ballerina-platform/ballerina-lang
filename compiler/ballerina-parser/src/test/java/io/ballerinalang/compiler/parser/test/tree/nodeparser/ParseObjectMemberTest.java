/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test {@code parseObjectMember} method.
 */
public class ParseObjectMemberTest {

    // Valid syntax tests

    @Test
    public void testRemoteDef() {
        String remoteDef = "remote function bar() {\n" +
                "    int n = 0;\n" +
                "    n += 1;\n" +
                "}";
        Node objectMemberNode = NodeParser.parseObjectMember(remoteDef);
        Assert.assertEquals(objectMemberNode.kind(), SyntaxKind.OBJECT_METHOD_DEFINITION);
        Assert.assertFalse(objectMemberNode.hasDiagnostics());
    }

    @Test
    public void testResourceDef() {
        String resourceDef = "resource function foo . () {\n" +
                "    int n = 0;\n" +
                "    n += 1;\n" +
                "}";
        Node objectMemberNode = NodeParser.parseObjectMember(resourceDef);
        Assert.assertEquals(objectMemberNode.kind(), SyntaxKind.RESOURCE_ACCESSOR_DEFINITION);
        Assert.assertFalse(objectMemberNode.hasDiagnostics());
    }

    @Test
    public void testMethoddef() {
        String methodDef = "function bar() {\n" +
                "    int n = 0;\n" +
                "    n += 1;\n" +
                "}";
        Node objectMemberNode = NodeParser.parseObjectMember(methodDef);
        Assert.assertEquals(objectMemberNode.kind(), SyntaxKind.OBJECT_METHOD_DEFINITION);
        Assert.assertFalse(objectMemberNode.hasDiagnostics());
    }

    @Test
    public void testObjectField() {
        String objectField = "int x = 1;";
        Node objectMemberNode = NodeParser.parseObjectMember(objectField);
        Assert.assertEquals(objectMemberNode.kind(), SyntaxKind.OBJECT_FIELD);
        Assert.assertFalse(objectMemberNode.hasDiagnostics());
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        Node objectMemberNode = NodeParser.parseObjectMember("");
        Assert.assertEquals(objectMemberNode.kind(), SyntaxKind.OBJECT_FIELD);
        Assert.assertTrue(objectMemberNode.hasDiagnostics());

        Assert.assertEquals(objectMemberNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(objectMemberNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(objectMemberNode.toString(), " MISSING[] MISSING[] MISSING[;]");
    }

    @Test
    public void testWithoutResourcePath() {
        String resourceDef = "resource function foo() {}";
        Node objectMemberNode = NodeParser.parseObjectMember(resourceDef);
        Assert.assertEquals(objectMemberNode.kind(), SyntaxKind.RESOURCE_ACCESSOR_DEFINITION);
        Assert.assertTrue(objectMemberNode.hasDiagnostics());

        Assert.assertEquals(objectMemberNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(objectMemberNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(objectMemberNode.toString(), "resource function foo MISSING[.]() {}");
    }

    @Test
    public void testWithInvalidTokens() {
        String resourceDef = "% isolated resource function foo . () {\n" +
                "% int x = 1;\n" +
                "};;";
        Node objectMemberNode = NodeParser.parseObjectMember(resourceDef);
        Assert.assertEquals(objectMemberNode.kind(), SyntaxKind.RESOURCE_ACCESSOR_DEFINITION);
        Assert.assertTrue(objectMemberNode.hasDiagnostics());

        List<Token> tokens = objectMemberNode.leadingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.PERCENT_TOKEN);
        Assert.assertEquals(objectMemberNode.toString(), " INVALID[%] isolated resource function foo . () {\n" +
                " INVALID[%] int x = 1;\n" +
                "}; INVALID[;]");
        tokens = objectMemberNode.trailingInvalidTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0).kind(), SyntaxKind.SEMICOLON_TOKEN);
    }
}
