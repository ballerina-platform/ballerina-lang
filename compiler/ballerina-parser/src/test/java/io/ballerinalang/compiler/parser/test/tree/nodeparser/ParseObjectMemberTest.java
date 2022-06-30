package io.ballerinalang.compiler.parser.test.tree.nodeparser;

import io.ballerina.compiler.syntax.tree.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ParseObjectMemberTest {

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
}
