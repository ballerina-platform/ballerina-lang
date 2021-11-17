
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

import io.ballerina.compiler.api.impl.symbols.BallerinaAbsResourcePathAttachPoint;
import io.ballerina.compiler.api.impl.symbols.BallerinaAnnotationSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaClassFieldSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaClassSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaConstantSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaEnumSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaFunctionSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaLiteralAttachPoint;
import io.ballerina.compiler.api.impl.symbols.BallerinaMethodSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaModule;
import io.ballerina.compiler.api.impl.symbols.BallerinaObjectFieldSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaParameterSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaPathParameterSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaRecordFieldSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaResourceMethodSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaServiceDeclarationSymbol;
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
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEnumSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourceFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
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
    private final Types types;
    private final SymbolTable symTable;

    private SymbolFactory(CompilerContext context) {
        context.put(SYMBOL_FACTORY_KEY, this);
        this.context = context;
        this.typesFactory = TypesFactory.getInstance(context);
        this.types = Types.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
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
            if (symbol.kind == SymbolKind.FUNCTION && !isFunctionPointer(symbol)) {
                if (Symbols.isFlagOn(symbol.flags, Flags.ATTACHED)) {
                    if (Symbols.isFlagOn(symbol.flags, Flags.RESOURCE)) {
                        return createResourceMethodSymbol((BInvokableSymbol) symbol);
                    }
                    return createMethodSymbol((BInvokableSymbol) symbol, name);
                }
                return createFunctionSymbol((BInvokableSymbol) symbol, name);
            }
            if (symbol instanceof BConstantSymbol) {
                return createConstantSymbol((BConstantSymbol) symbol, name);
            }
            if (symbol.type instanceof BFutureType && ((BFutureType) symbol.type).workerDerivative) {
                return createWorkerSymbol((BVarSymbol) symbol, name);
            }
            if (symbol.owner instanceof BRecordTypeSymbol) {
                return createRecordFieldSymbol((BVarSymbol) symbol);
            }
            if (symbol.owner instanceof BClassSymbol) {
                return createClassFieldSymbol((BVarSymbol) symbol);
            }
            if (symbol.owner instanceof BObjectTypeSymbol) {
                return createObjectFieldSymbol((BVarSymbol) symbol);
            }
            if (Symbols.isFlagOn(symbol.flags, Flags.REQUIRED_PARAM)) {
                return createBallerinaParameter((BVarSymbol) symbol, ParameterKind.REQUIRED);
            }
            if (Symbols.isFlagOn(symbol.flags, Flags.DEFAULTABLE_PARAM)) {
                return createBallerinaParameter((BVarSymbol) symbol, ParameterKind.DEFAULTABLE);
            }
            if (Symbols.isFlagOn(symbol.flags, Flags.INCLUDED)) {
                return createBallerinaParameter((BVarSymbol) symbol, ParameterKind.INCLUDED_RECORD);
            }
            if (Symbols.isFlagOn(symbol.flags, Flags.REST_PARAM)) {
                return createBallerinaParameter((BVarSymbol) symbol, ParameterKind.REST);
            }
            if (symbol.kind == SymbolKind.PATH_PARAMETER) {
                return createPathParamSymbol((BVarSymbol) symbol, PathSegment.Kind.PATH_PARAMETER);
            }
            if (symbol.kind == SymbolKind.PATH_REST_PARAMETER) {
                return createPathParamSymbol((BVarSymbol) symbol, PathSegment.Kind.PATH_REST_PARAMETER);
            }

            // If the symbol is a wildcard('_'), a variable symbol will not be created.
            if (((BVarSymbol) symbol).isWildcard) {
                return null;
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
            if (symbol instanceof BEnumSymbol) {
                return createEnumSymbol((BEnumSymbol) symbol, name);
            }

            // For a type reference type symbol (SymTag.TYPE_REF)
            return createTypeDefinition(symbol, name);
        }

        if (symbol.kind == SymbolKind.TYPE_DEF) {
            // create the typeDefs
            return createTypeDefinition(symbol, name);
        }

        if (symbol.kind == SymbolKind.SERVICE) {
            return createServiceDeclSymbol((BServiceSymbol) symbol);
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
     * @return {@link Symbol}     generated
     */
    public BallerinaFunctionSymbol createFunctionSymbol(BInvokableSymbol invokableSymbol, String name) {
        BallerinaFunctionSymbol.FunctionSymbolBuilder builder =
                new BallerinaFunctionSymbol.FunctionSymbolBuilder(name, invokableSymbol, this.context);
        boolean isResourceMethod = Symbols.isFlagOn(invokableSymbol.flags, Flags.RESOURCE);
        boolean isRemoteMethod = Symbols.isFlagOn(invokableSymbol.flags, Flags.REMOTE);

        if (Symbols.isFlagOn(invokableSymbol.flags, Flags.PUBLIC) && !(isResourceMethod || isRemoteMethod)) {
            builder.withQualifier(Qualifier.PUBLIC);
        }
        if (Symbols.isFlagOn(invokableSymbol.flags, Flags.PRIVATE)) {
            builder.withQualifier(Qualifier.PRIVATE);
        }
        if (Symbols.isFlagOn(invokableSymbol.flags, Flags.ISOLATED)) {
            builder.withQualifier(Qualifier.ISOLATED);
        }
        if (isRemoteMethod) {
            builder.withQualifier(Qualifier.REMOTE);
        }
        if (isResourceMethod) {
            builder.withQualifier(Qualifier.RESOURCE);
        }
        if (Symbols.isFlagOn(invokableSymbol.flags, Flags.TRANSACTIONAL)) {
            builder.withQualifier(Qualifier.TRANSACTIONAL);
        }

        for (BLangAnnotationAttachment annAttachment : invokableSymbol.annAttachments) {
            if (annAttachment.annotationSymbol == null) {
                continue;
            }
            builder.withAnnotation(createAnnotationSymbol(annAttachment.annotationSymbol));
        }

        return builder.withTypeDescriptor((FunctionTypeSymbol) typesFactory
                .getTypeDescriptor(invokableSymbol.type, invokableSymbol.type.tsymbol, true))
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
            return new BallerinaMethodSymbol(functionSymbol, invokableSymbol, this.context);
        }

        throw new AssertionError("Invalid type descriptor found");
    }

    /**
     * Given a symbol for a resource method, returns a public resource method symbol instance.
     *
     * @param invokableSymbol The internal symbol for the method
     * @return The generated public symbol for the method
     */
    public BallerinaResourceMethodSymbol createResourceMethodSymbol(BInvokableSymbol invokableSymbol) {
        String name = getMethodName(invokableSymbol, (BObjectTypeSymbol) invokableSymbol.owner);
        TypeSymbol typeDescriptor = typesFactory.getTypeDescriptor(invokableSymbol.type);
        BallerinaFunctionSymbol functionSymbol = createFunctionSymbol(invokableSymbol, name);

        if (typeDescriptor.typeKind() == TypeDescKind.FUNCTION) {
            return new BallerinaResourceMethodSymbol(functionSymbol, invokableSymbol, this.context);
        }

        throw new AssertionError("Invalid type descriptor found");
    }

    /**
     * Create a generic variable symbol.
     *
     * @param symbol {@link BVarSymbol} to convert
     * @param name   symbol name
     * @return {@link BallerinaVariableSymbol} generated
     */
    public BallerinaVariableSymbol createVariableSymbol(BVarSymbol symbol, String name) {
        BallerinaVariableSymbol.VariableSymbolBuilder symbolBuilder =
                new BallerinaVariableSymbol.VariableSymbolBuilder(name, symbol, this.context);

        if (Symbols.isFlagOn(symbol.flags, Flags.FINAL) || Symbols.isFlagOn(symbol.flags, Flags.FUNCTION_FINAL)) {
            symbolBuilder.withQualifier(Qualifier.FINAL);
        }
        if (Symbols.isFlagOn(symbol.flags, Flags.LISTENER)) {
            symbolBuilder.withQualifier(Qualifier.LISTENER);
        }
        if (Symbols.isFlagOn(symbol.flags, Flags.READONLY)) {
            symbolBuilder.withQualifier(Qualifier.READONLY);
        }
        if (Symbols.isFlagOn(symbol.flags, Flags.PUBLIC)) {
            symbolBuilder.withQualifier(Qualifier.PUBLIC);
        }
        if (Symbols.isFlagOn(symbol.flags, Flags.CONFIGURABLE)) {
            symbolBuilder.withQualifier(Qualifier.CONFIGURABLE);
        }
        if (Symbols.isFlagOn(symbol.flags, Flags.ISOLATED)) {
            symbolBuilder.withQualifier(Qualifier.ISOLATED);
        }

        for (org.ballerinalang.model.symbols.AnnotationSymbol annot : symbol.getAnnotations()) {
            symbolBuilder.withAnnotation(createAnnotationSymbol((BAnnotationSymbol) annot));
        }

        return symbolBuilder
                .withTypeDescriptor(typesFactory.getTypeDescriptor(symbol.type))
                .build();
    }

    public BallerinaRecordFieldSymbol createRecordFieldSymbol(BVarSymbol symbol) {
        BField bField = getBField(symbol);
        return bField != null ? new BallerinaRecordFieldSymbol(this.context, bField) : null;
    }

    public BallerinaObjectFieldSymbol createObjectFieldSymbol(BVarSymbol symbol) {
        BField bField = getBField(symbol);
        return bField != null ? new BallerinaObjectFieldSymbol(this.context, bField) : null;
    }

    public BallerinaClassFieldSymbol createClassFieldSymbol(BVarSymbol symbol) {
        BField bField = getBField(symbol);
        return bField != null ? new BallerinaClassFieldSymbol(this.context, bField) : null;
    }

    public BallerinaWorkerSymbol createWorkerSymbol(BVarSymbol symbol, String name) {
        BallerinaWorkerSymbol.WorkerSymbolBuilder builder =
                new BallerinaWorkerSymbol.WorkerSymbolBuilder(name, symbol, this.context);

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
        String name = symbol.getOriginalName().getValue().isBlank() ? null : symbol.getOriginalName().getValue();
        TypeSymbol typeDescriptor = typesFactory.getTypeDescriptor(symbol.type);
        List<Qualifier> qualifiers = new ArrayList<>();
        if ((symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            qualifiers.add(Qualifier.PUBLIC);
        }

        List<AnnotationSymbol> annotSymbols = new ArrayList<>();
        for (org.ballerinalang.model.symbols.AnnotationSymbol annot : symbol.getAnnotations()) {
            annotSymbols.add(createAnnotationSymbol((BAnnotationSymbol) annot));
        }

        return new BallerinaParameterSymbol(name, typeDescriptor, qualifiers, annotSymbols, kind, symbol, this.context);
    }

    public PathParameterSymbol createPathParamSymbol(BVarSymbol symbol, PathSegment.Kind kind) {
        if (symbol == null) {
            return null;
        }
        return new BallerinaPathParameterSymbol(kind, symbol, this.context);
    }

    /**
     * Create a Ballerina Type Definition Symbol.
     *
     * @param typeSymbol type symbol to convert
     * @param name       symbol name
     * @return {@link}
     */
    public BallerinaTypeDefinitionSymbol createTypeDefinition(BSymbol typeSymbol, String name) {
        BallerinaTypeDefinitionSymbol.TypeDefSymbolBuilder symbolBuilder =
                new BallerinaTypeDefinitionSymbol.TypeDefSymbolBuilder(name, typeSymbol,
                                                                       this.context);

        if (Symbols.isFlagOn(typeSymbol.flags, Flags.PUBLIC)) {
            symbolBuilder.withQualifier(Qualifier.PUBLIC);
        }

        if (typeSymbol.kind == SymbolKind.TYPE_DEF) {
            for (BLangAnnotationAttachment annAttachment : ((BTypeDefinitionSymbol) typeSymbol).annAttachments) {
                if (annAttachment.annotationSymbol == null) {
                    continue;
                }
                symbolBuilder.withAnnotation(createAnnotationSymbol(annAttachment.annotationSymbol));
            }
        }

        return symbolBuilder.withTypeDescriptor(typesFactory.getTypeDescriptor(typeSymbol.type, typeSymbol, true))
                .build();
    }

    public BallerinaEnumSymbol createEnumSymbol(BEnumSymbol enumSymbol, String name) {
        BallerinaEnumSymbol.EnumSymbolBuilder symbolBuilder =
                new BallerinaEnumSymbol.EnumSymbolBuilder(name, enumSymbol, this.context);

        if (Symbols.isFlagOn(enumSymbol.flags, Flags.PUBLIC)) {
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
                .withTypeDescriptor(typesFactory.getTypeDescriptor(enumSymbol.type, enumSymbol, true))
                .build();
    }

    public BallerinaClassSymbol createClassSymbol(BClassSymbol classSymbol, String name) {
        TypeSymbol type = typesFactory.getTypeDescriptor(classSymbol.type, classSymbol, true);
        return createClassSymbol(classSymbol, name, type);
    }

    public BallerinaClassSymbol createClassSymbol(BClassSymbol classSymbol, String name, TypeSymbol type) {
        BallerinaClassSymbol.ClassSymbolBuilder symbolBuilder =
                new BallerinaClassSymbol.ClassSymbolBuilder(this.context, name, classSymbol);

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

    public BallerinaServiceDeclarationSymbol createServiceDeclSymbol(BServiceSymbol serviceDeclSymbol) {
        BallerinaServiceDeclarationSymbol.ServiceDeclSymbolBuilder symbolBuilder =
                new BallerinaServiceDeclarationSymbol.ServiceDeclSymbolBuilder(serviceDeclSymbol, this.context);
        BClassSymbol associatedClass = serviceDeclSymbol.getAssociatedClassSymbol();

        if (Symbols.isFlagOn(serviceDeclSymbol.flags, Flags.ISOLATED)) {
            symbolBuilder.withQualifier(Qualifier.ISOLATED);
        }

        for (org.ballerinalang.model.symbols.AnnotationSymbol annot : associatedClass.getAnnotations()) {
            symbolBuilder.withAnnotation(createAnnotationSymbol((BAnnotationSymbol) annot));
        }

        if (serviceDeclSymbol.getAbsResourcePath().isPresent()) {
            symbolBuilder.withAttachPoint(
                    new BallerinaAbsResourcePathAttachPoint(serviceDeclSymbol.getAbsResourcePath().get()));
        } else if (serviceDeclSymbol.getAttachPointStringLiteral().isPresent()) {
            symbolBuilder.withAttachPoint(
                    new BallerinaLiteralAttachPoint(serviceDeclSymbol.getAttachPointStringLiteral().get()));
        }

        return symbolBuilder.withTypeDescriptor(typesFactory.getTypeDescriptor(serviceDeclSymbol.type)).build();
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
                new BallerinaConstantSymbol.ConstantSymbolBuilder(name, constantSymbol,
                                                                  this.context);
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
                new BallerinaAnnotationSymbol.AnnotationSymbolBuilder(symbol.getOriginalName().getValue(), symbol,
                        this.context);
        if ((symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            symbolBuilder.withQualifier(Qualifier.PUBLIC);
        }

        // Skipping the compiler-generated singleton type `true`.
        if (symbol.attachedType != null && !types.isAssignable(symbol.attachedType, this.symTable.trueType)) {
            symbolBuilder.withTypeDescriptor(typesFactory.getTypeDescriptor(symbol.attachedType));
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
                new BallerinaXMLNSSymbol.XmlNSSymbolBuilder(symbol.name.getValue(), symbol, this.context);

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
        return new BallerinaModule.ModuleSymbolBuilder(this.context, name, symbol).build();
    }

    // Private methods

    private String getMethodName(BInvokableSymbol method, BObjectTypeSymbol owner) {
        List<BAttachedFunction> methods = new ArrayList<>(owner.attachedFuncs);
        methods.add(owner.initializerFunc);

        for (BAttachedFunction mthd : methods) {
            if (method == mthd.symbol) {
                if (mthd instanceof BResourceFunction) {
                    return ((BResourceFunction) mthd).accessor.value;
                }
                return mthd.funcName.value;
            }
        }

        throw new IllegalStateException(
                format("Method symbol for '%s' not found in owner symbol '%s'", method.name, owner.name));
    }

    private boolean isFunctionPointer(BSymbol symbol) {
        return Symbols.isTagOn(symbol, SymTag.VARIABLE) && !Symbols.isTagOn(symbol, SymTag.FUNCTION);
    }

    private void addIfFlagSet(BallerinaClassSymbol.ClassSymbolBuilder symbolBuilder, final long mask, final long flag,
                              Qualifier qualifier) {
        if (Symbols.isFlagOn(mask, flag)) {
            symbolBuilder.withQualifier(qualifier);
        }
    }

    private BField getBField(BVarSymbol symbol) {
        String fieldName = symbol.name.value;
        BStructureType type = (BStructureType) symbol.owner.type;
        return type.fields.get(fieldName);
    }
}
