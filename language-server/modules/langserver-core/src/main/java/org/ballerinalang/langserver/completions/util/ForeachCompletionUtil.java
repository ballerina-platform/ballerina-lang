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

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.builder.ForeachCompletionItemBuilder;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dynamic foreach snippet construction for iterables.
 *
 * @since 2.0.0
 */
public class ForeachCompletionUtil {

    private static final String VAR_NAME = "item";
    private static final String VAR_TYPE = "var";
    private static final List<TypeDescKind> ITERABLES = Arrays.asList(
            TypeDescKind.STRING, TypeDescKind.ARRAY, TypeDescKind.TUPLE,
            TypeDescKind.MAP, TypeDescKind.RECORD, TypeDescKind.TABLE,
            TypeDescKind.STREAM, TypeDescKind.XML);

    private ForeachCompletionUtil() {
    }

    /**
     * Given an expression node in a particular completion context,
     * validate if the expression is an iterable and returns the corresponding foreach completion item.
     *
     * @param ctx        completion context
     * @param expr       expression node
     * @param typeSymbol type symbol corresponding to the expression node
     * @return a list of {@link LSCompletionItem}.
     */
    public static List<LSCompletionItem> getForeachCompletionItemsForIterable(BallerinaCompletionContext ctx,
                                                                              FieldAccessExpressionNode expr,
                                                                              TypeSymbol typeSymbol) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (!isInBlockContext(expr) || !ITERABLES.contains(CommonUtil.getRawType(typeSymbol).typeKind())
                || !(expr.expression().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE ||
                expr.expression().kind() == SyntaxKind.FUNCTION_CALL)) {
            return completionItems;
        }
        completionItems.add(getForEachCompletionItem(ctx, expr, typeSymbol));
        return completionItems;
    }

    /**
     * Given an iterable field access expression returns the foreach completion item for it.
     *
     * @param symbol {@link TypeSymbol} iterable symbol
     * @param ctx    completion context
     * @param expr   expression node
     * @return {@link LSCompletionItem}
     */
    private static LSCompletionItem getForEachCompletionItem(BallerinaCompletionContext ctx,
                                                             FieldAccessExpressionNode expr,
                                                             TypeSymbol symbol) {
        String symbolName;
        if (expr.expression().kind() == SyntaxKind.FUNCTION_CALL) {
            symbolName = expr.expression().toSourceCode().trim();
            //todo: Trim for arguments as well
        } else {
            symbolName = ((SimpleNameReferenceNode) expr.expression()).name().text();
        }
        String detail = "foreach snippet for iterable variable - " + symbolName;
        String label = "foreach - " + symbolName;
        String type = getTypeOfIteratorVariable(symbol);
        StringBuilder snippet = new StringBuilder("foreach");
        snippet.append(" ").append(type).append(" ")
                .append(CommonUtil.getValidatedSymbolName(ctx, VAR_NAME)).append(" in ").append(symbolName)
                .append(" ").append("{").append(CommonUtil.LINE_SEPARATOR).append("\t${1}")
                .append(CommonUtil.LINE_SEPARATOR).append("}");

        //create the text edit item to replace the field accession text
        List<TextEdit> textEdits = new ArrayList<>();
        TextEdit textEdit = new TextEdit();
        textEdit.setNewText("");
        LineRange lineRange = expr.lineRange();
        Position start = new Position(lineRange.startLine().line(), lineRange.startLine().offset());
        Position end = new Position(lineRange.endLine().line(), lineRange.endLine().offset());
        textEdit.setRange(new Range(start, end));
        textEdits.add(textEdit);
        CompletionItem completionItem =
                ForeachCompletionItemBuilder.build(snippet.toString(), label, detail, textEdits);
        return new StaticCompletionItem(ctx, completionItem, StaticCompletionItem.Kind.OTHER);
    }

    /**
     * Returns the type of the member type of an iterable type symbol.
     *
     * @param symbol iterable type symbol.
     * @return signature of the member type.
     */
    public static String getTypeOfIteratorVariable(TypeSymbol symbol) {

        String type;
        TypeSymbol rawType = CommonUtil.getRawType(symbol);
        switch (rawType.typeKind()) {
            case STRING:
            case XML:
                type = rawType.signature();
                break;
            case ARRAY:
                type = ((ArrayTypeSymbol) rawType).memberTypeDescriptor().signature();
                break;
            case TUPLE:
                List<String> typesSet = new ArrayList<>(((TupleTypeSymbol) rawType).
                        memberTypeDescriptors().stream().map(
                        tSymbol -> tSymbol.typeKind().getName()).collect(Collectors.toSet()));
                if (typesSet.size() == 1) {
                    type = typesSet.get(0);
                } else {
                    type = VAR_TYPE;
                }
                break;
            case MAP:
                type = ((MapTypeSymbol) rawType).typeParam().signature();
                break;
            case TABLE:
                type = ((TableTypeSymbol) rawType).rowTypeParameter().signature();
                break;
            case STREAM:
                type = ((StreamTypeSymbol) rawType).typeParameter().signature();
                break;
            case RECORD:
                //todo: Fix when #30245 is fixed.
            default:
                type = VAR_TYPE;
                break;
        }
        return type;
    }

    /**
     * Check if the given node is within a Block.
     *
     * @param node Expression node of which the context is checked
     * @return
     */
    private static boolean isInBlockContext(FieldAccessExpressionNode node) {
        return node.parent() instanceof ExpressionStatementNode &&
                (node.parent().parent() instanceof FunctionBodyNode ||
                        node.parent().parent() instanceof BlockStatementNode);
    }
}
