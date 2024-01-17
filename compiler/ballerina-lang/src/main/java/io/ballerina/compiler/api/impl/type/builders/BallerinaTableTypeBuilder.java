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
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The implementation of the methods used to build the Table type descriptor in Types API.
 *
 * @since 2201.2.0
 */
public class BallerinaTableTypeBuilder implements TypeBuilder.TABLE {

    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private final Types types;
    private TypeSymbol rowType;
    private final List<TypeSymbol> keyTypes = new ArrayList<>();
    private final List<String> fieldNames = new ArrayList<>();

    public BallerinaTableTypeBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
        types = Types.getInstance(context);
    }

    @Override
    public TypeBuilder.TABLE withRowType(TypeSymbol rowType) {
        if (rowType.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            return this.withRowType(((TypeReferenceTypeSymbol) rowType).typeDescriptor());
        }

        this.rowType = rowType;
        return this;
    }

    @Override
    public TypeBuilder.TABLE withKeyConstraints(TypeSymbol... keyTypes) {
        this.keyTypes.clear();
        this.keyTypes.addAll(Arrays.asList(keyTypes));
        return this;
    }

    @Override
    public TypeBuilder.TABLE withKeySpecifiers(String... fieldNames) {
        this.fieldNames.clear();
        this.fieldNames.addAll(Arrays.asList(fieldNames));

        return this;
    }

    @Override
    public TableTypeSymbol build() {
        BType rowBType = getRowBType(rowType,
                (keyTypes.isEmpty() && fieldNames.isEmpty())
                        || (keyTypes.stream().allMatch(typeSymbol -> typeSymbol.typeKind() == TypeDescKind.NEVER)));
        BTypeSymbol tableSymbol = Symbols.createTypeSymbol(SymTag.TYPE, Flags.PUBLIC, Names.EMPTY,
                symTable.rootPkgSymbol.pkgID, null, symTable.rootPkgSymbol, symTable.builtinPos,
                symTable.rootPkgSymbol.origin);

        BTableType tableType = new BTableType(TypeTags.TABLE, rowBType, tableSymbol);
        tableSymbol.type = tableType;
        if (!keyTypes.isEmpty()) {
            tableType.keyTypeConstraint = getKeyConstraintBType(keyTypes, rowType);
        } else if (!fieldNames.isEmpty() && isValidFieldNameList(fieldNames, rowType)) {
            tableType.fieldNameList = new ArrayList<>(fieldNames);
        }

        TableTypeSymbol tableTypeSymbol = (TableTypeSymbol) typesFactory.getTypeDescriptor(tableType);
        rowType = null;
        keyTypes.clear();
        fieldNames.clear();

        return tableTypeSymbol;
    }

    private boolean isValidFieldNameList(List<String> fieldNames, TypeSymbol rowType) {
        // check if a key field name exist in the rowType's record fields as a readonly field.
        if (rowType instanceof RecordTypeSymbol) {
            Map<String, RecordFieldSymbol> fieldDescriptors = ((RecordTypeSymbol) rowType).fieldDescriptors();
            for (String fieldName : fieldNames) {
                if (!fieldDescriptors.containsKey(fieldName)) {
                    return false;
                }

                RecordFieldSymbol recordField = fieldDescriptors.get(fieldName);
                if (recordField.isOptional() || !isReadOnlyField(recordField)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private boolean isReadOnlyField(RecordFieldSymbol recordField) {
        TypeSymbol typeDescriptor = recordField.typeDescriptor();
        if (typeDescriptor instanceof AbstractTypeSymbol) {
            BType bType = ((AbstractTypeSymbol) typeDescriptor).getBType();

            return Symbols.isFlagOn(bType.flags, Flags.READONLY);
        }

        return false;
    }

    private BType getKeyConstraintBType(List<TypeSymbol> keyTypes, TypeSymbol rowType) {
        if (keyTypes.size() == 1) {
            return checkKeyConstraintBType(keyTypes.get(0), rowType);
        }

        List<BTupleMember> tupleMembers = new ArrayList<>();
        for (TypeSymbol keyType : keyTypes) {
            BType constraintType = checkKeyConstraintBType(keyType, rowType);
            BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(constraintType);
            tupleMembers.add(new BTupleMember(constraintType, varSymbol));
        }

        return new BTupleType(tupleMembers);
    }

    private BType checkKeyConstraintBType(TypeSymbol keyType, TypeSymbol rowType) {
        // if map type, check if field names equivalent to key type is readonly & required.
        // if record type check same with field descriptors.
        BType keyBType = getKeyBType(keyType);
        if (rowType.typeKind() == TypeDescKind.MAP) {
            TypeSymbol typeParam = ((MapTypeSymbol) rowType).typeParam();
            if (typeParam.subtypeOf(keyType)) {
                return keyBType;
            }
        } else if (rowType.typeKind() == TypeDescKind.RECORD) {
            Map<String, RecordFieldSymbol> recordFields = ((RecordTypeSymbol) rowType).fieldDescriptors();
            for (RecordFieldSymbol recordFieldSymbol : recordFields.values()) {
                if (!recordFieldSymbol.isOptional() && isValidKeyConstraintType(recordFieldSymbol.typeDescriptor(),
                        keyType)) {
                    return keyBType;
                }
            }
        }

        throw new IllegalArgumentException("Invalid key type provided");
    }

    private boolean isValidKeyConstraintType(TypeSymbol fieldType, TypeSymbol keyType) {
        if ((fieldType.typeKind() == keyType.typeKind() || keyType.subtypeOf(fieldType))
                && fieldType instanceof AbstractTypeSymbol) {
            return Symbols.isFlagOn(((AbstractTypeSymbol) fieldType).getBType().flags, Flags.READONLY);
        }

        return false;
    }

    private BType getKeyBType(TypeSymbol typeSymbol) {
        if (typeSymbol instanceof AbstractTypeSymbol) {
            return ((AbstractTypeSymbol) typeSymbol).getBType();
        }

        throw new IllegalArgumentException("Invalid key type parameter provided");
    }

    private BType getRowBType(TypeSymbol rowType, boolean isKeyless) {
        if (rowType == null) {
            throw new IllegalArgumentException("Row type parameter can not be null");
        }

        BType rowBType = getBType(rowType);
        if (types.isAssignable(rowBType, symTable.mapType)) {
            if (rowBType instanceof BRecordType) {
                if (isKeyless || isValidRowRecordType((BRecordType) rowBType)) {
                    return rowBType;
                }
            } else if (rowBType instanceof BMapType) {
                BMapType rowMapType = (BMapType) rowBType;
                BType mapTypeConstraint = rowMapType.getConstraint();
                if (types.isAssignable(mapTypeConstraint, symTable.pureType)) {
                    return rowBType;
                }
            }
        }

        throw new IllegalArgumentException("Invalid row type parameter provided");
    }

    private boolean isValidRowRecordType(BRecordType rowBType) {
        for (BField field : rowBType.getFields().values()) {
            if (types.isAssignable(field.type, symTable.anydataType)
                    && field.symbol != null
                    && Symbols.isFlagOn(field.symbol.flags, Flags.READONLY)
                    && !Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL)) {
                return true;
            }
        }

        return false;
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
