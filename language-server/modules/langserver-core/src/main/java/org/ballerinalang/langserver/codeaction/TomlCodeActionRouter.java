package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Project;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.WorkspaceEdit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Code Action Router for C2C.
 *
 * @since 2.0.0
 */
public class TomlCodeActionRouter {

    /**
     * Returns a list of supported code actions.
     *
     * @param ctx {@link CodeActionContext}
     * @return list of code actions
     */
    public static List<CodeAction> getAvailableCodeActions(CodeActionContext ctx) {
        List<CodeAction> codeActions = new ArrayList<>();
        List<Diagnostic> cursorDiagnostics = ctx.cursorDiagnostics();
        if (cursorDiagnostics != null && !cursorDiagnostics.isEmpty()) {
            for (Diagnostic diagnostic : cursorDiagnostics) {
                CodeAction action = new CodeAction();
                WorkspaceEdit edit = new WorkspaceEdit();
                switch (diagnostic.getMessage()) {
                    case "Invalid Path":
                        codeActions.add(addResourceToService(cursorDiagnostics, ctx));
                        break;
                    case "Invalid Port":
                        action.setTitle("Port");
                        action.setDiagnostics(cursorDiagnostics);
                        codeActions.add(action);
                        break;
                }
            }
        }

//        CodeActionProvidersHolder codeActionProvidersHolder = CodeActionProvidersHolder.getInstance();
//
//        // Get available node-type based code-actions
//        SyntaxTree syntaxTree = ctx.workspace().syntaxTree(ctx.filePath()).orElseThrow();
//        Optional<Node> matchedNode = CodeActionUtil.getTopLevelNode(ctx.cursorPosition(), syntaxTree);
//        CodeActionNodeType matchedNodeType = CodeActionUtil.codeActionNodeType(matchedNode.orElse(null));
//        SemanticModel semanticModel = ctx.workspace().semanticModel(ctx.filePath()).orElseThrow();
//        String relPath = ctx.workspace().relativePath(ctx.filePath()).orElseThrow();
//        if (matchedNode.isPresent() && matchedNodeType != CodeActionNodeType.NONE) {
//            Range range = CommonUtil.toRange(matchedNode.get().lineRange());
//            Node expressionNode = CodeActionUtil.largestExpressionNode(matchedNode.get(), range);
//            TypeSymbol matchedTypeSymbol = semanticModel.type(relPath, expressionNode.lineRange()).orElse(null);
//
//            PositionDetails posDetails = CodeActionPositionDetails.from(matchedNode.get(), null, matchedTypeSymbol);
//            ctx.setPositionDetails(posDetails);
//            codeActionProvidersHolder.getActiveNodeBasedProviders(matchedNodeType).forEach(provider -> {
//                try {
//                    List<CodeAction> codeActionsOut = provider.getNodeBasedCodeActions(ctx);
//                    if (codeActionsOut != null) {
//                        codeActions.addAll(codeActionsOut);
//                    }
//                } catch (Exception e) {
//                    String msg = "CodeAction '" + provider.getClass().getSimpleName() + "' failed!";
//                    LSClientLogger.logError(msg, e, null, (Position) null);
//                }
//            });
//        }
//
//        // Get available diagnostics based code-actions
//        List<Diagnostic> cursorDiagnostics = ctx.cursorDiagnostics();
//        if (cursorDiagnostics != null && !cursorDiagnostics.isEmpty()) {
//            for (Diagnostic diagnostic : cursorDiagnostics) {
//                PositionDetails positionDetails = computePositionDetails(diagnostic.getRange(), syntaxTree, ctx);
//                ctx.setPositionDetails(positionDetails);
//                codeActionProvidersHolder.getActiveDiagnosticsBasedProviders().forEach(provider -> {
//                    try {
//                        List<CodeAction> codeActionsOut = provider.getDiagBasedCodeActions(diagnostic, ctx);
//                        if (codeActionsOut != null) {
//                            codeActions.addAll(codeActionsOut);
//                        }
//                    } catch (Exception e) {
//                        String msg = "CodeAction '" + provider.getClass().getSimpleName() + "' failed!";
//                        LSClientLogger.logError(msg, e, null, (Position) null);
//                    }
//                });
//            }
//        }
        return codeActions;
    }

    public static CodeAction addResourceToService(List<Diagnostic> cursorDiagnostics, CodeActionContext ctx) {
//        Path filePath = ctx.filePath();
//        Optional<Project> project = ctx.workspace().project(filePath);
//        ModulePartNode modulePartNode = project.orElseThrow().rootNode();
        CodeAction action = new CodeAction();
        action.setTitle("Add Resource to Service");
        action.setDiagnostics(cursorDiagnostics);
        return action;
    }
}
