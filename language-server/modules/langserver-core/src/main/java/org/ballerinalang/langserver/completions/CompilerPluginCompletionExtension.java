package org.ballerinalang.langserver.completions;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.projects.CompletionManager;
import io.ballerina.projects.CompletionResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.plugins.completion.CompletionContextImpl;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.CompletionExtension;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion extension implementation for ballerina compiler plugins.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.LanguageExtension")
public class CompilerPluginCompletionExtension implements CompletionExtension {

    @Override
    public boolean validate(CompletionParams inputParams) {
        return inputParams.getTextDocument().getUri().endsWith(".bal");
    }

    @Override
    public List<CompletionItem> execute(CompletionParams inputParams, CompletionContext context,
                                        LanguageServerContext serverContext) throws Throwable {

        return Collections.emptyList();
    }

    @Override
    public List<CompletionItem> execute(CompletionParams inputParams,
                                        CompletionContext context,
                                        LanguageServerContext serverContext,
                                        CancelChecker cancelChecker) throws Throwable {
        Optional<PackageCompilation> packageCompilation =
                context.workspace().waitAndGetPackageCompilation(context.filePath());
        if (packageCompilation.isEmpty() || context.currentDocument().isEmpty() ||
                context.currentSemanticModel().isEmpty()) {
            return Collections.emptyList();
        }

        LSClientLogger clientLogger = LSClientLogger.getInstance(context.languageServercontext());

        CompletionManager completionManager = packageCompilation.get().getCompletionManager();
        CompletionContextImpl completionContextImp = getCompletionContext(context);

        CompletionResult result = completionManager.completions(completionContextImp);
        if (!result.getErrors().isEmpty()) {
            result.getErrors().forEach(ex -> clientLogger.logError(LSContextOperation.TXT_COMPLETION,
                    "Exception thrown while getting completion items from: " + ex.getProviderName(),
                    ex.getCause(), new TextDocumentIdentifier(context.fileUri())));
        }

        return result.getCompletionItems().stream().map(completionItem -> {
            CompletionItem item = new CompletionItem();
            item.setLabel(completionItem.getLabel());
            item.setDetail(ItemResolverConstants.SNIPPET_TYPE);
            item.setInsertText(completionItem.getInsertText());
            item.setKind(CompletionItemKind.Snippet);
            item.setInsertTextFormat(InsertTextFormat.Snippet);
            if (completionItem.getPriority() == io.ballerina.projects.plugins.completion.CompletionItem.Priority.HIGH) {
                item.setSortText(SortingUtil.genSortText(1) + SortingUtil.genSortText(16));
            } else {
                item.setSortText(SortingUtil.genSortText(16));
            }
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> handledCustomURISchemes(CompletionParams inputParams,
                                                CompletionContext context,
                                                LanguageServerContext serverContext) {
        return Collections.singletonList(CommonUtil.URI_SCHEME_EXPR);
    }

    /**
     * Find the token at cursor.
     */
    public static CompletionContextImpl getCompletionContext(CompletionContext context) {
        Optional<Document> document = context.currentDocument();
        if (document.isEmpty()) {
            throw new RuntimeException("Could not find a valid document/token");
        }
        TextDocument textDocument = document.get().textDocument();

        Position position = context.getCursorPosition();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        context.setCursorPositionInTree(txtPos);
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nodeAtCursor = ((ModulePartNode) document.get().syntaxTree().rootNode()).findNode(range);

        Position cursorPos = context.getCursorPosition();
        int cursorPosInTree = context.getCursorPositionInTree();
        LinePosition linePosition = LinePosition.from(cursorPos.getLine(), cursorPos.getCharacter());
        SemanticModel semanticModel = context.currentSemanticModel().get();

        return CompletionContextImpl.from(context.fileUri(), context.filePath(),
                linePosition, cursorPosInTree, nodeAtCursor, document.get(), semanticModel);
    }
}
