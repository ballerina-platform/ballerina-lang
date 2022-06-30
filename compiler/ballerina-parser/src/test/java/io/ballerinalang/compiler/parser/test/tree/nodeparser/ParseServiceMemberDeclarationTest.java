package io.ballerinalang.compiler.parser.test.tree.nodeparser;

import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ParseServiceMemberDeclarationTest {

    @Test
    public void testRemoteDef() {
        String serviceDef = "remote function bar() {\n" +
                "    int n = 0;\n" +
                "    n += 1;\n" +
                "}";
        ModuleMemberDeclarationNode serviceMemberDeclNode = NodeParser.parseServiceMemberDeclaration(serviceDef);
        Assert.assertEquals(serviceMemberDeclNode.kind(), SyntaxKind.OBJECT_METHOD_DEFINITION);
        Assert.assertFalse(serviceMemberDeclNode.hasDiagnostics());
    }

    @Test
    public void testResourceDef() {
        String serviceDef = "resource function foo . () {\n" +
                "    int n = 0;\n" +
                "    n += 1;\n" +
                "}";
        ModuleMemberDeclarationNode serviceMemberDeclNode = NodeParser.parseServiceMemberDeclaration(serviceDef);
        Assert.assertEquals(serviceMemberDeclNode.kind(), SyntaxKind.RESOURCE_ACCESSOR_DEFINITION);
        Assert.assertFalse(serviceMemberDeclNode.hasDiagnostics());
    }

    @Test
    public void testMethoddef() {
        String serviceDef = "function bar() {\n" +
                "    int n = 0;\n" +
                "    n += 1;\n" +
                "}";
        ModuleMemberDeclarationNode serviceMemberDeclNode = NodeParser.parseServiceMemberDeclaration(serviceDef);
        Assert.assertEquals(serviceMemberDeclNode.kind(), SyntaxKind.OBJECT_METHOD_DEFINITION);
        Assert.assertFalse(serviceMemberDeclNode.hasDiagnostics());
    }
}
