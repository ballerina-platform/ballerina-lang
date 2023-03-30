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
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.CodeActionResolveContext;
import org.ballerinalang.langserver.commons.codeaction.ResolvableCodeAction;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.ResolvableCodeActionProvider;
import org.ballerinalang.langserver.telemetry.TelemetryUtil;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;

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

        // Get available range based code-actions
        Optional<SyntaxTree> syntaxTree = ctx.currentSyntaxTree();
        if (syntaxTree.isEmpty()) {
            clientLogger.logTrace(LSContextOperation.TXT_CODE_ACTION.getName() + " " +
                    " Syntax tree is empty for file " + ctx.fileUri());
            return Collections.emptyList();
        }
        Range highlightedRange = ctx.range();
        // Run code action node analyzer
        CodeActionNodeAnalyzer analyzer = CodeActionNodeAnalyzer.analyze(highlightedRange, syntaxTree.get());
        Optional<NonTerminalNode> codeActionNode = analyzer.getCodeActionNode();
        SyntaxKind syntaxKind = analyzer.getSyntaxKind();
        if (codeActionNode.isPresent() && syntaxKind != SyntaxKind.NONE) {
            Range range = PositionUtil.toRange(codeActionNode.get().lineRange());
            Node expressionNode = CodeActionUtil.largestExpressionNode(codeActionNode.get(), range);
            TypeSymbol matchedTypeSymbol = getMatchedTypeSymbol(ctx, expressionNode).orElse(null);

            RangeBasedPositionDetails posDetails = RangeBasedPositionDetailsImpl.PositionDetailsBuilder.newBuilder()
                    .setTopLevelNodeType(matchedTypeSymbol)
                    .setTopLevelNode(codeActionNode.get())
                    .setCodeActionNode(codeActionNode.get())
                    .setDocumentableNode(analyzer.getDocumentableNode().orElse(null))
                    .setEnclosingDocumentableNode(analyzer.getEnclosingDocumentableNode().orElse(null))
                    .setStatementNode(analyzer.getStatementNode().orElse(null))
                    .build();

            codeActionProvidersHolder.getActiveRangeBasedProviders(syntaxKind, ctx).forEach(provider -> {
                try {
                    // Check whether the code action request has been cancelled
                    // in order to avoid unnecessary calculations
                    ctx.checkCancelled();

                    if (!provider.validate(ctx, posDetails)) {
                        return;
                    }
                    List<CodeAction> codeActionsOut = provider.getCodeActions(ctx, posDetails);
                    if (codeActionsOut != null) {
                        codeActionsOut.forEach(codeAction ->
                                TelemetryUtil.addReportFeatureUsageCommandToCodeAction(codeAction, provider));
                        codeActions.addAll(codeActionsOut);
                    }
                } catch (CancellationException ignore) {
                    // Ignore the cancellation exception
                } catch (Exception e) {
                    String msg = "CodeAction '" + provider.getClass().getSimpleName() + "' failed!";
                    clientLogger.logError(LSContextOperation.TXT_CODE_ACTION, msg, e, null, (Position) null);
                }
            });
        }
        // Get available diagnostics based code-actions
        ctx.diagnostics(ctx.filePath()).stream().
                filter(diag -> PositionUtil
                        .isRangeWithinRange(highlightedRange, PositionUtil.toRange(diag.location().lineRange()))
                )
                .forEach(diagnostic -> {
                    DiagBasedPositionDetails positionDetails =
                            computePositionDetails(syntaxTree.get(), diagnostic, ctx);
                    codeActionProvidersHolder.getActiveDiagnosticsBasedProviders(ctx)
                            .forEach(provider -> {
                                try {
                                    // Check whether the code action request has been cancelled
                                    // in order to avoid unnecessary calculations
                                    ctx.checkCancelled();

                                    if (!provider.validate(diagnostic, positionDetails, ctx)) {
                                        return;
                                    }
                                    List<CodeAction> codeActionsOut = provider
                                            .getCodeActions(diagnostic, positionDetails, ctx);
                                    codeActionsOut.forEach(codeAction ->
                                            TelemetryUtil.addReportFeatureUsageCommandToCodeAction(codeAction,
                                                    provider));
                                    codeActions.addAll(codeActionsOut);
                                } catch (CancellationException ignore) {
                                    // Ignore the cancellation exception
                                } catch (Exception e) {
                                    String msg = "CodeAction '" + provider.getClass().getSimpleName() + "' failed!";
                                    clientLogger.logError(LSContextOperation.TXT_CODE_ACTION, msg, e, null,
                                            (Position) null);
                                }
                            });
                });
        return codeActions;
    }

    public static CodeAction resolveCodeAction(ResolvableCodeAction codeAction,
                                               CodeActionResolveContext resolveContext) {
        CodeActionProvidersHolder codeActionProvidersHolder = CodeActionProvidersHolder
                .getInstance(resolveContext.languageServercontext());
        Optional<? extends LSCodeActionProvider> provider = codeActionProvidersHolder.getProviderByName(
                codeAction.getData().getCodeActionName());
        CodeAction action = codeAction;
        if (provider.isPresent() && provider.get() instanceof ResolvableCodeActionProvider) {
            action = ((ResolvableCodeActionProvider) provider.get()).resolve(codeAction, resolveContext);
        }

        return action;
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
