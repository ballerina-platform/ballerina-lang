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

import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.FieldAccessCompletionResolver;
import org.ballerinalang.langserver.completions.util.ForeachCompletionUtil;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.TypeGuardCompletionUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic Completion provider for field access providers.
 * eg: Optional Field access and Field Access
 *
 * @param <T> Field access node type
 * @since 2.0.0
 */
public abstract class FieldAccessContext<T extends Node> extends AbstractCompletionProvider<T> {

    public FieldAccessContext(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    /**
     * Get the entries for the given field access expression.
     * This particular logic is written in order to capture the chain completion usage as well.
     *
     * @param ctx  language server operation context
     * @param expr expression node to evaluate
     * @return {@link List} of filtered scope entries
     */
    protected List<LSCompletionItem> getEntries(BallerinaCompletionContext ctx, ExpressionNode expr) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        FieldAccessCompletionResolver resolver = new FieldAccessCompletionResolver(ctx);
        Optional<TypeSymbol> typeSymbol = resolver.getTypeSymbol(expr);
        if (typeSymbol.isEmpty()) {
            return completionItems;
        }
        TypeSymbol rawType = CommonUtil.getRawType(typeSymbol.get());
        if (rawType.typeKind() == TypeDescKind.XML) {
            // Fill the xml attribute access expression completions
            completionItems.addAll(this.getXmlAttributeAccessCompletions(ctx));
        }
        /*
        expr being a field access expression and current node at cursor being qualified name reference is valid
        for xml attribute access expression where the attribute name being a qualified name reference.
        In this scenario we don't need to get the entries against the expr. This use case is handled above.
         */
        if (!QNameRefCompletionUtil.onQualifiedNameIdentifier(ctx, ctx.getNodeAtCursor())) {
            List<Symbol> symbolList = resolver.getVisibleEntries(expr);
            //Add typeguard and foreach snippets.
            if (expr.parent().kind() == SyntaxKind.FIELD_ACCESS) {
                FieldAccessExpressionNode fieldAccessExpr = (FieldAccessExpressionNode) expr.parent();
                completionItems.addAll(TypeGuardCompletionUtil.getTypeGuardDestructedItems(
                        ctx, fieldAccessExpr, typeSymbol.get()));
                completionItems.addAll(ForeachCompletionUtil.getForeachCompletionItemsForIterable(ctx,
                        fieldAccessExpr, typeSymbol.get()));
            }
            completionItems.addAll(this.getCompletionItemList(symbolList, ctx));
        }

        if (isMemberAccessAllowed(rawType, expr.parent())) {
            completionItems.add(this.getMemberAccessCompletionItem(ctx, (FieldAccessExpressionNode) expr.parent()));
        }

        return completionItems;
    }

    /**
     * Concrete implementations should do their own sorting.
     *
     * @param context         Completion context
     * @param node            Node for which completion is being provided
     * @param completionItems Completion items to be sorted
     */
    public abstract void sort(BallerinaCompletionContext context, T node, List<LSCompletionItem> completionItems);

    private boolean isMemberAccessAllowed(TypeSymbol typeSymbol, Node parentNode) {
        return parentNode.kind() == SyntaxKind.FIELD_ACCESS && (typeSymbol.typeKind() == TypeDescKind.MAP
                || (typeSymbol.typeKind() == TypeDescKind.RECORD)
                && ((RecordTypeSymbol) typeSymbol).restTypeDescriptor().isPresent());
    }

    private List<LSCompletionItem> getXmlAttributeAccessCompletions(BallerinaCompletionContext context) {
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            /*
            Following contexts are addressed
            eg:
            1). xmlVar.mod1:<cursor>
            2). xmlVar.mod1:b<cursor>
            Should suggest
            - public, string constants of the module 
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
            Predicate<Symbol> predicate = symbol -> symbol.kind() == SymbolKind.CONSTANT
                    && CommonUtil.getRawType(((ConstantSymbol) symbol)
                    .broaderTypeDescriptor()).typeKind() == TypeDescKind.STRING;
            List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(context, qNameRef, predicate);

            return this.getCompletionItemList(moduleContent, context);
        }
        List<LSCompletionItem> completionItems = new ArrayList<>();
        /*
        eg:
            xmlVar.<cursor>
            xmlVar.a<cursor>

        XML attribute access expression
        Suggest the following
        1). xml namespaces defined
        2). modules
         */
        List<Symbol> xmlNamespaces = context.visibleSymbols(context.getCursorPosition()).stream()
                .filter(symbol -> symbol.kind() == SymbolKind.XMLNS)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(xmlNamespaces, context));
        completionItems.addAll(this.getModuleCompletionItems(context));

        return completionItems;
    }

    private LSCompletionItem getMemberAccessCompletionItem(BallerinaCompletionContext context,
                                                           FieldAccessExpressionNode faNode) {
        String insertText = "[\"${1}\"]";
        int dotStart = faNode.dotToken().textRange().startOffset();
        int dotEnd = faNode.dotToken().textRange().endOffset();
        // Here we do not check for the existence of the current document because otherwise it will not reach here
        Range range = PositionUtil.toRange(dotStart, dotEnd, context.currentDocument().get().textDocument());

        TextEdit textEdit = new TextEdit();
        textEdit.setNewText("");
        textEdit.setRange(range);

        CompletionItem completionItem = new CompletionItem();
        completionItem.setInsertText(insertText);
        completionItem.setKind(CompletionItemKind.Snippet);
        completionItem.setDocumentation("Convert to a member access expression");
        completionItem.setLabel("[\"\"]");
        completionItem.setAdditionalTextEdits(Collections.singletonList(textEdit));
        completionItem.setInsertTextFormat(InsertTextFormat.Snippet);

        return new StaticCompletionItem(context, completionItem, StaticCompletionItem.Kind.OTHER);
    }
}
