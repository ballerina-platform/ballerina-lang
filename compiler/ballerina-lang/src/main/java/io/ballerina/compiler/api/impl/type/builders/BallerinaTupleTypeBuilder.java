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

package io.ballerina.compiler.api.impl.type.builders;

import io.ballerina.compiler.api.TypeBuilder;
import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the methods used to build the Tuple type descriptor in Types API.
 *
 * @since 2201.2.0
 */
public class BallerinaTupleTypeBuilder implements TypeBuilder.TUPLE {

    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private final List<TypeSymbol> memberTypes = new ArrayList<>();
    private TypeSymbol restType;

    public BallerinaTupleTypeBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
    }

    @Override
    public TypeBuilder.TUPLE withMemberType(TypeSymbol memberType) {
        memberTypes.add(memberType);
        return this;
    }

    @Override
    public TypeBuilder.TUPLE withRestType(TypeSymbol restType) {
        this.restType = restType;
        return this;
    }

    @Override
    public TupleTypeSymbol build() {
        List<BTupleMember> memberTypes = new ArrayList<>();
        for (TypeSymbol memberType : this.memberTypes) {
            BType type = getMemberType(memberType);
            BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(type);
            memberTypes.add(new BTupleMember(type, varSymbol));
        }

        BTypeSymbol tupleSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.PUBLIC, Names.EMPTY,
                symTable.rootPkgSymbol.pkgID, null,
                symTable.rootPkgSymbol, symTable.builtinPos, SymbolOrigin.COMPILED_SOURCE);

        BTupleType tupleType = new BTupleType(tupleSymbol, memberTypes);
        tupleSymbol.type = tupleType;
        BType restBType = getRestType(restType);
        if (restBType != null) {
            tupleType.restType = restBType;
        }

        TupleTypeSymbol tupleTypeSymbol = (TupleTypeSymbol) typesFactory.getTypeDescriptor(tupleType);
        this.memberTypes.clear();
        this.restType = null;

        return tupleTypeSymbol;
    }

    private BType getMemberType(TypeSymbol memberType) {
        if (memberType == null) {
            throw new IllegalArgumentException("Member type provided to the Tuple type descriptor can not be null.");
        }

        if (memberType instanceof AbstractTypeSymbol) {
            return ((AbstractTypeSymbol) memberType).getBType();
        }
        return symTable.noType;
    }

    private BType getRestType(TypeSymbol restType) {
        return restType instanceof AbstractTypeSymbol ? ((AbstractTypeSymbol) restType).getBType() : null;
    }
}
