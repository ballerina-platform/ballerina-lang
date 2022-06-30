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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test {@code parseObjectMember} method.
 *
 */
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
