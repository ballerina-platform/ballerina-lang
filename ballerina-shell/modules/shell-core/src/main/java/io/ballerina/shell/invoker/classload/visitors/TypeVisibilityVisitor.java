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
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Visits all the nodes and informs whether the type needs
 * elevating due to some types not being visible to the REPL.
 */
public class TypeVisibilityVisitor extends TypeSymbolVisitor {
    private final List<TypeSymbol> invisibleTypes;

    public TypeVisibilityVisitor() {
        this.invisibleTypes = new ArrayList<>();
    }

    public List<TypeSymbol> getInvisibleTypes() {
        return invisibleTypes;
    }

    public boolean isVisible() {
        return this.invisibleTypes.isEmpty();
    }

    @Override
    public void visitType(TypeSymbol symbol) {
        super.visitType(symbol);
    }

    @Override
    protected void visit(ArrayTypeSymbol symbol) {
        visitType(symbol.memberTypeDescriptor());
    }

    @Override
    protected void visit(FunctionTypeSymbol symbol) {
        symbol.returnTypeDescriptor().ifPresent(this::visitType);
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
    protected void visit(ObjectTypeSymbol symbol) {
        symbol.typeInclusions().forEach(this::visitType);
    }

    @Override
    protected void visit(RecordTypeSymbol symbol) {
        symbol.restTypeDescriptor().ifPresent(this::visitType);
    }

    @Override
    protected void visit(StreamTypeSymbol symbol) {
        visitType(symbol.typeParameter());
        symbol.completionValueTypeParameter().ifPresent(this::visitType);
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
    protected void visit(TypeDescTypeSymbol symbol) {
        symbol.typeParameter().ifPresent(this::visitType);
    }

    @Override
    protected void visit(UnionTypeSymbol symbol) {
        symbol.memberTypeDescriptors().forEach(this::visitType);
    }

    @Override
    protected void visit(XMLTypeSymbol symbol) {
        symbol.typeParameter().ifPresent(this::visitType);
    }

    @Override
    protected void visit(ErrorTypeSymbol symbol) {
        setVisibility(symbol, true);
    }

    @Override
    protected void visit(TypeReferenceTypeSymbol symbol) {
        setVisibility(symbol, true);
        visit((TypeSymbol) symbol);
    }

    @Override
    protected void visit(TypeSymbol symbol) {
    }

    /**
     * Sets the visibility depending on the type given.
     * This will be called on all the visited nodes,
     * This would elevate the type if a visited type is not visible.
     * If all of the visited nodes are error types, elevates to 'error'.
     * If some of the visited nodes are error types, elevates to 'any|error'.
     * If none of the visited nodes are error types, elevates to 'any'.
     * TODO: Implement isVisible finding via type.
     *
     * @param typeSymbol Typ[e to visit.
     */
    protected void setVisibility(TypeSymbol typeSymbol, boolean isVisible) {
        if (typeSymbol.moduleID().orgName().equals("$anon")
                || (typeSymbol.moduleID().moduleName().equals("lang.annotations")
                && typeSymbol.moduleID().orgName().equals("ballerina"))
                || isVisible) {
            return;
        }
        this.invisibleTypes.add(typeSymbol);
    }
}
