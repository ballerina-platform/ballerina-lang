/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.parser.test.tree.nodeparser;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test {@code parseAnnotation} method.
 *
 * @since 2201.7.0
 */
public class ParseAnnotationTest {

    // Valid syntax tests

    @Test
    public void testAnnotation() {
        AnnotationNode annotationNode = NodeParser.parseAnnotation("@foo:bar {qux: 5}");
        Assert.assertEquals(annotationNode.kind(), SyntaxKind.ANNOTATION);
        Assert.assertFalse(annotationNode.hasDiagnostics());
    }

    // Invalid syntax tests

    @Test
    public void testEmptyString() {
        AnnotationNode annotationNode = NodeParser.parseAnnotation("");
        Assert.assertEquals(annotationNode.kind(), SyntaxKind.ANNOTATION);
        Assert.assertTrue(annotationNode.hasDiagnostics());

        Assert.assertEquals(annotationNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(annotationNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(annotationNode.toString(), " MISSING[@] MISSING[]");
    }

    @Test
    public void testAnnotationWithAdditionalTokens() {
        AnnotationNode annotationNode = NodeParser.parseAnnotation("% @foo {};");
        Assert.assertEquals(annotationNode.kind(), SyntaxKind.ANNOTATION);
        Assert.assertTrue(annotationNode.hasDiagnostics());

        Assert.assertEquals(annotationNode.leadingInvalidTokens().size(), 1);
        Assert.assertEquals(annotationNode.trailingInvalidTokens().size(), 1);
        Assert.assertEquals(annotationNode.toString(), " INVALID[%] @foo {} INVALID[;]");
    }

    @Test
    public void testAnnotationRecovery() {
        AnnotationNode annotationNode = NodeParser.parseAnnotation("@ {");
        Assert.assertEquals(annotationNode.kind(), SyntaxKind.ANNOTATION);
        Assert.assertTrue(annotationNode.hasDiagnostics());

        Assert.assertEquals(annotationNode.leadingInvalidTokens().size(), 0);
        Assert.assertEquals(annotationNode.trailingInvalidTokens().size(), 0);
        Assert.assertEquals(annotationNode.toString(), "@  MISSING[]{ MISSING[}]");
    }
}
