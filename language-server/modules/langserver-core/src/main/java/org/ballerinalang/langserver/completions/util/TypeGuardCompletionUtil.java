/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util;

import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.builder.TypeGuardCompletionItemBuilder;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Dynamic snippet constructs for typeguards.
 *
 * @since 2.0.0
 */
public class TypeGuardCompletionUtil {

    private static final String TYPE_GUARD_LABEL = "typeguard";

    private TypeGuardCompletionUtil() {
    }

    /**
     * Given a union type variable symbol, returns the typeguard snippet destructuring the
     * member types of the given symbol.
     *
     * @param symbol Uniontype symbol
     * @param ctx    Completion context
     * @return {@link LSCompletionItem} generated completion item
     */
    public static LSCompletionItem getTypeGuardDestructedItem(UnionTypeSymbol symbol,
                                                              BallerinaCompletionContext ctx,
                                                              FieldAccessExpressionNode expr) {
        String symbolName;
        if (expr.expression().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            symbolName = ((SimpleNameReferenceNode) expr.expression()).name().text();
        } else {
            symbolName = expr.expression().toSourceCode().trim();
        }

        List<TypeSymbol> members = new ArrayList<>(symbol.memberTypeDescriptors());
        String detail = "Destructure the variable " + symbolName + " with typeguard";
        String snippet = "";
        snippet += IntStream.range(0, members.size() - 1).mapToObj(value -> {
            TypeSymbol bType = members.get(value);
            String placeHolder = "\t${" + (value + 1) + "}";
            return "if " + symbolName + " is " + NameUtil.getModifiedTypeName(ctx, bType) + " {"
                    + CommonUtil.LINE_SEPARATOR + placeHolder + CommonUtil.LINE_SEPARATOR + "}";
        }).collect(Collectors.joining(" else ")) + " else {" + CommonUtil.LINE_SEPARATOR + "\t${"
                + members.size() + "}" + CommonUtil.LINE_SEPARATOR + "}";

        //create the text edit item to replace the field accession text.
        List<TextEdit> textEdits = new ArrayList<>();
        TextEdit textEdit = new TextEdit();
        textEdit.setNewText("");
        Position start = new Position(expr.lineRange().startLine().line(), expr.lineRange().startLine().offset());
        Position end = new Position(expr.lineRange().endLine().line(), expr.lineRange().endLine().offset());
        textEdit.setRange(new Range(start, end));
        textEdits.add(textEdit);
        CompletionItem completionItem =
                TypeGuardCompletionItemBuilder.build(snippet, TYPE_GUARD_LABEL, detail, textEdits);
        return new StaticCompletionItem(ctx, completionItem, StaticCompletionItem.Kind.OTHER);
    }

    /**
     * Returns Typeguard completion items for union type variable symbols for the given
     * expression node in the given completion context.
     *
     * @param ctx  Completion context
     * @param expr expression node
     * @return List of typeguard completion items
     */
    public static List<LSCompletionItem> getTypeGuardDestructedItems(BallerinaCompletionContext ctx,
                                                                     FieldAccessExpressionNode expr,
                                                                     TypeSymbol typeSymbol) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (TypeGuardCompletionUtil.isInBlockContext(expr)
                && typeSymbol.typeKind() == TypeDescKind.UNION
                && (expr.expression().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE ||
                expr.expression().kind() == SyntaxKind.FUNCTION_CALL ||
                expr.expression().kind() == SyntaxKind.FIELD_ACCESS)) {
            completionItems.add(getTypeGuardDestructedItem((UnionTypeSymbol) typeSymbol, ctx, expr));
        }
        return completionItems;
    }

    /**
     * Check if the given node is within a FunctionBodyNode or BlockStatementNode.
     *
     * @param node Expression node of which the context is checked
     * @return
     */
    public static boolean isInBlockContext(FieldAccessExpressionNode node) {
        return node.parent() instanceof ExpressionStatementNode &&
                (node.parent().parent() instanceof FunctionBodyNode ||
                        node.parent().parent() instanceof BlockStatementNode);
    }
}
