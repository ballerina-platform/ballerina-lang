/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ErrorMatchPatternNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ErrorMatchPatternNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ErrorMatchPatternNodeContext extends MatchStatementContext<ErrorMatchPatternNode> {

    public ErrorMatchPatternNodeContext() {
        super(ErrorMatchPatternNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ErrorMatchPatternNode node) {
        /*
        Covers the following cases
        eg:
            1) error <cursor>
            2) error E<cursor>
            3) error module1:<cursor>
            4) error module1:E<cursor>
         */
        List<Symbol> errorTypes;
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            // Covers 3 and 4
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
            errorTypes = QNameReferenceUtil.getModuleContent(context, qNameRef, this.errorTypeFilter());
        } else {
            // covers 1 and 2
            List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
            errorTypes = visibleSymbols.stream()
                    .filter(this.errorTypeFilter())
                    .collect(Collectors.toList());
            completionItems.addAll(this.getModuleCompletionItems(context));
        }
        completionItems.addAll(this.getCompletionItemList(errorTypes, context));
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private Predicate<Symbol> errorTypeFilter() {
        return symbol -> symbol.kind() == SymbolKind.TYPE_DEFINITION
                && this.isValidErrorType((TypeDefinitionSymbol) symbol);
    }

    private boolean isValidErrorType(TypeDefinitionSymbol typeDef) {
        TypeSymbol rawType = CommonUtil.getRawType(typeDef.typeDescriptor());
        if (rawType.typeKind() == TypeDescKind.UNION) {
            return ((UnionTypeSymbol) rawType).memberTypeDescriptors().stream()
                    .allMatch(typeSymbol -> typeSymbol.kind() == SymbolKind.TYPE_DEFINITION
                            && this.isValidErrorType((TypeDefinitionSymbol) typeSymbol));
        }

        return rawType.typeKind() == TypeDescKind.ERROR;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ErrorMatchPatternNode node) {
        return !node.errorKeyword().isMissing();
    }
}
