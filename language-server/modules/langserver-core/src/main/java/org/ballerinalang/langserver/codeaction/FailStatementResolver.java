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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.DoStatementNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.LockStatementNode;
import io.ballerina.compiler.syntax.tree.MatchStatementNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LineRange;

import java.util.Optional;

/**
 * This visitor is used to resolve the regular compound statement node of a given node.
 * 
 * @since 2.0.0
 */
public class FailStatementResolver extends NodeTransformer<Optional<Node>> {
    
    private Diagnostic diagnostic;
    
    public FailStatementResolver(Diagnostic diagnostic) {
        this.diagnostic = diagnostic;
    }
    
    public Optional<Node> getRegularCompoundStatementNode(Node node) {
        return node.apply(this);
    }

    @Override
    public Optional<Node> transform(WhileStatementNode whileStatementNode) {
        LineRange diagnosticLineRange = diagnostic.location().lineRange();
        LineRange whileStmtConLineRange = whileStatementNode.condition().lineRange();
        if (diagnosticLineRange.startLine().line() == whileStmtConLineRange.startLine().line()
                && diagnosticLineRange.startLine().offset() > whileStmtConLineRange.startLine().offset() 
                && diagnosticLineRange.endLine().offset() < whileStmtConLineRange.endLine().offset()) {
            return Optional.of(whileStatementNode.whileKeyword());
        }
        return Optional.of(whileStatementNode);
    }
    
    @Override
    public Optional<Node> transform(DoStatementNode doStatementNode) {
        return Optional.of(doStatementNode);
    }
    
    @Override
    public Optional<Node> transform(MatchStatementNode matchStatementNode) {
        return Optional.of(matchStatementNode);
    }
    
    @Override
    public Optional<Node> transform(ForEachStatementNode forEachStatementNode) {
        return Optional.of(forEachStatementNode);
    }
    
    @Override
    public Optional<Node> transform(LockStatementNode lockStatementNode) {
        return Optional.of(lockStatementNode);
    }
    
    @Override
    protected Optional<Node> transformSyntaxNode(Node node) {
        if (node.parent() != null) {
            return node.parent().apply(this);
        }
        return Optional.empty();
    }
}
