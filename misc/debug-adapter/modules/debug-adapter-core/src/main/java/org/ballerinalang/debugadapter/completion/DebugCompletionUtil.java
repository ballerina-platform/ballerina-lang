/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.debugadapter.completion;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.DebugExpressionCompiler;
import org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider;
import org.ballerinalang.langserver.completions.ProviderFactory;
import org.eclipse.lsp4j.debug.CompletionItem;
import org.eclipse.lsp4j.debug.CompletionItemType;
import org.eclipse.lsp4j.debug.CompletionsArguments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Common utility methods for debug completion operation.
 *
 * @since 2.0.0
 */
public class DebugCompletionUtil {

    private static final String PARENTHESIS = "()";

    /**
     * Get the updated bal file content which includes debug console expression
     *
     * @param args       debug completions arguments.
     * @param node       non terminal node
     * @param sourcePath source path of the debug bal file.
     * @param lineNumber debug breakpoint line number.
     * @return updated file content
     */
    public static String getUpdatedBalFileContent(CompletionsArguments args, NonTerminalNode node, String sourcePath,
                                                  int lineNumber) throws IOException {
        Path path = Paths.get(sourcePath);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        if (node instanceof FunctionBodyBlockNode) {
            int nodeStartLine = node.lineRange().startLine().line();
            int nodeEndLine = node.lineRange().endLine().line();

            if (nodeStartLine == lineNumber - 1) {
                String lineContent = lines.get(nodeStartLine);
                lineContent = lineContent + System.lineSeparator() + args.getText();
                lines.set(nodeStartLine, lineContent);
            } else if (nodeEndLine == lineNumber - 1) {
                String lineContent = lines.get(nodeEndLine);
                int startLineOffSet = node.lineRange().endLine().offset() - 1;
                lineContent = lineContent.substring(0, startLineOffSet) + args.getText() + System.lineSeparator()
                        + String.join("", Collections.nCopies(startLineOffSet, " "))
                        + lineContent.substring(startLineOffSet);
                lines.set(nodeEndLine, lineContent);
            }
        } else if (args != null && node != null && lineNumber > 0) {
            int nodeStartLine = node.lineRange().startLine().line();
            String lineContent = lines.get(nodeStartLine);
            int startLineOffSet = node.lineRange().startLine().offset();
            lineContent = lineContent.substring(0, startLineOffSet) + args.getText() + System.lineSeparator()
                    + String.join("", Collections.nCopies(startLineOffSet, " "))
                    + lineContent.substring(startLineOffSet);
            lines.set(nodeStartLine, lineContent);
        }

        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            result.append(line).append(System.lineSeparator());
        }
        return result.toString();
    }

    /**
     * Get the bal file content
     *
     * @param sourcePath source path of the debug bal file.
     * @return bal file content
     */
    public static String getBalFileContent(String sourcePath) throws IOException {
        return getUpdatedBalFileContent(null, null, sourcePath, 0);
    }

    /**
     * Get the nearest matching provider for the context node.
     * Router can be called recursively. Therefore, if there is an already checked resolver in the resolver chain,
     * that means the particular resolver could not handle the completions request. Therefore skip the particular node
     * and traverse the parent ladder to find the nearest matching resolver.
     *
     * @param node                   node to evaluate
     * @param debugCompletionContext debug completion context
     */
    public static void route(Node node, BallerinaDebugCompletionContext debugCompletionContext) {
        Map<Class<?>, BallerinaCompletionProvider<Node>> providers = ProviderFactory.instance().getProviders();
        Node reference = node;
        BallerinaCompletionProvider<Node> provider;

        while ((reference != null)) {
            provider = providers.get(reference.getClass());
            if (provider != null) {
                debugCompletionContext.addResolver(reference);
                break;
            }
            debugCompletionContext.addResolver(reference);
            reference = reference.parent();
        }
    }

    /**
     * Get the visible symbol completion items
     *
     * @param debugCompletionContext debug completion contexts
     * @return visible symbol completion item array
     */
    public static CompletionItem[] getVisibleSymbolCompletions(BallerinaDebugCompletionContext debugCompletionContext) {
        SuspendedContext suspendedContext = debugCompletionContext.getSuspendedContext();
        DebugExpressionCompiler debugCompiler = suspendedContext.getDebugCompiler();
        SemanticModel semanticContext = debugCompiler.getSemanticInfo();
        List<Symbol> symbolList = semanticContext.visibleSymbols(
                suspendedContext.getDocument(), LinePosition.from(suspendedContext.getLineNumber() - 1, 0));
        return getCompletions(symbolList);
    }

    /**
     * Get the visible symbol completion items
     *
     * @param symbolList visible symbol list
     * @return visible symbol completion item array
     */
    public static CompletionItem[] getCompletions(List<Symbol> symbolList) {
        List<CompletionItem> completionItems = new ArrayList<>();

        for (Symbol symbol : symbolList) {
            CompletionItem completionItem = new CompletionItem();
            if (symbol.getName().isPresent()
                    && (symbol.kind().name().equals(CompletionItemType.METHOD.toString())
                    || symbol.kind().name().equals(CompletionItemType.FUNCTION.toString()))) {
                completionItem.setLabel(symbol.getName().get() + PARENTHESIS);
                completionItem.setText(symbol.getName().get() + PARENTHESIS);
            } else {
                completionItem.setLabel(symbol.getName().get());
                completionItem.setText(symbol.getName().get());
            }
            if (Arrays.stream(CompletionItemType.values()).anyMatch(s -> s.name().equals(symbol.kind().name()))) {
                completionItem.setType(CompletionItemType.valueOf(symbol.kind().name()));
            }
            completionItems.add(completionItem);
        }
        return completionItems.toArray(new CompletionItem[0]);
    }

    /**
     * Get non terminal node at breakpoint
     *
     * @param source     bal source file
     * @param sourcePath bal source file path
     * @param lineNumber breakpoint line number
     * @return non terminal node at breakpoint
     */
    public static NonTerminalNode getNonTerminalNode(String source, String sourcePath, int lineNumber) {
        return getNonTerminalNode(source, sourcePath, null, lineNumber, 0);
    }

    /**
     * Get modified non terminal node at breakpoint
     *
     * @param source          bal source file
     * @param sourcePath      bal source file path
     * @param nonTerminalNode non terminal node
     * @param lineNumber      breakpoint line number
     * @param column          debug expression column number
     * @return non terminal node at breakpoint
     */
    public static NonTerminalNode getNonTerminalNode(String source, String sourcePath, NonTerminalNode nonTerminalNode,
                                                     int lineNumber, int column) {
        TextDocument document = TextDocuments.from(source);
        SyntaxTree syntaxTree = SyntaxTree.from(document, sourcePath);

        int textPosition = getTextPosition(nonTerminalNode, document, lineNumber, column - 1);
        TextRange range = TextRange.from(textPosition, 0);
        return ((ModulePartNode) syntaxTree.rootNode()).findNode(range);
    }

    private static int getTextPosition(NonTerminalNode nonTerminalNode, TextDocument textDocument, int lineNumber,
                                       int offset) {
        int textPosition = 0;

        if (nonTerminalNode == null) {
            return textDocument.textPositionFrom(LinePosition.from(lineNumber, offset));
        } else if (nonTerminalNode instanceof FunctionBodyBlockNode) {
            if (nonTerminalNode.lineRange().startLine().line() == lineNumber - 1) {
                textPosition = textDocument.textPositionFrom(LinePosition.from(
                        nonTerminalNode.lineRange().startLine().line() + 1, offset));
            } else if (nonTerminalNode.lineRange().endLine().line() == lineNumber - 1) {
                textPosition = textDocument.textPositionFrom(LinePosition.from(
                        nonTerminalNode.lineRange().endLine().line(), offset));
            }
        } else {
            textPosition = textDocument.textPositionFrom(
                    LinePosition.from(nonTerminalNode.lineRange().startLine().line(),
                            nonTerminalNode.lineRange().startLine().offset() + offset));
        }
        return textPosition;
    }
}
