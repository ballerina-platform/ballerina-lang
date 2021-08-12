/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.commons.toml.common;

import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.SeparatedNodeList;
import io.ballerina.toml.syntax.tree.ValueNode;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.commons.toml.visitor.ValueType;

/**
 * Utility class used as a helper for Toml Syntax tree related usage.
 *
 * @since 2.0.0
 */
public class TomlSyntaxTreeUtil {

    //Toml node types.
    public static final String NUMBER = "Number";
    public static final String STRING = "String";
    public static final String BOOLEAN = "Boolean";
    public static final String ARRAY = "Array";
    public static final String TABLE_ARRAY = "Table Array";
    public static final String TABLE = "Table";

    private TomlSyntaxTreeUtil() {

    }

    /**
     * Returns the qualified key identifier given a KeyNode.
     * i.e: table1.subtable1.subtable2
     *
     * @param nodeList KeyNode.
     * @return {@link String} qualified Name.
     */
    public static String toQualifiedName(SeparatedNodeList<ValueNode> nodeList) {
        StringBuilder output = new StringBuilder();
        for (ValueNode valueNode : nodeList) {
            String valueString = valueNode.toString().trim();
            output.append(".").append(valueString);
        }
        return output.substring(1).trim();
    }

    /**
     * Check if a given position is within the text range of a given node.
     *
     * @param position
     * @param node
     * @return Whether the position is with in the node.
     */
    public static boolean withinTextRange(int position, NonTerminalNode node) {
        TextRange rangeWithMinutiae = node.textRangeWithMinutiae();
        TextRange textRange = node.textRange();
        TextRange leadingMinutiaeRange = TextRange.from(rangeWithMinutiae.startOffset(),
                textRange.startOffset() - rangeWithMinutiae.startOffset());
        return leadingMinutiaeRange.endOffset() <= position;
    }

    /**
     * Trims and remove white space and '/' from a given string.
     *
     * @param resourcePath string to be trimmed.
     * @return {@link String} trimmed string.
     */
    public static String trimResourcePath(String resourcePath) {
        resourcePath = resourcePath.trim();
        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }
        if (resourcePath.endsWith("/")) {
            resourcePath = resourcePath.substring(0, resourcePath.length() - 1);
        }
        return resourcePath;
    }

    /**
     * Returns the default value given a type.
     *
     * @param type value type.
     * @return {@link String} default value.
     */
    public static String getDefaultValueForType(ValueType type) {
        switch (type) {
            case NUMBER:
                return "0";
            case BOOLEAN:
                return "false";
            case STRING:
            default:
                return "";
        }
    }

}
