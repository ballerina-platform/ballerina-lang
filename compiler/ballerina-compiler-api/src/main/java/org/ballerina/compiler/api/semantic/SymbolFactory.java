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
package org.ballerina.compiler.api.semantic;

import org.ballerina.compiler.api.model.AccessModifier;
import org.ballerina.compiler.api.model.BCompiledSymbol;
import org.ballerina.compiler.api.model.BallerinaAnnotationSymbol;
import org.ballerina.compiler.api.model.BallerinaConstantSymbol;
import org.ballerina.compiler.api.model.BallerinaFunctionSymbol;
import org.ballerina.compiler.api.model.BallerinaObjectVarSymbol;
import org.ballerina.compiler.api.model.BallerinaParameter;
import org.ballerina.compiler.api.model.BallerinaRecordVarSymbol;
import org.ballerina.compiler.api.model.BallerinaTypeDefinition;
import org.ballerina.compiler.api.model.BallerinaVariable;
import org.ballerina.compiler.api.model.BallerinaWorkerSymbol;
import org.ballerina.compiler.api.types.TypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
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
     * @return generated compiled symbol
     */
    public static BCompiledSymbol getBCompiledSymbol(BSymbol symbol) {
        if (symbol instanceof BVarSymbol) {
            if (symbol.kind == SymbolKind.FUNCTION) {
                return createFunctionSymbol((BInvokableSymbol) symbol);
            }
            if (symbol.kind == SymbolKind.CONSTANT) {
                return createConstantSymbol((BConstantSymbol) symbol);
            }
            if (symbol.type instanceof BFutureType && ((BFutureType) symbol.type).workerDerivative) {
                return createWorkerSymbol((BVarSymbol) symbol);
            }
            if (symbol.type != null && symbol.type.tsymbol != null && symbol.type.tsymbol.kind == SymbolKind.OBJECT) {
                return createObjectVarSymbol(symbol.name.getValue(), (BObjectTypeSymbol) symbol.type.tsymbol);
            }
            if (symbol.type != null && symbol.type.tsymbol != null && symbol.type.tsymbol.kind == SymbolKind.RECORD) {
                return createRecordVarSymbol(symbol.name.getValue(), (BRecordTypeSymbol) symbol.type.tsymbol);
            }
            // return the variable symbol
            return createVariableSymbol((BVarSymbol) symbol);
        }
        
        if (symbol instanceof BTypeSymbol) {
            if (symbol.kind == SymbolKind.ANNOTATION) {
                return createAnnotationSymbol((BAnnotationSymbol) symbol);
            }
            // create the typeDefs
            return createTypeDefinition((BTypeSymbol) symbol);
        }
        
        return null;
    }

    /**
     * Create Function Symbol.
     * 
     * @param invokableSymbol {@link BInvokableSymbol} to convert
     * @return {@link BCompiledSymbol} generated
     */
    public static BallerinaFunctionSymbol createFunctionSymbol(BInvokableSymbol invokableSymbol) {
        String name = invokableSymbol.getName().getValue();
        PackageID pkgID = invokableSymbol.pkgID;
        return new BallerinaFunctionSymbol.FunctionSymbolBuilder(name, pkgID, invokableSymbol).build();
    }

    /**
     * Create an Object Symbol.
     * 
     * @param name name of the variable
     * @param objectTypeSymbol ObjectTypeSymbol to convert
     * @return {@link BallerinaObjectVarSymbol} generated
     */
    public static BallerinaObjectVarSymbol createObjectVarSymbol(String name, BObjectTypeSymbol objectTypeSymbol) {
        if (objectTypeSymbol == null) {
            return null;
        }
        return new BallerinaObjectVarSymbol.ObjectVarSymbolBuilder(name,
                objectTypeSymbol.pkgID,
                objectTypeSymbol).build();

    }

    /**
     * Create a Record variable Symbol.
     *
     * @param name name of the variable
     * @param recordTypeSymbol BRecordTypeSymbol to convert
     * @return {@link BallerinaObjectVarSymbol} generated
     */
    public static BallerinaRecordVarSymbol createRecordVarSymbol(String name, BRecordTypeSymbol recordTypeSymbol) {
        if (recordTypeSymbol == null) {
            return null;
        }
        BallerinaRecordVarSymbol.RecordVarSymbolBuilder symbolBuilder =
                new BallerinaRecordVarSymbol.RecordVarSymbolBuilder(name, recordTypeSymbol.pkgID, recordTypeSymbol);
        if ((recordTypeSymbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            symbolBuilder.withAccessModifier(AccessModifier.PUBLIC);
        }
        
        return symbolBuilder
                .withTypeDescriptor(TypesFactory.getTypeDescriptor(recordTypeSymbol.type))
                .build();
    }

    /**
     * Create a generic variable symbol.
     * 
     * @param symbol {@link BVarSymbol} to convert
     * @return {@link BallerinaVariable} generated
     */
    public static BallerinaVariable createVariableSymbol(BVarSymbol symbol) {
        String name = symbol.getName().getValue();
        PackageID pkgID = symbol.pkgID;
        BallerinaVariable.VariableSymbolBuilder symbolBuilder =
                new BallerinaVariable.VariableSymbolBuilder(name, pkgID, symbol);
        
        return symbolBuilder
                .withTypeDescriptor(TypesFactory.getTypeDescriptor(symbol.type))
                .build();
    }
    
    public static BallerinaWorkerSymbol createWorkerSymbol(BVarSymbol symbol) {
        return new BallerinaWorkerSymbol.WorkerSymbolBuilder(symbol.name.getValue(), symbol.pkgID, symbol)
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
        TypeDescriptor typeDescriptor = TypesFactory.getTypeDescriptor(symbol.getType());
        List<AccessModifier> accessModifiers = new ArrayList<>();
        if ((symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            accessModifiers.add(AccessModifier.PUBLIC);
        }
        
        return new BallerinaParameter(name, typeDescriptor, accessModifiers);
    }

    /**
     * Create a Ballerina Type Definition Symbol.
     * 
     * @param typeSymbol type symbol to convert
     * @return {@link} 
     */
    public static BallerinaTypeDefinition createTypeDefinition(BTypeSymbol typeSymbol) {
        BallerinaTypeDefinition.TypeDefSymbolBuilder symbolBuilder =
                new BallerinaTypeDefinition.TypeDefSymbolBuilder(typeSymbol.getName().getValue(),
                        typeSymbol.pkgID,
                        typeSymbol);
        if ((typeSymbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            symbolBuilder.withAccessModifier(AccessModifier.PUBLIC);
        }
        
        return symbolBuilder.withTypeDescriptor(TypesFactory.getTypeDescriptor(typeSymbol.type))
                .build();
    }

    /**
     * Create a constant symbol.
     * 
     * @param constantSymbol BConstantSymbol to convert
     * @return {@link BallerinaConstantSymbol} generated
     */
    public static BallerinaConstantSymbol createConstantSymbol(BConstantSymbol constantSymbol) {
        String name = constantSymbol.name.getValue();
        BallerinaConstantSymbol.ConstantSymbolBuilder symbolBuilder =
                new BallerinaConstantSymbol.ConstantSymbolBuilder(name, constantSymbol.pkgID, constantSymbol);
        return symbolBuilder.withConstValue(constantSymbol.getConstValue()).build();
    }

    /**
     * Creates an annotation Symbol.
     * 
     * @param symbol Annotation symbol to convert
     * @return {@link BallerinaAnnotationSymbol}
     */
    public static BallerinaAnnotationSymbol createAnnotationSymbol(BAnnotationSymbol symbol) {
        return new BallerinaAnnotationSymbol.AnnotationSymbolBuilder(symbol.name.getValue(), symbol.pkgID, symbol)
                .build();
    }
}
