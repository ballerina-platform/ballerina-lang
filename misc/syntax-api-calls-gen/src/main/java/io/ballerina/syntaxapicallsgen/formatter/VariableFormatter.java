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

package io.ballerina.syntaxapicallsgen.formatter;

import io.ballerina.syntaxapicallsgen.SyntaxApiCallsGenException;
import io.ballerina.syntaxapicallsgen.segment.NodeFactorySegment;
import io.ballerina.syntaxapicallsgen.segment.Segment;
import io.ballerina.syntaxapicallsgen.segment.factories.SegmentFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

/**
 * Grouped Variable formatter.
 * Groups minutiae with corresponding node.
 *
 * @since 2.0.0
 */
public class VariableFormatter extends SegmentFormatter {
    private static final String SEMICOLON_CHAR = ";";
    private static final String SPACE_CHAR = " ";
    private static final String EQ_CHAR = " = ";
    private static final String MINUTIAE_LIST_DEF = "MinutiaeList trailingMinutiae, leadingMinutiae;\n\n";
    private static final String LEADING_MINUTIAE = "leadingMinutiae";
    private static final String TRAILING_MINUTIAE = "trailingMinutiae";
    private static final String TOKEN_SUFFIX = "Token";
    private final SegmentFormatter formatter;
    private HashMap<String, Integer> variableCount;

    VariableFormatter() {
        formatter = new NoFormatter();
    }

    @Override
    public String format(Segment segment) {
        if (segment instanceof NodeFactorySegment nodeFactorySegment) {
            variableCount = new HashMap<>();
            return MINUTIAE_LIST_DEF + processNode(nodeFactorySegment);
        }
        throw new SyntaxApiCallsGenException("Expected a valid node segment but fount parsed segment of " + segment);
    }

    /**
     * Processes a token and returns variable name and the content that should come before.
     *
     * @param token Token segment.
     * @return Created content. (First minutiae, then token)
     */
    private NamedContent processToken(NodeFactorySegment token) {
        StringBuilder stringBuilder = new StringBuilder();
        Stack<Segment> params = new Stack<>();
        token.forEach(params::push);

        NamedContent namedContent = new NamedContent(token.getType(), variableCount);
        NodeFactorySegment factorySegment = SegmentFactory.createNodeFactorySegment(token.getMethodName());

        // Define and add minutiae
        boolean minutiaeNodesPresent = false;
        if (params.size() >= 2) {
            Segment lastSegment1 = params.get(params.size() - 1);
            Segment lastSegment2 = params.get(params.size() - 2);
            if (lastSegment1 instanceof NodeFactorySegment
                    && lastSegment2 instanceof NodeFactorySegment
                    && ((NodeFactorySegment) lastSegment1).isMinutiae()
                    && ((NodeFactorySegment) lastSegment2).isMinutiae()) {
                String trailingMinutiae = formatter.format(params.pop());
                String leadingMinutiae = formatter.format(params.pop());
                stringBuilder.append(LEADING_MINUTIAE).append(EQ_CHAR)
                        .append(leadingMinutiae)
                        .append(SEMICOLON_CHAR).append(System.lineSeparator());
                stringBuilder.append(TRAILING_MINUTIAE).append(EQ_CHAR)
                        .append(trailingMinutiae)
                        .append(SEMICOLON_CHAR).append(System.lineSeparator());
                minutiaeNodesPresent = true;
            }
        }

        // Add params and minutiae
        while (!params.isEmpty()) {
            factorySegment.addParameter(params.remove(0));
        }

        if (minutiaeNodesPresent) {
            factorySegment.addParameter(SegmentFactory.createCodeSegment(LEADING_MINUTIAE));
            factorySegment.addParameter(SegmentFactory.createCodeSegment(TRAILING_MINUTIAE));
        }

        stringBuilder
                .append(token.getType()).append(SPACE_CHAR)
                .append(namedContent.name).append(EQ_CHAR)
                .append(factorySegment)
                .append(SEMICOLON_CHAR).append(System.lineSeparator())
                .append(System.lineSeparator());

        namedContent.content = stringBuilder.toString();
        return namedContent;
    }

    /**
     * Processes node and returns variable name and the content that should come before.
     *
     * @param segment Segment to process.
     * @return Created content. (First parameters, then current)
     */
    private NamedContent processNode(NodeFactorySegment segment) {
        // If it is a token, handle accordingly
        if (segment.getMethodName().endsWith(TOKEN_SUFFIX)) {
            return processToken(segment);
        }

        // Get each child and add the content that should come before
        StringBuilder stringBuilder = new StringBuilder();
        NodeFactorySegment factorySegment = segment.createCopy();
        for (Segment child : segment) {
            if (child instanceof NodeFactorySegment childFactoryCall) {
                NamedContent namedContent = processNode(childFactoryCall);
                stringBuilder.append(namedContent.content);
                factorySegment.addParameter(SegmentFactory.createCodeSegment(namedContent.name));
            } else {
                factorySegment.addParameter(child);
            }
        }

        // Node definition
        NamedContent namedContent = new NamedContent(segment.getType(), variableCount);
        stringBuilder
                .append(factorySegment.getType())
                .append(factorySegment.getGenericType())
                .append(SPACE_CHAR).append(namedContent.name)
                .append(EQ_CHAR).append(factorySegment)
                .append(SEMICOLON_CHAR).append(System.lineSeparator())
                .append(System.lineSeparator());
        namedContent.content = stringBuilder.toString();
        return namedContent;
    }

    /**
     * Data structure to hold string with an variable name.
     *
     * @since 2.0.0
     */
    private static class NamedContent {
        final String name;
        String content;

        NamedContent(String type, Map<String, Integer> variableCount) {
            // Find the variable name: var, var1, var2, ...
            String varGenericName = type.substring(0, 1).toLowerCase(Locale.getDefault()) + type.substring(1);
            int varGenericCount = variableCount.getOrDefault(varGenericName, 0);
            this.name = varGenericName + (varGenericCount == 0 ? "" : String.valueOf(varGenericCount));
            variableCount.put(varGenericName, varGenericCount + 1);
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
