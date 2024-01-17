/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api;

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.AnyTypeSymbol;
import io.ballerina.compiler.api.symbols.AnydataTypeSymbol;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.BooleanTypeSymbol;
import io.ballerina.compiler.api.symbols.ByteTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.CompilationErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.DecimalTypeSymbol;
import io.ballerina.compiler.api.symbols.EnumSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.FloatTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.HandleTypeSymbol;
import io.ballerina.compiler.api.symbols.IntSigned16TypeSymbol;
import io.ballerina.compiler.api.symbols.IntSigned32TypeSymbol;
import io.ballerina.compiler.api.symbols.IntSigned8TypeSymbol;
import io.ballerina.compiler.api.symbols.IntTypeSymbol;
import io.ballerina.compiler.api.symbols.IntUnsigned16TypeSymbol;
import io.ballerina.compiler.api.symbols.IntUnsigned32TypeSymbol;
import io.ballerina.compiler.api.symbols.IntUnsigned8TypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.JSONTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.NeverTypeSymbol;
import io.ballerina.compiler.api.symbols.NilTypeSymbol;
import io.ballerina.compiler.api.symbols.NoneTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.ReadonlyTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.SingletonTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.StringCharTypeSymbol;
import io.ballerina.compiler.api.symbols.StringTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.symbols.WorkerSymbol;
import io.ballerina.compiler.api.symbols.XMLCommentTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLElementTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLNamespaceSymbol;
import io.ballerina.compiler.api.symbols.XMLProcessingInstructionTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTextTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;


/**
 * A transformer for visiting a symbol and mapping the symbol to some other representation.
 *
 * @param <R> The type to which the symbol will be mapped
 */
public abstract class SymbolTransformer<R> {

    /**
     * An end user using a visitor should always be calling this method to transform a symbol instead of calling the
     * individual transform() methods. One implementing a transformer should implement this method and can use this to
     * include any common pre-processing and post-processing tasks to be done during the course of transforming the
     * symbol.
     *
     * @param symbol Symbol to be traversed
     * @return The transformed instance for the symbol
     */
    public abstract R transformSymbol(Symbol symbol);

    // Symbols
    public R transform(AnnotationSymbol annotation) {
        return transformSymbol(annotation);
    }

    public R transform(ClassSymbol clazz) {
        return transformSymbol(clazz);
    }

    public R transform(ConstantSymbol constant) {
        return transformSymbol(constant);
    }

    public R transform(EnumSymbol enumSymbol) {
        return transformSymbol(enumSymbol);
    }

    public R transform(FunctionSymbol function) {
        return transformSymbol(function);
    }

    public R transform(ModuleSymbol module) {
        return transformSymbol(module);
    }

    public R transform(ServiceDeclarationSymbol service) {
        return transformSymbol(service);
    }

    public R transform(TypeDefinitionSymbol typeDef) {
        return transformSymbol(typeDef);
    }

    public R transform(VariableSymbol var) {
        return transformSymbol(var);
    }

    public R transform(WorkerSymbol worker) {
        return transformSymbol(worker);
    }

    public R transform(XMLNamespaceSymbol xmlns) {
        return transformSymbol(xmlns);
    }

    // Util symbols (i.e., used within above symbols or type symbols)

    public R transform(ClassFieldSymbol classField) {
        return transformSymbol(classField);
    }

    public R transform(MethodSymbol method) {
        return transformSymbol(method);
    }

    public R transform(ObjectFieldSymbol objectField) {
        return transformSymbol(objectField);
    }

    public R transform(ParameterSymbol param) {
        return transformSymbol(param);
    }

    public R transform(PathParameterSymbol pathParam) {
        return transformSymbol(pathParam);
    }

    public R transform(RecordFieldSymbol recordField) {
        return transformSymbol(recordField);
    }

    public R transform(ResourceMethodSymbol resourceMethod) {
        return transformSymbol(resourceMethod);
    }

    // Types

    public R transform(AnyTypeSymbol anyType) {
        return transformSymbol(anyType);
    }

    public R transform(AnydataTypeSymbol anydataType) {
        return transformSymbol(anydataType);
    }

    public R transform(ArrayTypeSymbol arrayType) {
        return transformSymbol(arrayType);
    }

    public R transform(BooleanTypeSymbol booleanType) {
        return transformSymbol(booleanType);
    }

    public R transform(ByteTypeSymbol byteType) {
        return transformSymbol(byteType);
    }

    public R transform(CompilationErrorTypeSymbol compileErrorType) {
        return transformSymbol(compileErrorType);
    }

    public R transform(DecimalTypeSymbol decimalType) {
        return transformSymbol(decimalType);
    }

    public R transform(ErrorTypeSymbol errorType) {
        return transformSymbol(errorType);
    }

    public R transform(FloatTypeSymbol floatType) {
        return transformSymbol(floatType);
    }

    public R transform(FunctionTypeSymbol functionType) {
        return transformSymbol(functionType);
    }

    public R transform(FutureTypeSymbol futureType) {
        return transformSymbol(futureType);
    }

    public R transform(HandleTypeSymbol handleType) {
        return transformSymbol(handleType);
    }

    public R transform(IntTypeSymbol intType) {
        return transformSymbol(intType);
    }

    public R transform(IntSigned8TypeSymbol signedInt8Type) {
        return transformSymbol(signedInt8Type);
    }

    public R transform(IntSigned16TypeSymbol signedInt16Type) {
        return transformSymbol(signedInt16Type);
    }

    public R transform(IntSigned32TypeSymbol signedInt32Type) {
        return transformSymbol(signedInt32Type);
    }

    public R transform(IntUnsigned8TypeSymbol unsignedInt8Type) {
        return transformSymbol(unsignedInt8Type);
    }

    public R transform(IntUnsigned16TypeSymbol unsignedInt16Type) {
        return transformSymbol(unsignedInt16Type);
    }

    public R transform(IntUnsigned32TypeSymbol unsignedInt32Type) {
        return transformSymbol(unsignedInt32Type);
    }

    public R transform(IntersectionTypeSymbol intersectionType) {
        return transformSymbol(intersectionType);
    }

    public R transform(JSONTypeSymbol jsonType) {
        return transformSymbol(jsonType);
    }

    public R transform(MapTypeSymbol mapType) {
        return transformSymbol(mapType);
    }

    public R transform(NeverTypeSymbol neverType) {
        return transformSymbol(neverType);
    }

    public R transform(NilTypeSymbol nilType) {
        return transformSymbol(nilType);
    }

    public R transform(NoneTypeSymbol noneType) {
        return transformSymbol(noneType);
    }

    public R transform(ObjectTypeSymbol objectType) {
        return transformSymbol(objectType);
    }

    public R transform(ReadonlyTypeSymbol readonlyType) {
        return transformSymbol(readonlyType);
    }

    public R transform(RecordTypeSymbol recordType) {
        return transformSymbol(recordType);
    }

    public R transform(SingletonTypeSymbol singletonType) {
        return transformSymbol(singletonType);
    }

    public R transform(StreamTypeSymbol streamType) {
        return transformSymbol(streamType);
    }

    public R transform(StringTypeSymbol stringType) {
        return transformSymbol(stringType);
    }

    public R transform(StringCharTypeSymbol charType) {
        return transformSymbol(charType);
    }

    public R transform(TableTypeSymbol tableType) {
        return transformSymbol(tableType);
    }

    public R transform(TupleTypeSymbol tupleType) {
        return transformSymbol(tupleType);
    }

    public R transform(TypeDescTypeSymbol typedescType) {
        return transformSymbol(typedescType);
    }

    public R transform(TypeReferenceTypeSymbol typeRefType) {
        return transformSymbol(typeRefType);
    }

    public R transform(UnionTypeSymbol unionType) {
        return transformSymbol(unionType);
    }

    public R transform(XMLTypeSymbol xmlType) {
        return transformSymbol(xmlType);
    }

    public R transform(XMLCommentTypeSymbol xmlCommentType) {
        return transformSymbol(xmlCommentType);
    }

    public R transform(XMLElementTypeSymbol xmlElementType) {
        return transformSymbol(xmlElementType);
    }

    public R transform(XMLProcessingInstructionTypeSymbol xmlPIType) {
        return transformSymbol(xmlPIType);
    }

    public R transform(XMLTextTypeSymbol xmlTextType) {
        return transformSymbol(xmlTextType);
    }

    public R transform(Symbol symbol) {
        throw new UnsupportedOperationException();
    }

    public R transform(TypeSymbol symbol) {
        throw new UnsupportedOperationException();
    }
}
