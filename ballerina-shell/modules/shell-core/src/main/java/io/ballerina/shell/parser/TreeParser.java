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

package io.ballerina.shell.parser;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.shell.DiagnosticReporter;
import io.ballerina.shell.exceptions.TreeParserException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * In this stage the correct syntax tree is identified.
 * The root node of the syntax tree must be the corresponding
 * type for the statement.
 * For an example, for a import declaration,
 * the tree that is parsed should have
 * {@code ImportDeclarationNode} as the root node.
 *
 * @since 2.0.0
 */
public abstract class TreeParser extends DiagnosticReporter {
    /**
     * Parses a string into Nodes.
     *
     * @param source Input source code statements.
     * @return Syntax tree for the source code.
     */
    public Collection<Node> parseString(String source) throws TreeParserException {
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(parse(source));
        return nodes;
    }

    /**
     * Parses a source code string into a Node.
     *
     * @param statement Input source code statement.
     * @return Syntax tree for the source code.
     */
    public abstract Collection<Node> parse(String statement) throws TreeParserException;

    /**
     * Parses a source code entirely.
     * Input source code is expected to only have declarations.
     *
     * @param source Source to parse.
     * @return Parsed declaration nodes.
     */
    public abstract Collection<Node> parseDeclarations(String source) throws TreeParserException;
}
