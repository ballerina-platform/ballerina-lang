/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package io.ballerina.projects;

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.completion.CompletionContext;
import io.ballerina.projects.plugins.completion.CompletionException;
import io.ballerina.projects.plugins.completion.CompletionProvider;
import io.ballerina.tools.text.LinePosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages interaction with completion providers via compiler plugins.
 *
 * @since 2201.7.0
 */
public class CompletionManager {
    Map<Class<?>, List<CompletionProviderDescriptor>> completionProviders;

    private CompletionManager(List<CompilerPluginContextIml> compilerPluginContexts) {
        completionProviders = new HashMap<>();
        compilerPluginContexts.forEach(compilerPluginContextIml -> {
            for (CompletionProvider<Node> completionProvider : compilerPluginContextIml.completionProviders()) {
                for (Class<?> attachmentPoint : completionProvider.getSupportedNodes()) {
                    List<CompletionProviderDescriptor> completionProviderList =
                            completionProviders.computeIfAbsent(attachmentPoint, k -> new ArrayList<>());
                    completionProviderList.add(new CompletionProviderDescriptor(completionProvider,
                            compilerPluginContextIml.compilerPluginInfo()));
                }
            }
        });
    }

    /**
     * Create a CompletionManager instance from the provided compiler plugin contexts.
     *
     * @param compilerPluginContexts compiler plugin contexts.
     * @return CompletionManager instance.
     */
    public static CompletionManager from(List<CompilerPluginContextIml> compilerPluginContexts) {
        return new CompletionManager(compilerPluginContexts);
    }

    /**
     * Get completions for the provided context from all available compiler plugins.
     *
     * @param context completion context.
     * @return Result object containing completion items and errors occurred while processing completion providers
     */
    public CompletionResult completions(CompletionContext context) {
        CompletionResult result = new CompletionResult();

        //If there are no completion items for the referenceNode resolve completion items for the parent node
        List<CompletionProviderDescriptor> completionProviderDescriptors = new ArrayList<>();
        Node referenceNode = context.nodeAtCursor();
        List<Node> resolverChain = new ArrayList<>();
        while ((referenceNode != null)) {
            completionProviderDescriptors =
                    completionProviders.getOrDefault(referenceNode.getClass(), Collections.emptyList());
            if (!completionProviderDescriptors.isEmpty() && !resolverChain.contains(referenceNode)) {
                break;
            }
            resolverChain.add(referenceNode);
            referenceNode = referenceNode.parent();
        }

        //Atm, we allow completions only inside service declarations
        if (referenceNode == null || !isInServiceBodyNodeContext(context, referenceNode)) {
            return result;
        }

        ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) referenceNode;
        List<ModuleSymbol> moduleSymbols = getModulesOfActiveListeners(context, serviceDeclarationNode);
        completionProviderDescriptors.stream().filter(descriptor -> {
                    if (descriptor.compilerPluginInfo.kind() == CompilerPluginKind.PACKAGE_PROVIDED) {
                        PackageProvidedCompilerPluginInfo compilerPluginInfo =
                                (PackageProvidedCompilerPluginInfo) descriptor.compilerPluginInfo();
                        return moduleSymbols.stream().anyMatch(moduleSymbol ->
                                moduleSymbol.id().orgName().equals(compilerPluginInfo.packageDesc().org().value())
                                        && moduleSymbol.id().packageName()
                                        .equals(compilerPluginInfo.packageDesc().name().value()));
                    }
                    return moduleSymbols.stream().anyMatch(moduleSymbol ->
                            moduleSymbol.id().orgName()
                                    .equals(context.currentDocument().module().descriptor().org().value())
                                    && moduleSymbol.id().packageName()
                                    .equals(context.currentDocument().module().descriptor().packageName().value()));
                })
                .forEach(descriptor -> {
                    try {
                        result.addCompletionItems(descriptor.completionProvider()
                                .getCompletions(context, serviceDeclarationNode));
                    } catch (Throwable t) {
                        String name;
                        if (descriptor.compilerPluginInfo.kind() == CompilerPluginKind.PACKAGE_PROVIDED) {
                            PackageProvidedCompilerPluginInfo compilerPluginInfo =
                                    (PackageProvidedCompilerPluginInfo) descriptor.compilerPluginInfo();
                            name = compilerPluginInfo.packageDesc().org().value() + "/" +
                                    compilerPluginInfo.packageDesc().name().value() + ":" +
                                    compilerPluginInfo.packageDesc().version().value()
                                    + ":" + descriptor.completionProvider().name();
                        } else {
                            name = descriptor.completionProvider().name();
                        }
                        CompletionException ex = new CompletionException(t, name);
                        result.addError(ex);
                    }
                });
        return result;
    }

    private List<ModuleSymbol> getModulesOfActiveListeners(CompletionContext context, ServiceDeclarationNode node) {
        Optional<Symbol> serviceSymbol = context.currentSemanticModel().symbol(node);
        Optional<LinePosition> linePosition = context.cursorPosition();
        if (serviceSymbol.isEmpty() && linePosition.isEmpty()) {
            return Collections.emptyList();
        }
        List<TypeSymbol> listenerTypes = ((ServiceDeclarationSymbol) serviceSymbol.get()).listenerTypes();
        return listenerTypes.stream().map(listenerType -> {
                    if (listenerType.typeKind() == TypeDescKind.UNION) {
                        return ((UnionTypeSymbol) listenerType).memberTypeDescriptors()
                                .stream()
                                .filter(memberType -> getRawType(memberType).typeKind() == TypeDescKind.OBJECT)
                                .findAny();
                    }
                    return Optional.of(listenerType);
                }).filter(listenerType -> listenerType.isPresent() && listenerType.get().getModule().isPresent())
                .map(listenerType -> listenerType.get().getModule().get()).toList();
    }

    private TypeSymbol getRawType(TypeSymbol typeDescriptor) {
        if (typeDescriptor.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) typeDescriptor;
            return typeRef.typeDescriptor();
        }
        return typeDescriptor;
    }

    private boolean isInServiceBodyNodeContext(CompletionContext context,
                                               Node referenceNode) {
        Optional<LinePosition> cursorPosition = context.cursorPosition();
        if (referenceNode.kind() != SyntaxKind.SERVICE_DECLARATION
                || cursorPosition.isEmpty()) {
            return false;
        }

        ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) referenceNode;
        LinePosition openBrace = serviceDeclarationNode.openBraceToken().lineRange().endLine();
        LinePosition closeBrace = serviceDeclarationNode.closeBraceToken().lineRange().startLine();
        int cursorLine = cursorPosition.get().line();
        int cursorOffset = cursorPosition.get().offset();

        //Ensure that the cursor is within braces (in service body)
        if (cursorLine < openBrace.line() || cursorLine > closeBrace.line()
                || cursorLine == openBrace.line() && cursorOffset < openBrace.offset()
                || cursorLine == closeBrace.line() && cursorOffset > closeBrace.offset()) {
            return false;
        }

        ServiceDeclarationContextValidator validator = new ServiceDeclarationContextValidator(context);
        validator.visitNode(context.nodeAtCursor());
        return validator.isValidContext();
    }

    /**
     * Descriptor for a completion provider.
     */
    static class CompletionProviderDescriptor {

        private final CompletionProvider<Node> completionProvider;
        private final CompilerPluginInfo compilerPluginInfo;

        public CompletionProviderDescriptor(CompletionProvider completionProvider,
                                            CompilerPluginInfo compilerPluginInfo) {
            this.completionProvider = completionProvider;
            this.compilerPluginInfo = compilerPluginInfo;
        }

        public CompletionProvider<Node> completionProvider() {
            return completionProvider;
        }

        public CompilerPluginInfo compilerPluginInfo() {
            return compilerPluginInfo;
        }
    }

    /**
     * Visitor to validate the completion context.
     *
     */
    static class ServiceDeclarationContextValidator extends NodeVisitor {

        private CompletionContext context;
        private boolean isValidContext = false;

        public ServiceDeclarationContextValidator(CompletionContext context) {
            this.context = context;
        }

        @Override
        public void visit(ServiceDeclarationNode serviceDeclarationNode) {
            isValidContext = true;
        }

        @Override
        public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
            simpleNameReferenceNode.parent().accept(this);
        }

        @Override
        public void visit(ObjectFieldNode objectFieldNode) {
            int cursorPosition = context.cursorPosInTree();
            isValidContext = objectFieldNode.textRange().startOffset() <= cursorPosition
                    && cursorPosition <= objectFieldNode.textRange().endOffset()
                    && objectFieldNode.fieldName().isMissing()
                    && objectFieldNode.equalsToken().isEmpty();
        }

        @Override
        public void visit(FunctionDefinitionNode functionDefinitionNode) {
            int cursorLine = context.cursorPosition().get().line();
            isValidContext = cursorLine < functionDefinitionNode.lineRange().startLine().line()
                    || functionDefinitionNode.lineRange().endLine().line() < cursorLine;
        }

        @Override
        public void visit(MethodDeclarationNode methodDeclarationNode) {
            int cursorLine = context.cursorPosition().get().line();
            isValidContext = cursorLine < methodDeclarationNode.lineRange().startLine().line()
                    || methodDeclarationNode.lineRange().endLine().line() < cursorLine;
        }

        @Override
        protected void visitSyntaxNode(Node node) {
            //Do nothing
        }

        public Boolean isValidContext() {
            return this.isValidContext;
        }

        public void visitNode(Node node) {
            if (node.kind() == SyntaxKind.LIST) {
                if (node.parent() != null) {
                    node.parent().accept(this);
                }
            } else {
                node.accept(this);
            }
        }
    }

}
