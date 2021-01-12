
/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl;

import io.ballerina.compiler.api.impl.symbols.BallerinaAnnotationSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaClassSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaConstantSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaEnumSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaFunctionSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaMethodSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaModule;
import io.ballerina.compiler.api.impl.symbols.BallerinaParameterSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaTypeDefinitionSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaVariableSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaWorkerSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaXMLNSSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEnumSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Represents a set of factory methods to generate the {@link Symbol}s.
 *
 * @since 2.0.0
 */
public class SymbolFactory {

    private static final CompilerContext.Key<SymbolFactory> SYMBOL_FACTORY_KEY = new CompilerContext.Key<>();

    private final CompilerContext context;
    private final TypesFactory typesFactory;

    private SymbolFactory(CompilerContext context) {
        context.put(SYMBOL_FACTORY_KEY, this);
        this.context = context;
        this.typesFactory = TypesFactory.getInstance(context);
    }

    public static SymbolFactory getInstance(CompilerContext context) {
        SymbolFactory symbolFactory = context.get(SYMBOL_FACTORY_KEY);
        if (symbolFactory == null) {
            symbolFactory = new SymbolFactory(context);
        }

        return symbolFactory;
    }

    /**
     * Get the matching {@link Symbol} for a given {@link BSymbol}.
     *
     * @param symbol BSymbol to generated the BCompiled Symbol
     * @param name   symbol name
     * @return generated compiled symbol
     */
    public Symbol getBCompiledSymbol(BSymbol symbol, String name) {

        if (symbol == null) {
            throw new IllegalArgumentException("Symbol is 'null'");
        }

        if (symbol instanceof BVarSymbol) {
            if (symbol.kind == SymbolKind.FUNCTION) {
                if (Symbols.isFlagOn(symbol.flags, Flags.ATTACHED)) {
                    return createMethodSymbol((BInvokableSymbol) symbol);
                }
                return createFunctionSymbol((BInvokableSymbol) symbol, name);
            }
            if (symbol instanceof BConstantSymbol) {
                return createConstantSymbol((BConstantSymbol) symbol, name);
            }
            if (symbol.type instanceof BFutureType && ((BFutureType) symbol.type).workerDerivative) {
                return createWorkerSymbol((BVarSymbol) symbol, name);
            }

            // return the variable symbol
            return createVariableSymbol((BVarSymbol) symbol, name);
        }

        if (symbol instanceof BTypeSymbol) {
            if (symbol.kind == SymbolKind.ANNOTATION) {
                return createAnnotationSymbol((BAnnotationSymbol) symbol);
            }
            if (symbol instanceof BPackageSymbol) {
                return createModuleSymbol((BPackageSymbol) symbol, name);
            }
            if (symbol instanceof BClassSymbol) {
                return createClassSymbol((BClassSymbol) symbol, name);
            }
            if (Symbols.isFlagOn(symbol.flags, Flags.ENUM)) {
                return createEnumSymbol((BEnumSymbol) symbol, name);
            }

            // create the typeDefs
            return createTypeDefinition((BTypeSymbol) symbol, name);
        }

        if (symbol.kind == SymbolKind.XMLNS) {
            return createXMLNamespaceSymbol((BXMLNSSymbol) symbol);
        }

        throw new IllegalArgumentException("Unsupported symbol type: " + symbol.getClass().getName());
    }

    /**
     * Create Function Symbol.
     *
     * @param invokableSymbol {@link BInvokableSymbol} to convert
     * @param name            symbol name
     * @return {@link Symbol} generated
     */
    public BallerinaFunctionSymbol createFunctionSymbol(BInvokableSymbol invokableSymbol, String name) {
        PackageID pkgID = invokableSymbol.pkgID;
        BallerinaFunctionSymbol.FunctionSymbolBuilder builder =
                new BallerinaFunctionSymbol.FunctionSymbolBuilder(name, pkgID, invokableSymbol);
        boolean isResourceMethod = isFlagOn(invokableSymbol.flags, Flags.RESOURCE);
        boolean isRemoteMethod = isFlagOn(invokableSymbol.flags, Flags.REMOTE);

        if (isFlagOn(invokableSymbol.flags, Flags.PUBLIC) && !(isResourceMethod || isRemoteMethod)) {
            builder.withQualifier(Qualifier.PUBLIC);
        }
        if (isFlagOn(invokableSymbol.flags, Flags.PRIVATE)) {
            builder.withQualifier(Qualifier.PRIVATE);
        }
        if (isFlagOn(invokableSymbol.flags, Flags.ISOLATED)) {
            builder.withQualifier(Qualifier.ISOLATED);
        }
        if (isRemoteMethod) {
            builder.withQualifier(Qualifier.REMOTE);
        }
        if (isResourceMethod) {
            builder.withQualifier(Qualifier.RESOURCE);
        }
        if (isFlagOn(invokableSymbol.flags, Flags.TRANSACTIONAL)) {
            builder.withQualifier(Qualifier.TRANSACTIONAL);
        }

        for (BLangAnnotationAttachment annAttachment : invokableSymbol.annAttachments) {
            if (annAttachment.annotationSymbol == null) {
                continue;
            }
            builder.withAnnotation(createAnnotationSymbol(annAttachment.annotationSymbol));
        }

        return builder.withTypeDescriptor((FunctionTypeSymbol) typesFactory.getTypeDescriptor(invokableSymbol.type))
                .build();
    }

    /**
     * Create Method Symbol.
     *
     * @param invokableSymbol {@link BInvokableSymbol} to convert
     * @param name            symbol name
     * @return {@link Symbol} generated
     */
    public BallerinaMethodSymbol createMethodSymbol(BInvokableSymbol invokableSymbol, String name) {
        TypeSymbol typeDescriptor = typesFactory.getTypeDescriptor(invokableSymbol.type);
        BallerinaFunctionSymbol functionSymbol = createFunctionSymbol(invokableSymbol, name);
        if (typeDescriptor.typeKind() == TypeDescKind.FUNCTION) {
            return new BallerinaMethodSymbol(functionSymbol);
        }

        throw new AssertionError("Invalid type descriptor found");
    }

    /**
     * Create a Method Symbol.
     *
     * @param invokableSymbol {@link BInvokableSymbol} to convert
     * @return {@link Symbol} generated
     */
    private BallerinaMethodSymbol createMethodSymbol(BInvokableSymbol invokableSymbol) {
        String name = getMethodName(invokableSymbol, (BObjectTypeSymbol) invokableSymbol.owner);
        return createMethodSymbol(invokableSymbol, name);
    }

    /**
     * Create a generic variable symbol.
     *
     * @param symbol {@link BVarSymbol} to convert
     * @param name   symbol name
     * @return {@link BallerinaVariableSymbol} generated
     */
    public BallerinaVariableSymbol createVariableSymbol(BVarSymbol symbol, String name) {
        PackageID pkgID = symbol.pkgID;
        BallerinaVariableSymbol.VariableSymbolBuilder symbolBuilder =
                new BallerinaVariableSymbol.VariableSymbolBuilder(name, pkgID, symbol);

        if (isFlagOn(symbol.flags, Flags.FINAL) || isFlagOn(symbol.flags, Flags.FUNCTION_FINAL)) {
            symbolBuilder.withQualifier(Qualifier.FINAL);
        }
        if (isFlagOn(symbol.flags, Flags.LISTENER)) {
            symbolBuilder.withQualifier(Qualifier.LISTENER);
        }
        if (isFlagOn(symbol.flags, Flags.READONLY)) {
            symbolBuilder.withQualifier(Qualifier.READONLY);
        }
        if (isFlagOn(symbol.flags, Flags.PUBLIC)) {
            symbolBuilder.withQualifier(Qualifier.PUBLIC);
        }
        if (isFlagOn(symbol.flags, Flags.CONFIGURABLE)) {
            symbolBuilder.withQualifier(Qualifier.CONFIGURABLE);
        }

        for (org.ballerinalang.model.symbols.AnnotationSymbol annot : symbol.getAnnotations()) {
            symbolBuilder.withAnnotation(createAnnotationSymbol((BAnnotationSymbol) annot));
        }

        return symbolBuilder
                .withTypeDescriptor(typesFactory.getTypeDescriptor(symbol.type))
                .build();
    }

    public BallerinaWorkerSymbol createWorkerSymbol(BVarSymbol symbol, String name) {
        BallerinaWorkerSymbol.WorkerSymbolBuilder builder =
                new BallerinaWorkerSymbol.WorkerSymbolBuilder(name, symbol.pkgID, symbol);

        for (org.ballerinalang.model.symbols.AnnotationSymbol annot : symbol.getAnnotations()) {
            builder.withAnnotation(createAnnotationSymbol((BAnnotationSymbol) annot));
        }

        return builder.withReturnType(typesFactory.getTypeDescriptor(((BFutureType) symbol.type).constraint)).build();
    }

    /**
     * Create a ballerina parameter.
     *
     * @param symbol Variable symbol for the parameter
     * @param kind   The kind of the parameter
     * @return {@link ParameterSymbol} generated parameter
     */
    public ParameterSymbol createBallerinaParameter(BVarSymbol symbol, ParameterKind kind) {
        if (symbol == null) {
            return null;
        }
        String name = symbol.getName().getValue().isBlank() ? null : symbol.getName().getValue();
        TypeSymbol typeDescriptor = typesFactory.getTypeDescriptor(symbol.getType());
        List<Qualifier> qualifiers = new ArrayList<>();
        if ((symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            qualifiers.add(Qualifier.PUBLIC);
        }

        List<AnnotationSymbol> annotSymbols = new ArrayList<>();
        for (org.ballerinalang.model.symbols.AnnotationSymbol annot : symbol.getAnnotations()) {
            annotSymbols.add(createAnnotationSymbol((BAnnotationSymbol) annot));
        }

        return new BallerinaParameterSymbol(name, typeDescriptor, qualifiers, annotSymbols, kind);
    }

    /**
     * Create a Ballerina Type Definition Symbol.
     *
     * @param typeSymbol type symbol to convert
     * @param name       symbol name
     * @return {@link}
     */
    public BallerinaTypeDefinitionSymbol createTypeDefinition(BTypeSymbol typeSymbol, String name) {
        BallerinaTypeDefinitionSymbol.TypeDefSymbolBuilder symbolBuilder =
                new BallerinaTypeDefinitionSymbol.TypeDefSymbolBuilder(name, typeSymbol.pkgID, typeSymbol);

        if (isFlagOn(typeSymbol.flags, Flags.PUBLIC)) {
            symbolBuilder.withQualifier(Qualifier.PUBLIC);
        }

        return symbolBuilder.withTypeDescriptor(typesFactory.getTypeDescriptor(typeSymbol.type, true)).build();
    }

    public BallerinaEnumSymbol createEnumSymbol(BEnumSymbol enumSymbol, String name) {
        BallerinaEnumSymbol.EnumSymbolBuilder symbolBuilder =
                new BallerinaEnumSymbol.EnumSymbolBuilder(name, enumSymbol.pkgID, enumSymbol);

        if (isFlagOn(enumSymbol.flags, Flags.PUBLIC)) {
            symbolBuilder.withQualifier(Qualifier.PUBLIC);
        }

        List<ConstantSymbol> members = new ArrayList<>();
        for (BConstantSymbol member : enumSymbol.members) {
            members.add(this.createConstantSymbol(member, member.name.value));
        }

        for (org.ballerinalang.model.symbols.AnnotationSymbol annot : enumSymbol.getAnnotations()) {
            symbolBuilder.withAnnotation(createAnnotationSymbol((BAnnotationSymbol) annot));
        }

        return symbolBuilder
                .withMembers(members)
                .withTypeDescriptor(typesFactory.getTypeDescriptor(enumSymbol.type, true))
                .build();
    }

    public BallerinaClassSymbol createClassSymbol(BClassSymbol classSymbol, String name) {
        TypeSymbol type = typesFactory.getTypeDescriptor(classSymbol.type, true);
        return createClassSymbol(classSymbol, name, type);
    }

    public BallerinaClassSymbol createClassSymbol(BClassSymbol classSymbol, String name, TypeSymbol type) {
        BallerinaClassSymbol.ClassSymbolBuilder symbolBuilder =
                new BallerinaClassSymbol.ClassSymbolBuilder(this.context, name, classSymbol.pkgID, classSymbol);

        addIfFlagSet(symbolBuilder, classSymbol.flags, Flags.PUBLIC, Qualifier.PUBLIC);
        addIfFlagSet(symbolBuilder, classSymbol.flags, Flags.DISTINCT, Qualifier.DISTINCT);
        addIfFlagSet(symbolBuilder, classSymbol.flags, Flags.READONLY, Qualifier.READONLY);
        addIfFlagSet(symbolBuilder, classSymbol.flags, Flags.ISOLATED, Qualifier.ISOLATED);
        addIfFlagSet(symbolBuilder, classSymbol.flags, Flags.CLIENT, Qualifier.CLIENT);
        addIfFlagSet(symbolBuilder, classSymbol.flags, Flags.SERVICE, Qualifier.SERVICE);

        if (type.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            type = ((TypeReferenceTypeSymbol) type).typeDescriptor();
        }

        for (org.ballerinalang.model.symbols.AnnotationSymbol annot : classSymbol.getAnnotations()) {
            symbolBuilder.withAnnotation(createAnnotationSymbol((BAnnotationSymbol) annot));
        }

        return symbolBuilder.withTypeDescriptor((ObjectTypeSymbol) type).build();
    }

    /**
     * Create a constant symbol.
     *
     * @param constantSymbol BConstantSymbol to convert
     * @param name           symbol name
     * @return {@link BallerinaConstantSymbol} generated
     */
    public BallerinaConstantSymbol createConstantSymbol(BConstantSymbol constantSymbol, String name) {
        BallerinaConstantSymbol.ConstantSymbolBuilder symbolBuilder =
                new BallerinaConstantSymbol.ConstantSymbolBuilder(name, constantSymbol.pkgID, constantSymbol);
        symbolBuilder.withConstValue(constantSymbol.getConstValue())
                .withTypeDescriptor(typesFactory.getTypeDescriptor(constantSymbol.type))
                .withBroaderTypeDescriptor(typesFactory.getTypeDescriptor(constantSymbol.literalType));

        if ((constantSymbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            symbolBuilder.withQualifier(Qualifier.PUBLIC);
        }

        for (org.ballerinalang.model.symbols.AnnotationSymbol annot : constantSymbol.getAnnotations()) {
            symbolBuilder.withAnnotation(createAnnotationSymbol((BAnnotationSymbol) annot));
        }

        return symbolBuilder.build();
    }

    /**
     * Creates an annotation Symbol.
     *
     * @param symbol Annotation symbol to convert
     * @return {@link BallerinaAnnotationSymbol}
     */
    public BallerinaAnnotationSymbol createAnnotationSymbol(BAnnotationSymbol symbol) {
        BallerinaAnnotationSymbol.AnnotationSymbolBuilder symbolBuilder =
                new BallerinaAnnotationSymbol.AnnotationSymbolBuilder(symbol.name.getValue(), symbol.pkgID, symbol);
        if ((symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            symbolBuilder.withQualifier(Qualifier.PUBLIC);
        }
        if (symbol.attachedType != null && symbol.attachedType.getType() != null) {
            symbolBuilder.withTypeDescriptor(typesFactory.getTypeDescriptor(symbol.attachedType.getType()));
        }

        for (org.ballerinalang.model.symbols.AnnotationSymbol annot : symbol.getAnnotations()) {
            symbolBuilder.withAnnotation(createAnnotationSymbol((BAnnotationSymbol) annot));
        }

        return symbolBuilder.build();
    }

    /**
     * Creates an annotation Symbol.
     *
     * @param symbol Annotation symbol to convert
     * @return {@link BallerinaAnnotationSymbol}
     */
    public BallerinaXMLNSSymbol createXMLNamespaceSymbol(BXMLNSSymbol symbol) {
        BallerinaXMLNSSymbol.XmlNSSymbolBuilder symbolBuilder =
                new BallerinaXMLNSSymbol.XmlNSSymbolBuilder(symbol.name.getValue(), symbol.pkgID, symbol);

        return symbolBuilder.build();
    }

    /**
     * Create a module symbol.
     *
     * @param symbol Package Symbol to evaluate
     * @param name   symbol name
     * @return {@link BallerinaModule} symbol generated
     */
    public BallerinaModule createModuleSymbol(BPackageSymbol symbol, String name) {
        return new BallerinaModule.ModuleSymbolBuilder(this.context, name, symbol.pkgID, symbol).build();
    }

    // Private methods
    private static boolean isFlagOn(long mask, long flags) {
        return (mask & flags) == flags;
    }

    private String getMethodName(BInvokableSymbol method, BObjectTypeSymbol owner) {
        List<BAttachedFunction> methods = new ArrayList<>(owner.attachedFuncs);
        methods.add(owner.initializerFunc);

        for (BAttachedFunction mthd : methods) {
            if (method == mthd.symbol) {
                return mthd.funcName.value;
            }
        }

        throw new IllegalStateException(
                format("Method symbol for '%s' not found in owner symbol '%s'", method.name, owner.name));
    }

    private void addIfFlagSet(BallerinaClassSymbol.ClassSymbolBuilder symbolBuilder, final long mask, final long flag,
                              Qualifier qualifier) {
        if (Symbols.isFlagOn(mask, flag)) {
            symbolBuilder.withQualifier(qualifier);
        }
    }
}
