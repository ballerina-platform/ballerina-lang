/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.completions.util;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.PositionedOperationContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Symbol resolver for the field access expressions.
 * This visitor will go through the nodes and capture the respective types of the particular node.
 *
 * @since 2.0.0
 */
public class FieldAccessCompletionResolver extends NodeTransformer<Optional<TypeSymbol>> {

    private final PositionedOperationContext context;

    public FieldAccessCompletionResolver(PositionedOperationContext context) {
        this.context = context;
    }

    @Override
    public Optional<TypeSymbol> transform(SimpleNameReferenceNode node) {
        Optional<TypeSymbol> typeSymbol = this.context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(node));
        if (typeSymbol.isPresent()) {
            return typeSymbol;
        }

        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        return this.getSymbolByName(visibleSymbols, node.name().text())
                .flatMap(SymbolUtil::getTypeDescriptor);
    }

    @Override
    public Optional<TypeSymbol> transform(FieldAccessExpressionNode node) {
        // First capture the expression and the respective symbols
        Optional<TypeSymbol> typeSymbol = node.expression().apply(this);

        NameReferenceNode fieldName = node.fieldName();
        if (fieldName.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE || typeSymbol.isEmpty()) {
            return Optional.empty();
        }
        String name = ((SimpleNameReferenceNode) fieldName).name().text();
        List<Symbol> visibleEntries = this.getVisibleEntries(typeSymbol.get(), node.expression());
        Optional<Symbol> filteredSymbol = this.getSymbolByName(visibleEntries, name);

        if (filteredSymbol.isEmpty()) {
            return Optional.empty();
        }

        return SymbolUtil.getTypeDescriptor(filteredSymbol.get());
    }

    @Override
    public Optional<TypeSymbol> transform(OptionalFieldAccessExpressionNode node) {
        // First capture the expression and the respective symbols
        // In future we should use the following approach and get rid of this resolver.
        Optional<TypeSymbol> resolvedType = this.context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(node));
        if (resolvedType.isPresent() && resolvedType.get().typeKind() != TypeDescKind.COMPILATION_ERROR) {
            return SymbolUtil.getTypeDescriptor(resolvedType.get());
        }

        Optional<TypeSymbol> typeSymbol = node.expression().apply(this);
        NameReferenceNode fieldName = node.fieldName();
        if (fieldName.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE || typeSymbol.isEmpty()) {
            return Optional.empty();
        }
        String name = ((SimpleNameReferenceNode) fieldName).name().text();
        List<Symbol> visibleEntries = this.getVisibleEntries(typeSymbol.get(), node.expression());
        Optional<Symbol> filteredSymbol = this.getSymbolByName(visibleEntries, name);

        if (filteredSymbol.isEmpty()) {
            return Optional.empty();
        }

        return SymbolUtil.getTypeDescriptor(filteredSymbol.get());
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionCallExpressionNode node) {
        NameReferenceNode nameRef = node.functionName();

        Predicate<Symbol> fSymbolPredicate = symbol -> symbol.kind() == SymbolKind.FUNCTION;
        Predicate<Symbol> fPointerPredicate = symbol ->
                symbol.kind() == SymbolKind.VARIABLE
                        && CommonUtil.getRawType(((VariableSymbol) symbol).typeDescriptor()).typeKind()
                        == TypeDescKind.FUNCTION;
        List<Symbol> visibleEntries;
        String functionName;
        if (nameRef.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nameRef;
            visibleEntries = QNameRefCompletionUtil.getModuleContent(this.context, qNameRef,
                    fSymbolPredicate.or(fPointerPredicate));
            functionName = qNameRef.identifier().text();
        } else if (nameRef.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            functionName = ((SimpleNameReferenceNode) nameRef).name().text();
            visibleEntries = context.visibleSymbols(context.getCursorPosition()).stream()
                    .filter(fSymbolPredicate.or(fPointerPredicate))
                    .collect(Collectors.toList());
        } else {
            return Optional.empty();
        }

        Optional<Symbol> functionSymbol = this.getSymbolByName(visibleEntries, functionName);

        if (functionSymbol.isPresent() && fSymbolPredicate.test(functionSymbol.get())) {
            return ((FunctionSymbol) functionSymbol.get()).typeDescriptor().returnTypeDescriptor();
        }
        if (functionSymbol.isPresent() && fPointerPredicate.test(functionSymbol.get())) {
            TypeSymbol rawType = CommonUtil.getRawType(((VariableSymbol) functionSymbol.get()).typeDescriptor());
            return ((FunctionTypeSymbol) rawType).returnTypeDescriptor();
        }

        return Optional.empty();
    }

    @Override
    public Optional<TypeSymbol> transform(IndexedExpressionNode node) {
        Optional<TypeSymbol> containerType = node.containerExpression().apply(this);
        TypeSymbol rawType = CommonUtil.getRawType(containerType.orElseThrow());
        if (rawType.typeKind() == TypeDescKind.ARRAY) {
            return Optional.of(((ArrayTypeSymbol) rawType).memberTypeDescriptor());
        }
        if (rawType.typeKind() == TypeDescKind.MAP) {
            return Optional.of(((MapTypeSymbol) rawType).typeParam());
        }

        return context.currentSemanticModel().get().typeOf(node);
    }

    @Override
    public Optional<TypeSymbol> transform(BracedExpressionNode node) {
        return node.expression().apply(this);
    }

    @Override
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        if (node instanceof ExpressionNode) {
            return this.context.currentSemanticModel().get().typeOf(node);
        }

        return Optional.empty();
    }

    /**
     * Get the visible symbol entries on the type of the node.
     *
     * @param node to be evaluated
     * @return {@link List}
     */
    public List<Symbol> getVisibleEntries(Node node) {
        Optional<TypeSymbol> typeSymbol = getTypeSymbol(node);
        return typeSymbol.map(tSymbol -> this.getVisibleEntries(tSymbol, node)).orElse(Collections.emptyList());
    }

    /**
     * returns the TypeSymbol given the node.
     *
     * @param node Node of which the TypeSymbol should be resolved
     * @return {@link TypeSymbol}
     */
    public Optional<TypeSymbol> getTypeSymbol(Node node) {
        return node.apply(this);
    }

    private Optional<Symbol> getSymbolByName(List<Symbol> visibleSymbols, String name) {
        return visibleSymbols.stream()
                .filter((symbol -> symbol.nameEquals(name) && symbol.kind() != SymbolKind.TYPE_DEFINITION))
                .findFirst();
    }

    private List<Symbol> getVisibleEntries(TypeSymbol typeSymbol, Node node) {
        List<Symbol> visibleEntries = new ArrayList<>();
        TypeSymbol rawType = CommonUtil.getRawType(typeSymbol);
        switch (rawType.typeKind()) {
            case RECORD:
                // If the invoked for field access expression, then avoid suggesting the optional fields
                List<RecordFieldSymbol> filteredEntries =
                        new ArrayList<>(((RecordTypeSymbol) rawType).fieldDescriptors().values());
                visibleEntries.addAll(filteredEntries);
                break;
            case OBJECT:
                // add class field access test case as well
                Optional<Package> currentPkg = context.workspace()
                        .project(context.filePath())
                        .map(Project::currentPackage);
                Optional<Module> currentModule = context.currentModule();
                if (currentModule.isEmpty() || currentPkg.isEmpty()) {
                    break;
                }
                ObjectTypeSymbol objTypeDesc = (ObjectTypeSymbol) rawType;
                visibleEntries.addAll(objTypeDesc.fieldDescriptors().values().stream()
                        .filter(objectFieldSymbol -> withValidAccessModifiers(node, objectFieldSymbol, currentPkg.get(),
                                currentModule.get().moduleId())).collect(Collectors.toList()));
                boolean isClient = SymbolUtil.isClient(objTypeDesc);
                boolean isService = SymbolUtil.getTypeDescForObjectSymbol(objTypeDesc)
                        .qualifiers().contains(Qualifier.SERVICE);
                // If the object type desc is a client, then we avoid all the remote methods
                List<MethodSymbol> methodSymbols = objTypeDesc.methods().values().stream()
                        .filter(methodSymbol -> ((!isClient && !isService)
                                || !methodSymbol.qualifiers().contains(Qualifier.REMOTE))
                                && !methodSymbol.qualifiers().contains(Qualifier.RESOURCE)
                                && withValidAccessModifiers(node, methodSymbol, currentPkg.get(),
                                currentModule.get().moduleId()))
                        .collect(Collectors.toList());
                visibleEntries.addAll(methodSymbols);
                break;
            case UNION:
                if (node.parent().kind() != SyntaxKind.OPTIONAL_FIELD_ACCESS
                        || ((UnionTypeSymbol) rawType).memberTypeDescriptors().size() != 2) {
                    break;
                }
                List<TypeSymbol> members = ((UnionTypeSymbol) rawType).memberTypeDescriptors().stream()
                        .map(CommonUtil::getRawType)
                        .collect(Collectors.toList());
                if (!members.stream().allMatch(
                        member -> member.typeKind() == TypeDescKind.NIL || member.typeKind() == TypeDescKind.RECORD)) {
                    break;
                }
                // We have ensured that the members contain two members and one is record and one is nil.
                // Hence, safe to invoke .get without checking the isPresent
                TypeSymbol recordType = members.stream()
                        .filter(member -> member.typeKind() == TypeDescKind.RECORD)
                        .findFirst()
                        .get();
                visibleEntries.addAll(new ArrayList<>(((RecordTypeSymbol) recordType).fieldDescriptors().values()));
                break;
            case TYPE_REFERENCE:
                visibleEntries = getVisibleEntries(rawType, node);
                break;
            default:
                break;
        }
        visibleEntries.addAll(typeSymbol.langLibMethods());

        return visibleEntries;
    }

    private boolean withValidAccessModifiers(Node exprNode, Symbol symbol, Package currentPackage,
                                             ModuleId currentModule) {
        Optional<Project> project = context.workspace().project(context.filePath());
        Optional<ModuleSymbol> symbolModule = symbol.getModule();
        if (project.isEmpty() || symbolModule.isEmpty()) {
            return false;
        }

        boolean isPrivate = false;
        boolean isPublic = false;
        boolean isResource = false;

        if (symbol instanceof Qualifiable) {
            Qualifiable qSymbol = (Qualifiable) symbol;
            isPrivate = qSymbol.qualifiers().contains(Qualifier.PRIVATE);
            isPublic = qSymbol.qualifiers().contains(Qualifier.PUBLIC);
            isResource = qSymbol.qualifiers().contains(Qualifier.RESOURCE);
        }

        if (exprNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE
                && ((SimpleNameReferenceNode) exprNode).name().text().equals(Names.SELF.getValue())
                && !isResource) {
            return true;
        }

        ModuleID objModuleId = symbolModule.get().id();
        return isPublic || (!isPrivate && objModuleId.moduleName().equals(currentModule.moduleName())
                && objModuleId.orgName().equals(currentPackage.packageOrg().value()));
    }
}
