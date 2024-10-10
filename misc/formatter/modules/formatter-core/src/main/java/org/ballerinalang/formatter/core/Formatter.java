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
package org.ballerinalang.formatter.core;

import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.RequiredExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.TransactionalExpressionNode;
import io.ballerina.compiler.syntax.tree.TrapExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeofExpressionNode;
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLFilterExpressionNode;
import io.ballerina.compiler.syntax.tree.XMLStepExpressionNode;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.options.FormattingOptions;
import org.jetbrains.annotations.Nullable;

/**
 * Class that exposes the formatting APIs.
 */
public final class Formatter {

    private Formatter() {
    }

    /**
     * Formats the provided source string and returns back the formatted source string.
     *
     * @param source A Ballerina source in string form
     * @return A modified source string after formatting changes
     * @throws FormatterException Exception caught while formatting
     */
    public static String format(String source) throws FormatterException {
        return format(source, FormattingOptions.builder().build());
    }

    /**
     * Formats a line range of the provided SyntaxTree. All the nodes falling within the line range
     * specified will be formatted.
     *
     * @param syntaxTree The complete SyntaxTree, of which a part is to be formatted
     * @param range LineRange which specifies the range to be formatted
     * @return The modified SyntaxTree after formatting changes
     * @throws FormatterException Exception caught while formatting
     */
    public static SyntaxTree format(SyntaxTree syntaxTree, LineRange range) throws FormatterException {
        return format(syntaxTree, range, FormattingOptions.builder().build());
    }

    /**
     * Formats the provided SyntaxTree and returns back a formatted SyntaxTree.
     *
     * @param syntaxTree The SyntaxTree which is to be formatted
     * @return The modified SyntaxTree after formatting changes
     * @throws FormatterException Exception caught while formatting
     */
    public static SyntaxTree format(SyntaxTree syntaxTree) throws FormatterException {
        return format(syntaxTree, FormattingOptions.builder().build());
    }

    /**
     * Formats the provided source string while using the formatting options provided.
     *
     * @param source A Ballerina source in string form
     * @param options Formatting options that are to be used when formatting
     * @return A modified source string after formatting changes
     * @throws FormatterException Exception caught while formatting
     */
    public static String format(String source, FormattingOptions options) throws FormatterException {
        TextDocument textDocument = TextDocuments.from(source);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return modifyTree(syntaxTree, options, null).toSourceCode();
    }

    /**
     * Formats a line range of the provided SyntaxTree while using the formatting options provided. All the
     * nodes falling within the line range provided will be formatted.
     *
     * @param syntaxTree The complete SyntaxTree, of which a part is to be formatted
     * @param range LineRange which needs to be formatted
     * @param options Formatting options that are to be used when formatting
     * @return The modified SyntaxTree after formatting changes
     * @throws FormatterException Exception caught while formatting
     */
    public static SyntaxTree format(SyntaxTree syntaxTree, LineRange range, FormattingOptions options)
            throws FormatterException {
        return modifyTree(syntaxTree, options, range);
    }

    /**
     * Formats the provided SyntaxTree while using the formatting options provided.
     *
     * @param syntaxTree The SyntaxTree which is to be formatted
     * @param options Formatting options that are to be used when formatting
     * @return The modified SyntaxTree after formatting changes
     * @throws FormatterException Exception caught while formatting
     */
    public static SyntaxTree format(SyntaxTree syntaxTree, FormattingOptions options) throws FormatterException {
        return modifyTree(syntaxTree, options, null);
    }

    /**
     * Formats the provided expression while using the default formatting options.
     *
     * @param text The expression which is to be formatted
     * @return The modified text after formatting changes
     * @throws FormatterException Exception caught while formatting
     */
    public static String formatExpression(String text) throws FormatterException {
        FormattingTreeModifier treeModifier = new FormattingTreeModifier(FormattingOptions.builder().build(), null);
        ExpressionNode parsedNode = NodeParser.parseExpression(text);
        ExpressionNode formattedNode = switch (parsedNode.kind()) {
            case ANNOT_ACCESS -> treeModifier.transform((AnnotAccessExpressionNode) parsedNode);
            case BINARY_EXPRESSION -> treeModifier.transform((BinaryExpressionNode) parsedNode);
            case BRACED_EXPRESSION -> treeModifier.transform((BracedExpressionNode) parsedNode);
            case CHECK_EXPRESSION -> treeModifier.transform((CheckExpressionNode) parsedNode);
            case CONDITIONAL_EXPRESSION -> treeModifier.transform((ConditionalExpressionNode) parsedNode);
            case ERROR_CONSTRUCTOR -> treeModifier.transform((ErrorConstructorExpressionNode) parsedNode);
            case EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION ->
                    treeModifier.transform((ExplicitAnonymousFunctionExpressionNode) parsedNode);
            case EXPLICIT_NEW_EXPRESSION -> treeModifier.transform((ExplicitNewExpressionNode) parsedNode);
            case FIELD_ACCESS -> treeModifier.transform((FieldAccessExpressionNode) parsedNode);
            case FUNCTION_CALL -> treeModifier.transform((FunctionCallExpressionNode) parsedNode);
            case IMPLICIT_ANONYMOUS_FUNCTION_EXPRESSION ->
                    treeModifier.transform((ImplicitAnonymousFunctionExpressionNode) parsedNode);
            case IMPLICIT_NEW_EXPRESSION -> treeModifier.transform((ImplicitNewExpressionNode) parsedNode);
            case INDEXED_EXPRESSION -> treeModifier.transform((IndexedExpressionNode) parsedNode);
            case LET_EXPRESSION -> treeModifier.transform((LetExpressionNode) parsedNode);
            case LIST_CONSTRUCTOR -> treeModifier.transform((ListConstructorExpressionNode) parsedNode);
            case MAPPING_CONSTRUCTOR -> treeModifier.transform((MappingConstructorExpressionNode) parsedNode);
            case METHOD_CALL -> treeModifier.transform((MethodCallExpressionNode) parsedNode);
            case OBJECT_CONSTRUCTOR -> treeModifier.transform((ObjectConstructorExpressionNode) parsedNode);
            case OPTIONAL_FIELD_ACCESS -> treeModifier.transform((OptionalFieldAccessExpressionNode) parsedNode);
            case QUERY_EXPRESSION -> treeModifier.transform((QueryExpressionNode) parsedNode);
            case REQUIRED_EXPRESSION -> treeModifier.transform((RequiredExpressionNode) parsedNode);
            case TABLE_CONSTRUCTOR -> treeModifier.transform((TableConstructorExpressionNode) parsedNode);
            case RAW_TEMPLATE_EXPRESSION -> treeModifier.transform((TemplateExpressionNode) parsedNode);
            case TRANSACTIONAL_EXPRESSION -> treeModifier.transform((TransactionalExpressionNode) parsedNode);
            case TRAP_EXPRESSION -> treeModifier.transform((TrapExpressionNode) parsedNode);
            case TYPE_CAST_EXPRESSION -> treeModifier.transform((TypeCastExpressionNode) parsedNode);
            case TYPE_TEST_EXPRESSION -> treeModifier.transform((TypeTestExpressionNode) parsedNode);
            case TYPEOF_EXPRESSION -> treeModifier.transform((TypeofExpressionNode) parsedNode);
            case UNARY_EXPRESSION -> treeModifier.transform((UnaryExpressionNode) parsedNode);
            case XML_FILTER_EXPRESSION -> treeModifier.transform((XMLFilterExpressionNode) parsedNode);
            case XML_STEP_EXPRESSION -> treeModifier.transform((XMLStepExpressionNode) parsedNode);
            default -> throw new FormatterException("Unsupported expression type: " + parsedNode.kind());
        };

        return formattedNode.toSourceCode().strip();
    }

    private static SyntaxTree modifyTree(SyntaxTree syntaxTree, FormattingOptions options, @Nullable LineRange range)
            throws FormatterException {
        FormattingTreeModifier treeModifier = new FormattingTreeModifier(options, range);
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        try {
            return syntaxTree.modifyWith(treeModifier.transform(modulePartNode));
        } catch (Exception e) {
            throw new FormatterException("Error while formatting: " + e.getMessage(), e.getCause());
        }
    }
}
