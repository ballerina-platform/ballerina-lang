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
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.codeaction.MatchedExpressionNodeResolver;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.HoverContext;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.Collections;
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

        Position cursorPosition = context.getCursorPosition();
        LinePosition linePosition = LinePosition.from(cursorPosition.getLine(), cursorPosition.getCharacter());
        // Check for the cancellation before the time-consuming operation
        context.checkCancelled();
        Optional<? extends Symbol> symbolAtCursor = semanticModel.get().symbol(srcFile.get(), linePosition);
        // Check for the cancellation after the time-consuming operation
        context.checkCancelled();

        HoverObjectResolver provider = new HoverObjectResolver(context);
        //Handles new expression
        if (symbolAtCursor.isEmpty()) {
            Range nodeRange = new Range(context.getCursorPosition(), context.getCursorPosition());
            NonTerminalNode nodeAtCursor = CommonUtil.findNode(nodeRange, srcFile.get().syntaxTree());
            if (nodeAtCursor != null) {
                MatchedExpressionNodeResolver expressionResolver = new MatchedExpressionNodeResolver(nodeAtCursor);
                Optional<ExpressionNode> expr = expressionResolver.findExpression(nodeAtCursor);
                if (expr.isPresent()) {
                    return provider.getHoverObjectForExpression(expr.get());
                }
            }
            return HoverUtil.getHoverObject("");
        }
        return provider.getHoverObjectForSymbol(symbolAtCursor.get());
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

    protected static String getHorizontalSeparator() {
        return CommonUtil.MD_LINE_SEPARATOR + CommonUtil.MD_LINE_SEPARATOR + "---"
                + CommonUtil.MD_LINE_SEPARATOR + CommonUtil.MD_LINE_SEPARATOR;
    }

    protected static String quotedString(String value) {
        return "`" + value.trim() + "`";
    }

    protected static String boldString(String value) {
        return "**" + value.trim() + "**";
    }

    protected static String italicString(String value) {
        return "*" + value.trim() + "*";
    }

    protected static String bulletItem(String value) {
        return "+ " + value.trim() + CommonUtil.MD_LINE_SEPARATOR;
    }

    protected static String header(int level, String header) {
        return String.join("", Collections.nCopies(level, "#")) + " " + header;
    }

}
