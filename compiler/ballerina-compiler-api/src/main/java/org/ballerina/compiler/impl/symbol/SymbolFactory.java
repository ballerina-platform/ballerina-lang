/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerina.compiler.impl.symbol;

import org.ballerina.compiler.api.element.BallerinaParameter;
import org.ballerina.compiler.api.symbol.BCompiledSymbol;
import org.ballerina.compiler.api.symbol.Qualifier;
import org.ballerina.compiler.api.type.BallerinaTypeDescriptor;
import org.ballerina.compiler.api.type.TypeDescKind;
import org.ballerina.compiler.impl.semantic.TypesFactory;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a set of factory methods to generate the {@link BCompiledSymbol}s.
 *
 * @since 1.3.0
 */
public class SymbolFactory {

    /**
     * Get the matching {@link BCompiledSymbol} for a given {@link BSymbol}.
     *
     * @param symbol BSymbol to generated the BCompiled Symbol
     * @param name   symbol name
     * @return generated compiled symbol
     */
    public static BCompiledSymbol getBCompiledSymbol(BSymbol symbol, String name) {
        if (symbol instanceof BVarSymbol) {
            if (symbol.kind == SymbolKind.FUNCTION) {
                return createFunctionSymbol((BInvokableSymbol) symbol, name);
            }
            if (symbol.kind == SymbolKind.ERROR_CONSTRUCTOR) {
                return createTypeDefinition(symbol.type.tsymbol, name);
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
            // create the typeDefs
            return createTypeDefinition((BTypeSymbol) symbol, name);
        }

        return null;
    }

    /**
     * Create Function Symbol.
     *
     * @param invokableSymbol {@link BInvokableSymbol} to convert
     * @param name            symbol name
     * @return {@link BCompiledSymbol} generated
     */
    public static FunctionSymbolImplImpl createFunctionSymbol(BInvokableSymbol invokableSymbol, String name) {
        PackageID pkgID = invokableSymbol.pkgID;
        FunctionSymbolImplImpl.FunctionSymbolBuilder builder =
                new FunctionSymbolImplImpl.FunctionSymbolBuilder(name, pkgID, invokableSymbol);
        if ((invokableSymbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            builder.withQualifier(Qualifier.PUBLIC);
        }
        if ((invokableSymbol.flags & Flags.PRIVATE) == Flags.PRIVATE) {
            builder.withQualifier(Qualifier.PRIVATE);
        }

        return builder.build();
    }

    /**
     * Create Method Symbol.
     *
     * @param invokableSymbol {@link BInvokableSymbol} to convert
     * @param name            symbol name
     * @return {@link BCompiledSymbol} generated
     */
    public static MethodSymbolImpl createMethodSymbol(BInvokableSymbol invokableSymbol, String name) {
        BallerinaTypeDescriptor typeDescriptor = TypesFactory.getTypeDescriptor(invokableSymbol.type);
        FunctionSymbolImplImpl functionSymbol = SymbolFactory.createFunctionSymbol(invokableSymbol, name);
        if (typeDescriptor.kind() == TypeDescKind.FUNCTION) {
            return new MethodSymbolImpl(functionSymbol);
        }

        throw new AssertionError("Invalid type descriptor found");
    }

    /**
     * Create a generic variable symbol.
     *
     * @param symbol {@link BVarSymbol} to convert
     * @param name   symbol name
     * @return {@link BallerinaVariableSymbolImpl} generated
     */
    public static BallerinaVariableSymbolImpl createVariableSymbol(BVarSymbol symbol, String name) {
        PackageID pkgID = symbol.pkgID;
        BallerinaVariableSymbolImpl.VariableSymbolBuilder symbolBuilder =
                new BallerinaVariableSymbolImpl.VariableSymbolBuilder(name, pkgID, symbol);

        return symbolBuilder
                .withTypeDescriptor(TypesFactory.getTypeDescriptor(symbol.type))
                .build();
    }

    public static BallerinaWorkerSymbolImpl createWorkerSymbol(BVarSymbol symbol, String name) {
        return new BallerinaWorkerSymbolImpl.WorkerSymbolBuilder(name, symbol.pkgID, symbol)
                .withReturnType(TypesFactory.getTypeDescriptor(((BFutureType) symbol.type).constraint))
                .build();
    }

    /**
     * Create a ballerina parameter.
     *
     * @param symbol Variable symbol for the parameter
     * @return {@link BallerinaParameter} generated parameter
     */
    public static BallerinaParameter createBallerinaParameter(BVarSymbol symbol) {
        if (symbol == null) {
            return null;
        }
        String name = symbol.getName().getValue();
        BallerinaTypeDescriptor typeDescriptor = TypesFactory.getTypeDescriptor(symbol.getType());
        List<Qualifier> qualifiers = new ArrayList<>();
        if ((symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            qualifiers.add(Qualifier.PUBLIC);
        }
        return new BallerinaParameter(name, typeDescriptor, qualifiers, symbol.defaultableParam);
    }

    /**
     * Create a Ballerina Type Definition Symbol.
     *
     * @param typeSymbol type symbol to convert
     * @param name       symbol name
     * @return {@link}
     */
    public static TypeDefinitionImpl createTypeDefinition(BTypeSymbol typeSymbol, String name) {
        TypeDefinitionImpl.TypeDefSymbolBuilder symbolBuilder =
                new TypeDefinitionImpl.TypeDefSymbolBuilder(name,
                        typeSymbol.pkgID,
                        typeSymbol);
        if ((typeSymbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            symbolBuilder.withAccessModifier(Qualifier.PUBLIC);
        }

        return symbolBuilder.withTypeDescriptor(TypesFactory.getTypeDescriptor(typeSymbol.type))
                .build();
    }

    /**
     * Create a constant symbol.
     *
     * @param constantSymbol BConstantSymbol to convert
     * @param name           symbol name
     * @return {@link ConstantSymbolSymbolImpl} generated
     */
    public static ConstantSymbolSymbolImpl createConstantSymbol(BConstantSymbol constantSymbol, String name) {
        ConstantSymbolSymbolImpl.ConstantSymbolBuilder symbolBuilder =
                new ConstantSymbolSymbolImpl.ConstantSymbolBuilder(name, constantSymbol.pkgID, constantSymbol);
        return symbolBuilder.withConstValue(constantSymbol.getConstValue())
                .withTypeDescriptor(TypesFactory.getTypeDescriptor(constantSymbol.literalType))
                .build();
    }

    /**
     * Creates an annotation Symbol.
     *
     * @param symbol Annotation symbol to convert
     * @return {@link AnnotationSymbolImplImpl}
     */
    public static AnnotationSymbolImplImpl createAnnotationSymbol(BAnnotationSymbol symbol) {
        return new AnnotationSymbolImplImpl.AnnotationSymbolBuilder(symbol.name.getValue(), symbol.pkgID, symbol)
                .withTypeDescriptor(TypesFactory.getTypeDescriptor(symbol.attachedType.getType()))
                .build();
    }

    /**
     * Create a module symbol.
     *
     * @param symbol Package Symbol to evaluate
     * @param name   symbol name
     * @return {@link BallerinaModuleImpl} symbol generated
     */
    public static BallerinaModuleImpl createModuleSymbol(BPackageSymbol symbol, String name) {
        return new BallerinaModuleImpl.ModuleSymbolBuilder(name, symbol.pkgID, symbol).build();
    }
}
