/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
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
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public abstract class STXMLNameNode extends STNode {

    STXMLNameNode(SyntaxKind kind) {
        super(kind);
    }

    STXMLNameNode(SyntaxKind kind, Collection<STNodeDiagnostic> diagnostics) {
        super(kind, diagnostics);
    }
}
