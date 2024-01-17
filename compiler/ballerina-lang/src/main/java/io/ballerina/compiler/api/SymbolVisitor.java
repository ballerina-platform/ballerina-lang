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
 * A visitor for recursively visiting a symbol.
 */
public abstract class SymbolVisitor {

    /**
     * An end user using a visitor should always be calling this method to visit a symbol instead of calling the
     * individual visit() methods. One implementing a visitor should implement this method and can use this to include
     * any common pre-processing and post-processing tasks to be done during the course of visiting the symbol.
     *
     * @param symbol symbol to be traversed
     */
    public abstract void visitSymbol(Symbol symbol);

    // Symbols
    public void visit(AnnotationSymbol annotation) {
    }

    public void visit(ClassSymbol clazz) {
    }

    public void visit(ConstantSymbol constant) {
    }

    public void visit(EnumSymbol enumSymbol) {
    }

    public void visit(FunctionSymbol function) {
    }

    public void visit(ModuleSymbol module) {
    }

    public void visit(ServiceDeclarationSymbol service) {
    }

    public void visit(TypeDefinitionSymbol typeDef) {
    }

    public void visit(VariableSymbol var) {
    }

    public void visit(WorkerSymbol worker) {
    }

    public void visit(XMLNamespaceSymbol xmlns) {
    }

    // Util symbols (i.e., used within above symbols or type symbols)

    public void visit(ClassFieldSymbol classField) {
    }

    public void visit(MethodSymbol method) {
    }

    public void visit(ObjectFieldSymbol objectField) {
    }

    public void visit(ParameterSymbol param) {
    }

    public void visit(PathParameterSymbol pathParam) {
    }

    public void visit(RecordFieldSymbol recordField) {
    }

    public void visit(ResourceMethodSymbol resourceMethod) {
    }

    // Types

    public void visit(AnyTypeSymbol anyType) {
    }

    public void visit(AnydataTypeSymbol anydataType) {
    }

    public void visit(ArrayTypeSymbol arrayType) {
    }

    public void visit(BooleanTypeSymbol booleanType) {
    }

    public void visit(ByteTypeSymbol byteType) {
    }

    public void visit(CompilationErrorTypeSymbol compileErrorType) {
    }

    public void visit(DecimalTypeSymbol decimalType) {
    }

    public void visit(ErrorTypeSymbol errorType) {
    }

    public void visit(FloatTypeSymbol floatType) {
    }

    public void visit(FunctionTypeSymbol functionType) {
    }

    public void visit(FutureTypeSymbol futureType) {
    }

    public void visit(HandleTypeSymbol handleType) {
    }

    public void visit(IntTypeSymbol intType) {
    }

    public void visit(IntSigned8TypeSymbol signedInt8Type) {
    }

    public void visit(IntSigned16TypeSymbol signedInt16Type) {
    }

    public void visit(IntSigned32TypeSymbol signedInt32Type) {
    }

    public void visit(IntUnsigned8TypeSymbol unsignedInt8Type) {
    }

    public void visit(IntUnsigned16TypeSymbol unsignedInt16Type) {
    }

    public void visit(IntUnsigned32TypeSymbol unsignedInt32Type) {
    }

    public void visit(IntersectionTypeSymbol intersectionType) {
    }

    public void visit(JSONTypeSymbol jsonType) {
    }

    public void visit(MapTypeSymbol mapType) {
    }

    public void visit(NeverTypeSymbol neverType) {
    }

    public void visit(NilTypeSymbol nilType) {
    }

    public void visit(NoneTypeSymbol noneType) {
    }

    public void visit(ObjectTypeSymbol objectType) {
    }

    public void visit(ReadonlyTypeSymbol readonlyType) {
    }

    public void visit(RecordTypeSymbol recordType) {
    }

    public void visit(SingletonTypeSymbol singletonType) {
    }

    public void visit(StreamTypeSymbol streamType) {
    }

    public void visit(StringTypeSymbol stringType) {
    }

    public void visit(StringCharTypeSymbol charType) {
    }

    public void visit(TableTypeSymbol tableType) {
    }

    public void visit(TupleTypeSymbol tupleType) {
    }

    public void visit(TypeDescTypeSymbol typedescType) {
    }

    public void visit(TypeReferenceTypeSymbol typeRefType) {
    }

    public void visit(UnionTypeSymbol unionType) {
    }

    public void visit(XMLTypeSymbol xmlType) {
    }

    public void visit(XMLCommentTypeSymbol xmlCommentType) {
    }

    public void visit(XMLElementTypeSymbol xmlElementType) {
    }

    public void visit(XMLProcessingInstructionTypeSymbol xmlPIType) {
    }

    public void visit(XMLTextTypeSymbol xmlTextType) {
    }

    public void visit(Symbol symbol) {
        throw new UnsupportedOperationException();
    }

    public void visit(TypeSymbol symbol) {
        throw new UnsupportedOperationException();
    }
}
