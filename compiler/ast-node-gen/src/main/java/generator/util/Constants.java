/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package generator.util;

public class Constants {
    public static final String ABSTRACT_KEYWORD = "abstract";
    public static final String PUBLIC_KEYWORD = "public";
    public static final String EXTENDS_KEYWORD = "extends";
    public static final String OPEN_BRACKETS = "(";
    public static final String CLOSE_BRACKETS = ")";
    public static final String OPEN_PARENTHESIS = "{";
    public static final String NEW_LINE = "\n";
    public static final String DOT = ".";
    public static final String SEMI_COLON = ";";
    public static final String CLOSE_PARENTHESIS = "}";
    public static final String LESS_THAN_SYMBOL = "<";
    public static final String GREATER_THAN_SYMBOL = ">";
    public static final String ASSIGNMENT_OPERATOR = "=";
    public static final String WHITE_SPACE = " ";
    public static final String PARAMETER_SEPARATOR = ",";
    public static final String NULL_KEYWORD = "null";
    public static final String JAVA_EXT = "java";
    public static final String TO_STRING_SIGNATURE = "toString";
    public static final String RETURN_KEYWORD = "return";
    public static final String STRING_KEYWORD = "String";
    public static final String CONCAT_SYMBOL = "+";
    public static final String LIST_KEYWORD = "List";
    public static final String MISSING_TOKEN = "STMissingToken";
    public static final String LEADING_TRIVIA = "leadingTrivia";
    public static final String TRAILING_TRIVIA = "trailingTrivia";
    public static final String SYNTAX_TRIVIA = "SyntaxTrivia";
    public static final String ST_TOKEN = "STToken";
    public static final String NON_TERMINAL_NODE = "NonTerminalNode";
    public static final String PROPERTY = "text";
    public static final String KIND_PROPERTY = "kind.strValue";
    public static final String TOKEN = "Token";
    public static final String NODE_LIST = "NodeList<Node>";
    public static final String NODE = "Node";
    public static final String ST_NODE = "STNode";
    public static final String SYNTAX_LIST = "SyntaxList";
    public static final String FACADE_PACKAGE = "package generated.facade;";
    public static final String INTERNAL_PACKAGE = "package generated.internal;";
    public static final String INTERNAL_IMPORTS = "import generated.facade.*;";
    public static final String FACADE_IMPORTS = "import generated.internal.STNode;";

    public static final String IMPORTS_PLACEHOLDER = "imports";
    public static final String CLASSNAME_PLACEHOLDER = "className";
    public static final String RELATIONSHIP_PLACEHOLDER = "relationship";
    public static final String ABSTRACT_PLACEHOLDER = "abstract";
    public static final String PARENT_CLASS_PLACEHOLDER = "parentClass";
    public static final String IMMEDIATE_PARENT_PLACEHOLDER = "immediateParent";
    public static final String NODE_VARIABLE_PLACEHOLDER = "$variable";
    public static final String PACKAGE_PLACEHOLDER = "package";
}
