package io.ballerinalang.compiler.parser.test.tree.nodeparser;

import io.ballerina.compiler.syntax.tree.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ParseServiceMemberDeclarationTest {

    @Test
    public void testRemoteDef() {
        String remoteDef = "remote function bar() {\n" +
                "    int n = 0;\n" +
                "    n += 1;\n" +
                "}";
        Node serviceMemberDeclNode = NodeParser.parseObjectMember(remoteDef);
        Assert.assertEquals(serviceMemberDeclNode.kind(), SyntaxKind.OBJECT_METHOD_DEFINITION);
        Assert.assertFalse(serviceMemberDeclNode.hasDiagnostics());
    }

    @Test
    public void testResourceDef() {
        String resourceDef = "resource function foo . () {\n" +
                "    int n = 0;\n" +
                "    n += 1;\n" +
                "}";
        Node serviceMemberDeclNode = NodeParser.parseObjectMember(resourceDef);
        Assert.assertEquals(serviceMemberDeclNode.kind(), SyntaxKind.RESOURCE_ACCESSOR_DEFINITION);
        Assert.assertFalse(serviceMemberDeclNode.hasDiagnostics());
    }

    @Test
    public void testMethoddef() {
        String methodDef = "function bar() {\n" +
                "    int n = 0;\n" +
                "    n += 1;\n" +
                "}";
        Node serviceMemberDeclNode = NodeParser.parseObjectMember(methodDef);
        Assert.assertEquals(serviceMemberDeclNode.kind(), SyntaxKind.OBJECT_METHOD_DEFINITION);
        Assert.assertFalse(serviceMemberDeclNode.hasDiagnostics());
    }
}
