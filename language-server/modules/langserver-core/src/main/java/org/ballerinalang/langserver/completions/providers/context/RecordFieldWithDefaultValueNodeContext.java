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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.types.ObjectTypeDescriptor;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Completion provider for {@link RecordFieldWithDefaultValueNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class RecordFieldWithDefaultValueNodeContext extends
        AbstractCompletionProvider<RecordFieldWithDefaultValueNode> {

    public RecordFieldWithDefaultValueNodeContext() {
        super(RecordFieldWithDefaultValueNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, RecordFieldWithDefaultValueNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (this.onQualifiedNameIdentifier(context, node.expression())) {
            /*
            Captures the following cases
            (1) [module:]TypeName c = module:<cursor>
            (2) [module:]TypeName c = module:a<cursor>
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) node.expression();
            Predicate<Symbol> modSymbolFilter = symbol -> symbol instanceof VariableSymbol;
            List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, qNameRef, modSymbolFilter);
            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else {
            /*
            Captures the following cases
            (1) [module:]TypeName c = <cursor>
            (2) [module:]TypeName c = a<cursor>
             */
            completionItems.addAll(this.actionKWCompletions(context));
            completionItems.addAll(this.expressionCompletions(context));
            completionItems.addAll(getNewExprCompletionItems(context, node.typeName()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_IS.get()));
        }

        return completionItems;
    }

    private List<LSCompletionItem> getNewExprCompletionItems(LSContext context, Node typeNameNode) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        ArrayList<Symbol> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        Optional<ObjectTypeDescriptor> objectType;
        if (this.onQualifiedNameIdentifier(context, typeNameNode)) {
            String modulePrefix = QNameReferenceUtil.getAlias(((QualifiedNameReferenceNode) typeNameNode));
            Optional<ModuleSymbol> module = CommonUtil.searchModuleForAlias(context, modulePrefix);
            if (module.isEmpty()) {
                return completionItems;
            }
            String identifier = ((QualifiedNameReferenceNode) typeNameNode).identifier().text();
            objectType = module.get().typeDefinitions().stream()
                    .filter(symbol -> SymbolUtil.isObject(symbol) && symbol.name().equals(identifier))
                    .map(SymbolUtil::getTypeDescForObjectSymbol)
                    .findAny();
        } else if (typeNameNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            String identifier = ((SimpleNameReferenceNode) typeNameNode).name().text();
            objectType = visibleSymbols.stream()
                    .filter(symbol -> SymbolUtil.isObject(symbol) && symbol.name().equals(identifier))
                    .map(SymbolUtil::getTypeDescForObjectSymbol)
                    .findAny();
        } else {
            objectType = Optional.empty();
        }

        objectType.ifPresent(objectTypeDesc ->
                completionItems.add(this.getImplicitNewCompletionItem(objectTypeDesc, context)));

        return completionItems;
    }

    @Override
    public boolean onPreValidation(LSContext context, RecordFieldWithDefaultValueNode node) {
        Integer textPosition = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        TextRange equalTokenRange = node.equalsToken().textRange();
        return equalTokenRange.endOffset() <= textPosition;
    }
}
