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

import io.ballerina.compiler.syntax.tree.DocumentationLineToken;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.syntaxapicallsgen.segment.NodeFactorySegment;

/**
 * Handles {@link Token}(Leaf Nodes) to {@link NodeFactorySegment} conversion.
 *
 * @since 2.0.0
 */
public class TokenSegmentFactory {
    private static final String CREATE_LITERAL_METHOD_NAME = "createLiteralValueToken";
    private static final String CREATE_IDENTIFIER_METHOD_NAME = "createIdentifierToken";
    private static final String CREATE_DOC_LINE_METHOD_NAME = "createDocumentationLineToken";
    private static final String CREATE_TOKEN_METHOD_NAME = "createToken";

    private final boolean ignoreMinutiae;

    public TokenSegmentFactory(boolean ignoreMinutiae) {
        this.ignoreMinutiae = ignoreMinutiae;
    }

    /**
     * Converts Token to Segment.
     * Handles minutia of the token as well.
     *
     * @param token Token node to convert.
     * @return Created segment.
     */
    public NodeFactorySegment createTokenSegment(Token token) {
        // Decide on the method and add all parameters required, except for minutiae parameters.
        // If there are no minutiae and the token constructor supports calling without minutiae, use that call.
        NodeFactorySegment root;

        // Decide on factory call and add parameters(except minutiae)
        boolean canSkipMinutiae = false;
        if (token instanceof LiteralValueToken) {
            root = SegmentFactory.createNodeFactorySegment(CREATE_LITERAL_METHOD_NAME);
            root.addParameter(SegmentFactory.createSyntaxKindSegment(token.kind()));
            root.addParameter(SegmentFactory.createStringSegment(token.text()));
        } else if (token instanceof IdentifierToken) {
            root = SegmentFactory.createNodeFactorySegment(CREATE_IDENTIFIER_METHOD_NAME);
            root.addParameter(SegmentFactory.createStringSegment(token.text()));
            canSkipMinutiae = true;
        } else if (token instanceof DocumentationLineToken) {
            root = SegmentFactory.createNodeFactorySegment(CREATE_DOC_LINE_METHOD_NAME);
            root.addParameter(SegmentFactory.createStringSegment(token.text()));
        } else {
            root = SegmentFactory.createNodeFactorySegment(CREATE_TOKEN_METHOD_NAME);
            root.addParameter(SegmentFactory.createSyntaxKindSegment(token.kind()));
            canSkipMinutiae = true;
        }

        // If minutiae can be skipped, don't add them. Don't add if ignore flag is set.
        boolean hasNoMinutiae = token.leadingMinutiae().isEmpty() && token.trailingMinutiae().isEmpty();
        if (canSkipMinutiae && (ignoreMinutiae || hasNoMinutiae)) {
            return root;
        }

        if (ignoreMinutiae) {
            // If ignored bu cannot skip, add empty minutiae to obey ignore flag
            root.addParameter(MinutiaeSegmentFactory.createMinutiaeListSegment(NodeFactory.createEmptyMinutiaeList()));
            root.addParameter(MinutiaeSegmentFactory.createMinutiaeListSegment(NodeFactory.createEmptyMinutiaeList()));
            return root;
        }
        // Add leading and trailing minutiae parameters to the call.
        root.addParameter(MinutiaeSegmentFactory.createMinutiaeListSegment(token.leadingMinutiae()));
        root.addParameter(MinutiaeSegmentFactory.createMinutiaeListSegment(token.trailingMinutiae()));
        return root;
    }
}
