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

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Completion provider for {@link RecordFieldWithDefaultValueNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class RecordFieldWithDefaultValueNodeContext extends
        AbstractCompletionProvider<RecordFieldWithDefaultValueNode> {

    public RecordFieldWithDefaultValueNodeContext() {
        super(RecordFieldWithDefaultValueNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, RecordFieldWithDefaultValueNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (this.onQualifiedNameIdentifier(ctx, node.expression())) {
            /*
            Captures the following cases
            (1) [module:]TypeName c = module:<cursor>
            (2) [module:]TypeName c = module:a<cursor>
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) node.expression();
            Predicate<Symbol> modSymbolFilter = symbol -> symbol instanceof VariableSymbol;
            List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(ctx, qNameRef, modSymbolFilter);
            completionItems.addAll(this.getCompletionItemList(moduleContent, ctx));
        } else {
            /*
            Captures the following cases
            (1) [module:]TypeName c = <cursor>
            (2) [module:]TypeName c = a<cursor>
             */
            completionItems.addAll(this.actionKWCompletions(ctx));
            completionItems.addAll(this.expressionCompletions(ctx));
            completionItems.addAll(getNewExprCompletionItems(ctx, node.typeName()));
            completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_IS.get()));
        }
        this.sort(ctx, node, completionItems);

        return completionItems;
    }

    private List<LSCompletionItem> getNewExprCompletionItems(BallerinaCompletionContext context, Node typeNameNode) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        Optional<ClassSymbol> objectType;
        if (this.onQualifiedNameIdentifier(context, typeNameNode)) {
            String modulePrefix = QNameReferenceUtil.getAlias(((QualifiedNameReferenceNode) typeNameNode));
            Optional<ModuleSymbol> module = CommonUtil.searchModuleForAlias(context, modulePrefix);
            if (module.isEmpty()) {
                return completionItems;
            }
            String identifier = ((QualifiedNameReferenceNode) typeNameNode).identifier().text();
            ModuleSymbol moduleSymbol = module.get();
            Stream<Symbol> classesAndTypes = Stream.concat(moduleSymbol.classes().stream(),
                    moduleSymbol.typeDefinitions().stream());
            objectType = classesAndTypes
                    .filter(symbol -> SymbolUtil.isClass(symbol) && symbol.name().equals(identifier))
                    .map(SymbolUtil::getTypeDescForClassSymbol)
                    .findAny();
        } else if (typeNameNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            String identifier = ((SimpleNameReferenceNode) typeNameNode).name().text();
            objectType = visibleSymbols.stream()
                    .filter(symbol -> SymbolUtil.isClass(symbol) && symbol.name().equals(identifier))
                    .map(SymbolUtil::getTypeDescForClassSymbol)
                    .findAny();
        } else {
            objectType = Optional.empty();
        }

        objectType.ifPresent(objectTypeDesc ->
                completionItems.add(this.getImplicitNewCompletionItem(objectTypeDesc, context)));

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, RecordFieldWithDefaultValueNode node) {
        Integer textPosition = context.getCursorPositionInTree();
        TextRange equalTokenRange = node.equalsToken().textRange();
        return equalTokenRange.endOffset() <= textPosition;
    }
}
