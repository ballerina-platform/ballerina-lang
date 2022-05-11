/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.compiler.api.impl.types;

import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

public class BallerinaTableTypeBuilder implements TypeBuilder.TABLE {

    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private final Types types;
    private TypeSymbol rowType;
    private TypeSymbol keyType;
    private List<String> fieldNames;

    public BallerinaTableTypeBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
        types = Types.getInstance(context);
    }

    @Override
    public TypeBuilder.TABLE withRowType(TypeSymbol rowType) {
        this.rowType = rowType;
        return this;
    }

    @Override
    public TypeBuilder.TABLE withKeyConstraint(TypeSymbol keyType) {
        this.keyType = keyType;
        return this;
    }

    @Override
    public TypeBuilder.TABLE withKeyConstraint(String... fieldNames) {
        for (String fieldName : fieldNames) {
            this.fieldNames.add(fieldName);
        }
        return this;
    }

    @Override
    public TableTypeSymbol build() {
        BType rowBType = getRowBType(rowType);
        BTableType tableType = new BTableType(TypeTags.TABLE, rowBType, symTable.tableType.tsymbol);
        if (keyType != null) {
            tableType.keyTypeConstraint = getBType(keyType);
        } else if (!fieldNames.isEmpty()) {
            tableType.fieldNameList = fieldNames;
        }
        return (TableTypeSymbol) typesFactory.getTypeDescriptor(tableType);
    }

    private BType getRowBType(TypeSymbol rowType) {

        BType rowBType = getBType(rowType);
        if (types.isAssignable(rowBType, symTable.mapType)) {
            BMapType rowMapType = (BMapType) rowBType;
            if (types.isAssignable(rowMapType.getConstraint(), symTable.anyType) || types.isAssignable(rowMapType,
                    symTable.errorType)) {
                return rowBType;
            }
        }

        throw new IllegalArgumentException("Invalid row type parameter provided");
    }

    private BType getBType(TypeSymbol typeSymbol) {
        if (typeSymbol != null) {
            if (typeSymbol instanceof AbstractTypeSymbol
                    && typeSymbol.subtypeOf(typesFactory.getTypeDescriptor(symTable.anyType))) {
                return ((AbstractTypeSymbol) typeSymbol).getBType();
            }

            throw new IllegalArgumentException("Invalid type parameter provided");
        }

        return null;
    }
}
