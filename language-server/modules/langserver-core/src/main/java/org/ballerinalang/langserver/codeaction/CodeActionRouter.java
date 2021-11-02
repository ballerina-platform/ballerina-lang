/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;
import org.ballerinalang.langserver.telemetry.TelemetryUtil;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.codeaction.CodeActionUtil.computePositionDetails;

/**
 * Represents the Code Action router.
 *
 * @since 1.1.1
 */
public class CodeActionRouter {

    /**
     * Returns a list of supported code actions.
     *
     * @param ctx {@link CodeActionContext}
     * @return list of code actions
     */
    public static List<CodeAction> getAvailableCodeActions(CodeActionContext ctx) {
        LSClientLogger clientLogger = LSClientLogger.getInstance(ctx.languageServercontext());
        List<CodeAction> codeActions = new ArrayList<>();
        CodeActionProvidersHolder codeActionProvidersHolder
                = CodeActionProvidersHolder.getInstance(ctx.languageServercontext());

        // Get available node-type based code-actions
        SyntaxTree syntaxTree = ctx.currentSyntaxTree().orElseThrow();
        Optional<NonTerminalNode> topLevelNode = CodeActionUtil.getTopLevelNode(ctx.cursorPosition(), syntaxTree);
        CodeActionNodeType matchedNodeType = CodeActionUtil.codeActionNodeType(topLevelNode.orElse(null));
        if (topLevelNode.isPresent() && matchedNodeType != CodeActionNodeType.NONE) {
            Range range = CommonUtil.toRange(topLevelNode.get().lineRange());
            Node expressionNode = CodeActionUtil.largestExpressionNode(topLevelNode.get(), range);
            TypeSymbol matchedTypeSymbol = getMatchedTypeSymbol(ctx, expressionNode).orElse(null);

            NodeBasedPositionDetails posDetails = NodeBasedPositionDetailsImpl.from(topLevelNode.get(),
                                                                                    matchedStatementNode(ctx,
                                                                                                        syntaxTree),
                                                                                    matchedTypeSymbol);
            codeActionProvidersHolder.getActiveNodeBasedProviders(matchedNodeType, ctx).forEach(provider -> {
                try {
                    // Check whether the code action request has been cancelled
                    // in order to avoid unnecessary calculations
                    ctx.checkCancelled();
                    
                    List<CodeAction> codeActionsOut = provider.getNodeBasedCodeActions(ctx, posDetails);
                    if (codeActionsOut != null) {
                        codeActionsOut.forEach(codeAction -> 
                                TelemetryUtil.addReportFeatureUsageCommandToCodeAction(codeAction, provider));
                        codeActions.addAll(codeActionsOut);
                    }
                } catch (Exception e) {
                    String msg = "CodeAction '" + provider.getClass().getSimpleName() + "' failed!";
                    clientLogger.logError(LSContextOperation.TXT_CODE_ACTION, msg, e, null, (Position) null);
                }
            });
        }
        // Get available diagnostics based code-actions
        ctx.diagnostics(ctx.filePath()).stream().
                filter(diag -> CommonUtil
                        .isWithinRange(ctx.cursorPosition(), CommonUtil.toRange(diag.location().lineRange()))
                )
                .forEach(diagnostic -> {
                    DiagBasedPositionDetails positionDetails = computePositionDetails(syntaxTree, diagnostic, ctx);
                    codeActionProvidersHolder.getActiveDiagnosticsBasedProviders(ctx).forEach(provider -> {
                        try {
                            // Check whether the code action request has been cancelled
                            // in order to avoid unnecessary calculations
                            ctx.checkCancelled();
                            
                            List<CodeAction> codeActionsOut = provider.getDiagBasedCodeActions(diagnostic,
                                                                                               positionDetails, ctx);
                            if (codeActionsOut != null) {
                                codeActionsOut.forEach(codeAction ->
                                        TelemetryUtil.addReportFeatureUsageCommandToCodeAction(codeAction, provider));
                                codeActions.addAll(codeActionsOut);
                            }
                        } catch (Exception e) {
                            String msg = "CodeAction '" + provider.getClass().getSimpleName() + "' failed!";
                            clientLogger.logError(LSContextOperation.TXT_CODE_ACTION, msg, e, null, (Position) null);
                        }
                    });
                });
        return codeActions;
    }

    private static NonTerminalNode matchedStatementNode(CodeActionContext ctx, SyntaxTree syntaxTree) {
        Position cursorPos = ctx.cursorPosition();
        NonTerminalNode matchedNode = CommonUtil.findNode(new Range(cursorPos, cursorPos), syntaxTree);
        while (matchedNode.parent() != null &&
                matchedNode.parent().kind() != SyntaxKind.MODULE_PART &&
                matchedNode.parent().kind() != SyntaxKind.FUNCTION_BODY_BLOCK &&
                !(matchedNode.parent() instanceof BlockStatementNode)) {
            matchedNode = matchedNode.parent();
        }
        return matchedNode;
    }

    private static Optional<TypeSymbol> getMatchedTypeSymbol(CodeActionContext context, Node node) {
        SemanticModel semanticModel = context.currentSemanticModel().orElseThrow();
        if (node.kind() != SyntaxKind.CAPTURE_BINDING_PATTERN) {
            return semanticModel.typeOf(node.lineRange());
        }
        List<Symbol> visibleSymbols = context.visibleSymbols(context.cursorPosition());
        CaptureBindingPatternNode patternNode = (CaptureBindingPatternNode) node;
        String varName = patternNode.variableName().text();
        return visibleSymbols.stream()
                .filter(symbol -> symbol.getName().orElse("").equals(varName))
                .map(SymbolUtil::getTypeDescriptor)
                .findFirst()
                .orElse(Optional.empty());
    }
}
