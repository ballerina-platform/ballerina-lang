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
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Dynamic foreach snippet construction for iterables.
 *
 * @since 2.0.0
 */
public class ForeachCompletionUtil {

    private static final String VAR_NAME = "item";
    private static final String VAR_NAME_RANGE_EXP = "i";
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
        TypeSymbol rawType = CommonUtil.getRawType(typeSymbol);
        if (!isInBlockContext(expr) || !ITERABLES.contains(rawType.typeKind())
                || !(expr.expression().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE ||
                expr.expression().kind() == SyntaxKind.FUNCTION_CALL ||
                expr.expression().kind() == SyntaxKind.FIELD_ACCESS)) {
            return completionItems;
        }

        //Skip stream types which have specified the completion type.
        if (rawType.typeKind() == TypeDescKind.STREAM &&
                ((StreamTypeSymbol) rawType).completionValueTypeParameter().typeKind() != TypeDescKind.NIL) {
            return completionItems;
        }
        completionItems.addAll(getForEachCompletionItems(ctx, expr, typeSymbol));
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
    private static List<LSCompletionItem> getForEachCompletionItems(BallerinaCompletionContext ctx,
                                                                    FieldAccessExpressionNode expr,
                                                                    TypeSymbol symbol) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        //create the text edit item to replace the field accession text
        List<TextEdit> textEdits = new ArrayList<>();
        TextEdit textEdit = new TextEdit();
        textEdit.setNewText("");
        LineRange lineRange = expr.lineRange();
        Position start = new Position(lineRange.startLine().line(), lineRange.startLine().offset());
        Position end = new Position(lineRange.endLine().line(), lineRange.endLine().offset());
        textEdit.setRange(new Range(start, end));
        textEdits.add(textEdit);

        String symbolName;
        if (expr.expression().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            symbolName = ((SimpleNameReferenceNode) expr.expression()).name().text();
        } else {
            symbolName = expr.expression().toSourceCode().trim();
        }

        completionItems.add(new StaticCompletionItem(ctx,
                getIteratingCompletionItem(ctx, symbolName, symbol, textEdits), StaticCompletionItem.Kind.OTHER));

        //Skip foreach range statement for stream type
        if (CommonUtil.getRawType(symbol).typeKind() != TypeDescKind.STREAM) {
            completionItems.add(new StaticCompletionItem(ctx,
                    getRangeExprCompletionItem(ctx, symbolName, textEdits), StaticCompletionItem.Kind.OTHER));
        }
        return completionItems;
    }

    /**
     * Returns the foreach completionitem to iterate the symbol.
     *
     * @param ctx        completion context
     * @param symbolName symbol name corresponding to the symbol
     * @param symbol     type symbol
     * @param textEdits  edits that replace the field access symbol
     * @return
     */
    private static CompletionItem getIteratingCompletionItem(BallerinaCompletionContext ctx,
                                                             String symbolName, TypeSymbol symbol,
                                                             List<TextEdit> textEdits) {
        String detail = "foreach var item in expr";
        //change label and tests accordingly
        String label = "foreach";
        String type = getTypeOfIteratorVariable(ctx, symbol);
        StringBuilder snippet = new StringBuilder("foreach");
        snippet.append(" ").append(type).append(" ")
                .append(CommonUtil.getValidatedSymbolName(ctx, VAR_NAME)).append(" in ").append(symbolName)
                .append(" ").append("{").append(CommonUtil.LINE_SEPARATOR).append("\t${1}")
                .append(CommonUtil.LINE_SEPARATOR).append("}");
        String documentation = "foreach statement for iterable variable - " + symbolName;
        return ForeachCompletionItemBuilder.build(snippet.toString(), label, detail, documentation, textEdits);
    }

    /**
     * Returns the foreach completion item with range expression.
     *
     * @param ctx        completion context
     * @param symbolName symbol name corresponding to the symbol
     * @param textEdits  edits that replace the field access symbol
     * @return
     */
    private static CompletionItem getRangeExprCompletionItem(BallerinaCompletionContext ctx,
                                                             String symbolName,
                                                             List<TextEdit> textEdits) {
        String detail = "foreach int i in 0...expr";
        String label = "foreach i";
        StringBuilder snippet = new StringBuilder("foreach");
        snippet.append(" int ").append(CommonUtil.getValidatedSymbolName(ctx, VAR_NAME_RANGE_EXP))
                .append(" in ").append("${1:0}").append("...").append(symbolName)
                .append(".length() ").append("{").append(CommonUtil.LINE_SEPARATOR).append("\t${2}")
                .append(CommonUtil.LINE_SEPARATOR).append("}");
        String documentation = "foreach i statement for iterable variable - " + symbolName;
        return ForeachCompletionItemBuilder.build(snippet.toString(), label, detail, documentation, textEdits);
    }

    /**
     * Returns the type of the member type of an iterable type symbol.
     *
     * @param symbol iterable type symbol.
     * @return signature of the member type.
     */
    public static String getTypeOfIteratorVariable(BallerinaCompletionContext ctx, TypeSymbol symbol) {

        String type;
        TypeSymbol rawType = CommonUtil.getRawType(symbol);
        switch (rawType.typeKind()) {
            case STRING:
                type = "string:Char";
                break;
            case XML:
                Optional<TypeSymbol> xmlTypeParam = ((XMLTypeSymbol) symbol).typeParameter();
                //TODO: Refactor this specific fix after #31251
                if (xmlTypeParam.isEmpty() || (xmlTypeParam.get().typeKind() == TypeDescKind.UNION &&
                        ((UnionTypeSymbol) xmlTypeParam.get()).memberTypeDescriptors().size() == 4)) {
                    type = CommonUtil.getModifiedTypeName(ctx, rawType);
                    break;
                }
                type = CommonUtil.getModifiedTypeName(ctx, xmlTypeParam.get());
                break;
            case ARRAY:
                type = CommonUtil.getModifiedTypeName(ctx, ((ArrayTypeSymbol) rawType).memberTypeDescriptor());
                break;
            case TUPLE:
                List<String> typesSet = new ArrayList<>(((TupleTypeSymbol) rawType).
                        memberTypeDescriptors().stream().map(
                        tSymbol -> CommonUtil.getModifiedTypeName(ctx, tSymbol)).collect(Collectors.toSet()));
                if (typesSet.size() == 1) {
                    type = typesSet.get(0);
                } else {
                    type = VAR_TYPE;
                }
                break;
            case MAP:
                type = CommonUtil.getModifiedTypeName(ctx, ((MapTypeSymbol) rawType).typeParam());
                break;
            case TABLE:
                type = CommonUtil.getModifiedTypeName(ctx, ((TableTypeSymbol) rawType).rowTypeParameter());
                break;
            case STREAM:
                type = CommonUtil.getModifiedTypeName(ctx, ((StreamTypeSymbol) rawType).typeParameter());
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
