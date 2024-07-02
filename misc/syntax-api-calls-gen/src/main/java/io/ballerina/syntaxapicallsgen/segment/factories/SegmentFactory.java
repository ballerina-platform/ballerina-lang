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

package io.ballerina.syntaxapicallsgen.segment.factories;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.syntaxapicallsgen.segment.CodeSegment;
import io.ballerina.syntaxapicallsgen.segment.NodeFactorySegment;
import io.ballerina.syntaxapicallsgen.segment.StringSegment;
import io.ballerina.syntaxapicallsgen.segment.SyntaxKindSegment;

/**
 * Handles base segment creation through helper methods.
 *
 * @since 2.0.0
 */
public final class SegmentFactory {
    private static final String NULL_LITERAL = "null";

    private SegmentFactory() {
    }

    /**
     * Creates a factory method to create given node type.
     *
     * @param methodName Method name.
     * @return Created segment node.
     */
    public static NodeFactorySegment createNodeFactorySegment(String methodName) {
        return new NodeFactorySegment(methodName);
    }

    /**
     * Creates a factory method to create minutiae.
     *
     * @param methodName Method name.
     * @return Created segment node.
     */
    public static NodeFactorySegment createMinutiaeFactorySegment(String methodName) {
        return new NodeFactorySegment(methodName, true);
    }

    /**
     * Creates a factory method to create given node type with the generic type.
     *
     * @param nodeType    Method name.
     * @param genericType Generic type of the node. (Mostly the type of children)
     * @return Created segment node.
     */
    public static NodeFactorySegment createNodeFactorySegment(String nodeType, String genericType) {
        return new NodeFactorySegment(nodeType, genericType);
    }

    /**
     * Create a basic code segment.
     *
     * @param code Code segment string representation.
     * @return Created segment node.
     */
    public static CodeSegment createCodeSegment(String code) {
        return new CodeSegment(code);
    }

    /**
     * Create a string literal segment.
     *
     * @param string String literal.
     * @return Created segment node.
     */
    public static StringSegment createStringSegment(String string) {
        return new StringSegment(string);
    }

    /**
     * Creates a SyntaxKind enum segment.
     *
     * @param syntaxKind Enum to convert.
     * @return Created segment node.
     */
    public static SyntaxKindSegment createSyntaxKindSegment(SyntaxKind syntaxKind) {
        return new SyntaxKindSegment(syntaxKind);
    }

    /**
     * Helper function to create a null code segment.
     *
     * @return Created null segment node.
     */
    public static CodeSegment createNullSegment() {
        return createCodeSegment(NULL_LITERAL);
    }
}
