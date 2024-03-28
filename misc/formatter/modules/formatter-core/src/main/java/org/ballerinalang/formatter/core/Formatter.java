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

/**
 * Class that exposes the formatting APIs.
 */
public class Formatter {

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
        ExpressionNode formattedNode;
        switch (parsedNode.kind()) {
            case ANNOT_ACCESS:
                formattedNode = treeModifier.transform((AnnotAccessExpressionNode) parsedNode);
                break;
            case BINARY_EXPRESSION:
                formattedNode = treeModifier.transform((BinaryExpressionNode) parsedNode);
                break;
            case BRACED_EXPRESSION:
                formattedNode = treeModifier.transform((BracedExpressionNode) parsedNode);
                break;
            case CHECK_EXPRESSION:
                formattedNode = treeModifier.transform((CheckExpressionNode) parsedNode);
                break;
            case CONDITIONAL_EXPRESSION:
                formattedNode = treeModifier.transform((ConditionalExpressionNode) parsedNode);
                break;
            case ERROR_CONSTRUCTOR:
                formattedNode = treeModifier.transform((ErrorConstructorExpressionNode) parsedNode);
                break;
            case EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION:
                formattedNode = treeModifier.transform((ExplicitAnonymousFunctionExpressionNode) parsedNode);
                break;
            case EXPLICIT_NEW_EXPRESSION:
                formattedNode = treeModifier.transform((ExplicitNewExpressionNode) parsedNode);
                break;
            case FIELD_ACCESS:
                formattedNode = treeModifier.transform((FieldAccessExpressionNode) parsedNode);
                break;
            case FUNCTION_CALL:
                formattedNode = treeModifier.transform((FunctionCallExpressionNode) parsedNode);
                break;
            case IMPLICIT_ANONYMOUS_FUNCTION_EXPRESSION:
                formattedNode = treeModifier.transform((ImplicitAnonymousFunctionExpressionNode) parsedNode);
                break;
            case IMPLICIT_NEW_EXPRESSION:
                formattedNode = treeModifier.transform((ImplicitNewExpressionNode) parsedNode);
                break;
            case INDEXED_EXPRESSION:
                formattedNode = treeModifier.transform((IndexedExpressionNode) parsedNode);
                break;
            case LET_EXPRESSION:
                formattedNode = treeModifier.transform((LetExpressionNode) parsedNode);
                break;
            case LIST_CONSTRUCTOR:
                formattedNode = treeModifier.transform((ListConstructorExpressionNode) parsedNode);
                break;
            case MAPPING_CONSTRUCTOR:
                formattedNode = treeModifier.transform((MappingConstructorExpressionNode) parsedNode);
                break;
            case METHOD_CALL:
                formattedNode = treeModifier.transform((MethodCallExpressionNode) parsedNode);
                break;
            case OBJECT_CONSTRUCTOR:
                formattedNode = treeModifier.transform((ObjectConstructorExpressionNode) parsedNode);
                break;
            case OPTIONAL_FIELD_ACCESS:
                formattedNode = treeModifier.transform((OptionalFieldAccessExpressionNode) parsedNode);
                break;
            case QUERY_EXPRESSION:
                formattedNode = treeModifier.transform((QueryExpressionNode) parsedNode);
                break;
            case REQUIRED_EXPRESSION:
                formattedNode = treeModifier.transform((RequiredExpressionNode) parsedNode);
                break;
            case TABLE_CONSTRUCTOR:
                formattedNode = treeModifier.transform((TableConstructorExpressionNode) parsedNode);
                break;
            case RAW_TEMPLATE_EXPRESSION:
                formattedNode = treeModifier.transform((TemplateExpressionNode) parsedNode);
                break;
            case TRANSACTIONAL_EXPRESSION:
                formattedNode = treeModifier.transform((TransactionalExpressionNode) parsedNode);
                break;
            case TRAP_EXPRESSION:
                formattedNode = treeModifier.transform((TrapExpressionNode) parsedNode);
                break;
            case TYPE_CAST_EXPRESSION:
                formattedNode = treeModifier.transform((TypeCastExpressionNode) parsedNode);
                break;
            case TYPE_TEST_EXPRESSION:
                formattedNode = treeModifier.transform((TypeTestExpressionNode) parsedNode);
                break;
            case TYPEOF_EXPRESSION:
                formattedNode = treeModifier.transform((TypeofExpressionNode) parsedNode);
                break;
            case UNARY_EXPRESSION:
                formattedNode = treeModifier.transform((UnaryExpressionNode) parsedNode);
                break;
            case XML_FILTER_EXPRESSION:
                formattedNode = treeModifier.transform((XMLFilterExpressionNode) parsedNode);
                break;
            case XML_STEP_EXPRESSION:
                formattedNode = treeModifier.transform((XMLStepExpressionNode) parsedNode);
                break;
            default:
                throw new FormatterException("Unsupported expression type: " + parsedNode.kind());
        }

        return formattedNode.toSourceCode().strip();
    }

    private static SyntaxTree modifyTree(SyntaxTree syntaxTree, FormattingOptions options, LineRange range)
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
