/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.invoker.classload.visitors;

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;
import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.invoker.classload.ImportProcessor;
import io.ballerina.shell.utils.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Any imports that need to be done are calculated.
 * Eg: if the type was abc/z:TypeA then it will be converted as
 * 'import abc/z' will be added as an import.
 * We need to traverse all the sub-typed because any sub-type
 * may need to be imported.
 * Also, this will compute all the replacements that should be done.
 * In above case, abc/z:TypeA will be replaced with z:TypeA.
 *
 * @since 2.0.0
 */
public class ImportableTypeSymbolVisitor extends TypeSymbolVisitor {
    private static final String ANON_MODULE = "$anon";
    private static final Map<String, String> ORIGINAL_REPLACEMENTS = Map.of(
            "xml<>", "xml<never>",
            "anydata...;", "");

    private final ImportProcessor importProcessor;
    private final Set<String> implicitImportPrefixes;
    private final Map<String, String> importReplaces;
    private final Set<Symbol> visitedSymbols;

    public ImportableTypeSymbolVisitor(ImportProcessor importProcessor) {
        this.implicitImportPrefixes = new HashSet<>();
        this.importProcessor = importProcessor;
        this.importReplaces = new HashMap<>(ORIGINAL_REPLACEMENTS);
        this.visitedSymbols = new HashSet<>();
    }

    @Override
    protected void visitParameter(ParameterSymbol parameterSymbol) {
        visitType(parameterSymbol.typeDescriptor());
    }

    @Override
    protected void visitField(ObjectFieldSymbol fieldSymbol) {
        visitType(fieldSymbol.typeDescriptor());
    }

    @Override
    protected void visitField(RecordFieldSymbol fieldSymbol) {
        visitType(fieldSymbol.typeDescriptor());
    }

    @Override
    protected void visitMethod(MethodSymbol methodSymbol) {
        visitType(methodSymbol.typeDescriptor());
    }

    @Override
    protected void visit(ArrayTypeSymbol symbol) {
        visitType(symbol.memberTypeDescriptor());
    }

    @Override
    protected void visit(FutureTypeSymbol symbol) {
        symbol.typeParameter().ifPresent(this::visitType);
    }

    @Override
    protected void visit(IntersectionTypeSymbol symbol) {
        symbol.memberTypeDescriptors().forEach(this::visitType);
    }

    @Override
    protected void visit(MapTypeSymbol symbol) {
        symbol.typeParameter().ifPresent(this::visitType);
    }

    @Override
    protected void visit(TypeDescTypeSymbol symbol) {
        symbol.typeParameter().ifPresent(this::visitType);
    }

    @Override
    protected void visit(UnionTypeSymbol symbol) {
        symbol.memberTypeDescriptors().forEach(this::visitType);
    }

    @Override
    protected void visit(XMLTypeSymbol symbol) {
        // If symbol is simply `xml` no need to traverse child symbols
        if (!symbol.signature().equals("xml")) {
            symbol.typeParameter().ifPresent(this::visitType);
        }
    }

    @Override
    protected void visit(FunctionTypeSymbol symbol) {
        symbol.parameters().forEach(this::visitParameter);
        symbol.restParam().ifPresent(this::visitParameter);
        symbol.returnTypeDescriptor().ifPresent(this::visitType);
    }

    @Override
    protected void visit(ObjectTypeSymbol symbol) {
        symbol.fieldDescriptors().values().forEach(this::visitField);
        symbol.methods().values().forEach(this::visitMethod);
    }

    @Override
    protected void visit(RecordTypeSymbol symbol) {
        symbol.fieldDescriptors().values().forEach(this::visitField);
        symbol.restTypeDescriptor().ifPresent(this::visitType);
    }

    @Override
    protected void visit(StreamTypeSymbol symbol) {
        visitType(symbol.typeParameter());
        visitType(symbol.completionValueTypeParameter());
    }

    @Override
    protected void visit(TableTypeSymbol symbol) {
        visitType(symbol.rowTypeParameter());
        symbol.keyConstraintTypeParameter().ifPresent(this::visitType);
    }

    @Override
    protected void visit(TupleTypeSymbol symbol) {
        symbol.memberTypeDescriptors().forEach(this::visitType);
        symbol.restTypeDescriptor().ifPresent(this::visitType);
    }

    @Override
    protected void visit(ErrorTypeSymbol symbol) {
        String typeSignature = symbol.signature();
        String typeName = typeSignature.substring(typeSignature.lastIndexOf(':') + 1);
        findExternalRefType(symbol, typeName);
    }

    @Override
    protected void visit(TypeReferenceTypeSymbol symbol) {
        findExternalRefType(symbol, symbol.name());
    }

    @Override
    protected void visit(TypeSymbol symbol) {
    }

    @Override
    protected void visitType(TypeSymbol symbol) {
        // Guard to stop same type being visited twice - stop cyclic types
        if (visitedSymbols.contains(symbol)) {
            return;
        }
        visitedSymbols.add(symbol);
        super.visitType(symbol);
    }

    /**
     * Formats the type signature so that it can be used as a typedef. For example, int will be formatted to int.
     * ballerina/abc:1.0:pqr will be converted to 'imp1:pqr and an import added as import ballerina/abc.pqr as 'imp1.
     */
    private void findExternalRefType(TypeSymbol typeSymbol, String typeName) {
        String typeSignature = typeSymbol.signature();

        if (typeSymbol.moduleID().orgName().equals(ANON_MODULE) ||
                (typeSymbol.moduleID().moduleName().equals("lang.annotations")
                        && typeSymbol.moduleID().orgName().equals("ballerina"))) {
            // No import required. If the name is not found,
            // signature can be used without module parts.
            importReplaces.put(typeSignature, typeName);
            return;
        }

        // Compute the import statement
        String moduleName = Arrays.stream(typeSymbol.moduleID().moduleName().split("\\."))
                .map(StringUtils::quoted).collect(Collectors.joining("."));
        String fullModuleName = typeSymbol.moduleID().orgName() + "/" + moduleName;
        String defaultPrefix = StringUtils.quoted(typeSymbol.moduleID().modulePrefix());

        try {
            String importPrefix = importProcessor.processImplicitImport(fullModuleName, defaultPrefix);
            implicitImportPrefixes.add(importPrefix);
            String importedName = String.format("%s:%s", importPrefix, typeName);
            importReplaces.put(typeSignature, importedName);
        } catch (InvokerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds the type that should be included in generated code.
     * This will visit the types, find imports and return the type as a string.
     * Afterwards, use {@code getImplicitImportPrefixes} to get implicit imports.
     */
    public String computeType(TypeSymbol typeSymbol) {
        visitType(typeSymbol);
        String variableType = typeSymbol.signature();
        // Replace all found replacements
        for (Map.Entry<String, String> entry : importReplaces.entrySet()) {
            variableType = variableType.replace(entry.getKey(), entry.getValue());
        }
        return variableType;
    }

    /**
     * Get all the implicitly imported import prefixes.
     *
     * @return Set of implicit prefixes.
     */
    public Collection<String> getImplicitImportPrefixes() {
        return implicitImportPrefixes;
    }
}
