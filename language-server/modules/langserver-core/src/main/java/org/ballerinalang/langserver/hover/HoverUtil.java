/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.hover;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.codeaction.MatchedExpressionNodeResolver;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.HoverContext;
import org.ballerinalang.langserver.util.MarkupUtils;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.Optional;

/**
 * Utility class for Hover functionality of language server.
 */
public class HoverUtil {

    /**
     * Get the hover content.
     *
     * @param context Hover operation context
     * @return {@link Hover} Hover content
     */
    public static Hover getHover(HoverContext context) {
        Optional<Document> srcFile = context.currentDocument();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty() || srcFile.isEmpty()) {
            return HoverUtil.getHoverObject("");
        }
        //Fill node and token info at cursor
        fillTokenInfoAtCursor(context);

        Position cursorPosition = context.getCursorPosition();
        LinePosition linePosition = LinePosition.from(cursorPosition.getLine(), cursorPosition.getCharacter());
        // Check for the cancellation before the time-consuming operation
        context.checkCancelled();
        Optional<? extends Symbol> symbolAtCursor = semanticModel.get().symbol(srcFile.get(), linePosition);
        // Check for the cancellation after the time-consuming operation
        context.checkCancelled();

        HoverObjectResolver provider = new HoverObjectResolver(context);
        Hover hoverObj = HoverUtil.getHoverObject("");

        //Handles new expression
        if (symbolAtCursor.isEmpty()) {
            Range nodeRange = new Range(context.getCursorPosition(), context.getCursorPosition());
            NonTerminalNode nodeAtCursor = CommonUtil.findNode(nodeRange, srcFile.get().syntaxTree());
            if (nodeAtCursor != null) {
                MatchedExpressionNodeResolver expressionResolver = new MatchedExpressionNodeResolver(nodeAtCursor);
                Optional<ExpressionNode> expr = expressionResolver.findExpression(nodeAtCursor);
                if (expr.isPresent()) {
                    hoverObj = provider.getHoverObjectForExpression(expr.get());
                }
            }
        } else {
            hoverObj = provider.getHoverObjectForSymbol(symbolAtCursor.get());
        }
        //Add reference to APIDocs.
        if (hoverObj.getContents().isRight()) {
            MarkupContent markupContent = hoverObj.getContents().getRight();
            String content = markupContent.getValue();

            HoverSymbolResolver symbolResolver =
                    new HoverSymbolResolver(context, semanticModel.get());
            Optional<Symbol> symbol = context.getNodeAtCursor().apply(symbolResolver);
            if (symbol == null || symbol.isEmpty() || !symbolResolver.isSymbolReferable()) {
                return hoverObj;
            }

            Optional<ModuleID> moduleID = symbol.flatMap(Symbol::getModule).map(ModuleSymbol::id);
            if (moduleID.isEmpty() || symbol.get().getName().isEmpty()) {
                return hoverObj;
            }
            String url = APIDocReference.from(moduleID.get().orgName(),
                    moduleID.get().moduleName(), moduleID.get().version(), symbol.get().getName().get());
            markupContent.setValue((content.isEmpty() ? "" : content + MarkupUtils.getHorizontalSeparator())
                    + "[View API Docs](" + url + ")");
            hoverObj.setContents(markupContent);
        }

        return hoverObj;
    }

    /**
     * returns the default hover object.
     *
     * @return {@link Hover} hover object.
     */
    protected static Hover getHoverObject() {
        return getHoverObject("");
    }

    /**
     * Get a Hover object given the content.
     *
     * @return {@link Hover} hover object.
     */
    protected static Hover getHoverObject(String content) {
        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue(content);
        hover.setContents(hoverMarkupContent);
        return hover;
    }

    /**
     * Check if a given symbol has valid access modifiers to be visible with in the give context.
     *
     * @param symbol         Symbol.
     * @param currentPackage Current Package.
     * @param currentModule  Current Module.
     * @return {@link Boolean} Whether the symbol is visible in the current context.
     */
    protected static Boolean withValidAccessModifiers(Symbol symbol, Package currentPackage,
                                                      ModuleId currentModule, HoverContext context) {
        Optional<Project> project = context.workspace().project(context.filePath());
        Optional<ModuleSymbol> typeSymbolModule = symbol.getModule();

        if (project.isEmpty() || typeSymbolModule.isEmpty()) {
            return false;
        }

        boolean isResource = false;
        boolean isPrivate = false;
        boolean isPublic = false;
        boolean isRemote = false;

        if (symbol instanceof Qualifiable) {
            Qualifiable qSymbol = (Qualifiable) symbol;
            isPrivate = qSymbol.qualifiers().contains(Qualifier.PRIVATE);
            isPublic = qSymbol.qualifiers().contains(Qualifier.PUBLIC);
            isResource = qSymbol.qualifiers().contains(Qualifier.RESOURCE);
            isRemote = qSymbol.qualifiers().contains(Qualifier.REMOTE);
        }

        if (isResource || isRemote || isPublic) {
            return true;
        }

        ModuleID objModuleId = typeSymbolModule.get().id();
        return (!isPrivate && objModuleId.moduleName().equals(currentModule.moduleName())
                && objModuleId.orgName().equals(currentPackage.packageOrg().value()));
    }

    /**
     * Get the description only hover object.
     *
     * @return {@link Hover}
     */
    public static Hover getDescriptionOnlyHoverObject(Symbol symbol) {
        if (!(symbol instanceof Documentable) || ((Documentable) symbol).documentation().isEmpty()) {
            return HoverUtil.getHoverObject("");
        }

        return getDescriptionOnlyHoverObject(((Documentable) symbol).documentation().get());
    }

    /**
     * Get the description only hover object.
     *
     * @return {@link Hover}
     */
    public static Hover getDescriptionOnlyHoverObject(Documentation documentation) {
        String description = "";
        if (documentation.description().isPresent()) {
            description = documentation.description().get();
        }
        return HoverUtil.getHoverObject(description);
    }

    public static void fillTokenInfoAtCursor(HoverContext context) {
        Optional<Token> tokenAtCursor = PositionUtil.findTokenAtPosition(context, context.getCursorPosition());
        Optional<Document> document = context.currentDocument();
        if (document.isEmpty() || tokenAtCursor.isEmpty()) {
            throw new RuntimeException("Could not find a valid document/token");
        }
        context.setTokenAtCursor(tokenAtCursor.get());
        TextDocument textDocument = document.get().textDocument();

        Position position = context.getCursorPosition();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        context.setCursorPositionInTree(txtPos);
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nonTerminalNode = ((ModulePartNode) document.get().syntaxTree().rootNode()).findNode(range);
        context.setNodeAtCursor(nonTerminalNode);
    }
}
