/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link ObjectFieldNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ObjectFieldNodeContext extends AbstractCompletionProvider<ObjectFieldNode> {

    public ObjectFieldNodeContext() {
        super(ObjectFieldNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ObjectFieldNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (this.onExpressionContext(context, node)) {
            completionItems.addAll(this.getExpressionContextCompletions(context));
        } else if (this.onModuleTypeDescriptorsOnly(context, node)) {
            NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> typesInModule = QNameReferenceUtil.getTypesInModule(context, qNameRef);
            completionItems.addAll(this.getCompletionItemList(typesInModule, context));
        } else {
            /*
            If the cursor is at the following position, we route to the parent since it is a common and ideal place.
            Eg:
            (1). object {
                    i<cursor>
                }
             Return from here, since the sorting will be handled by the parent.
             */
            return CompletionUtil.route(context, node.parent());
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private List<LSCompletionItem> getExpressionContextCompletions(BallerinaCompletionContext ctx) {
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            return this.getCompletionItemList(QNameReferenceUtil.getExpressionContextEntries(ctx, qNameRef), ctx);
        }

        return this.expressionCompletions(ctx);
    }

    private boolean onModuleTypeDescriptorsOnly(BallerinaCompletionContext context, ObjectFieldNode node) {
        int cursor = context.getCursorPositionInTree();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        Optional<Token> qualifier = node.visibilityQualifier();

        return qualifier.isPresent() && qualifier.get().textRange().endOffset() < cursor
                && nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE;
    }

    private boolean onExpressionContext(BallerinaCompletionContext context, ObjectFieldNode node) {
        int cursor = context.getCursorPositionInTree();
        Optional<Token> equalsToken = node.equalsToken();

        return equalsToken.isPresent() && equalsToken.get().textRange().endOffset() <= cursor;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ObjectFieldNode node) {
        /*
        This validation is added in order to avoid identifying the following context as object field node context.
        This is happened due to the parser recovery strategy.
        Eg: type TestType client o<cursor>
         */
        NonTerminalNode parent = node.parent();
        return (parent.kind() == SyntaxKind.CLASS_DEFINITION
                && !((ClassDefinitionNode) parent).openBrace().isMissing()) ||
                (parent.kind() == SyntaxKind.OBJECT_TYPE_DESC
                        && !((ObjectTypeDescriptorNode) parent).openBrace().isMissing()) ||
                (parent.kind() == SyntaxKind.OBJECT_CONSTRUCTOR
                        && !((ObjectConstructorExpressionNode) parent).openBraceToken().isMissing()) ||
                (parent.kind() == SyntaxKind.SERVICE_DECLARATION
                        && !((ServiceDeclarationNode) parent).openBraceToken().isMissing());
    }
}
