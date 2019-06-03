/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.writer;

import io.netty.buffer.ByteBuf;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.bir.model.Visibility;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.ByteCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.FloatCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.IntegerCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.PackageCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntermediateCollectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Writes bType to a Byte Buffer in binary format.
 * A ConstPool is used to store string typed information.
 * 
 * @since 0.995.0
 */
public class BIRTypeWriter implements TypeVisitor {
    private final ByteBuf buff;

    private final ConstantPool cp;

    public BIRTypeWriter(ByteBuf buff, ConstantPool cp) {
        this.buff = buff;
        this.cp = cp;
    }

    public void visitType(BType type) {
        buff.writeByte(type.tag);
        type.accept(this);
    }

    private void writeTypeCpIndex(BType type) {
        buff.writeInt(cp.addShapeCPEntry(type));
    }

    @Override
    public void visit(BAnnotationType bAnnotationType) {
        throwUnimplementedError(bAnnotationType);
    }

    @Override
    public void visit(BArrayType bArrayType) {
        buff.writeByte(bArrayType.state.getValue());
        buff.writeInt(bArrayType.size);
        writeTypeCpIndex(bArrayType.getElementType());
    }

    @Override
    public void visit(BBuiltInRefType bBuiltInRefType) {
        throwUnimplementedError(bBuiltInRefType);
    }

    @Override
    public void visit(BAnyType bAnyType) {
    }

    @Override
    public void visit(BErrorType bErrorType) {
        writeTypeCpIndex(bErrorType.reasonType);
        writeTypeCpIndex(bErrorType.detailType);
    }

    @Override
    public void visit(BFiniteType bFiniteType) {
        BTypeSymbol tsymbol = bFiniteType.tsymbol;
        buff.writeInt(addStringCPEntry(tsymbol.name.value));
        buff.writeByte(getVisibility(tsymbol).value());
        buff.writeInt(bFiniteType.valueSpace.size());
        for (BLangExpression valueLiteral : bFiniteType.valueSpace) {
            if (!(valueLiteral instanceof BLangLiteral)) {
                throw new AssertionError(
                        "Type serialization is not implemented for finite type with value: " + valueLiteral.getKind());
            }
            writeTypeCpIndex(valueLiteral.type);
            writeValue(((BLangLiteral) valueLiteral).value, valueLiteral.type);
        }
    }

    @Override
    public void visit(BIntermediateCollectionType bIntermediateCollectionType) {
        throwUnimplementedError(bIntermediateCollectionType);
    }

    @Override
    public void visit(BInvokableType bInvokableType) {
        buff.writeInt(bInvokableType.paramTypes.size());
        for (BType params : bInvokableType.paramTypes) {
            writeTypeCpIndex(params);
        }
        writeTypeCpIndex(bInvokableType.retType);
    }

    @Override
    public void visit(BJSONType bjsonType) {
        // Nothing to do
    }

    @Override
    public void visit(BMapType bMapType) {
        writeTypeCpIndex(bMapType.constraint);
    }

    @Override
    public void visit(BTableType bTableType) {
        writeTypeCpIndex(bTableType.constraint);
    }

    @Override
    public void visit(BStreamType bStreamType) {
        writeTypeCpIndex(bStreamType.constraint);
    }

    @Override
    public void visit(BFutureType bFutureType) {
        writeTypeCpIndex(bFutureType.constraint);
    }

    @Override
    public void visit(BNilType bNilType) {
        // Nothing to do
    }

    @Override
    public void visit(BNoType bNoType) {
        // Nothing to do
    }

    @Override
    public void visit(BAnydataType bAnydataType) {
        // Nothing to do
    }

    @Override
    public void visit(BPackageType bPackageType) {
        throwUnimplementedError(bPackageType);
    }

    @Override
    public void visit(BServiceType bServiceType) {
        //This is to say this is an object, this is a temporary fix object - 1, service - 0,
        // ideal fix would be to use the type tag to
        // differentiate. TODO fix later
        buff.writeByte(1);

        writeObjectAndServiceTypes(bServiceType);
    }

    @Override
    public void visit(BStructureType bStructureType) {
        throwUnimplementedError(bStructureType);
    }

    @Override
    public void visit(BTupleType bTupleType) {
        buff.writeInt(bTupleType.tupleTypes.size());
        for (BType memberType : bTupleType.tupleTypes) {
            writeTypeCpIndex(memberType);
        }
    }

    @Override
    public void visit(BUnionType bUnionType) {
        buff.writeInt(bUnionType.getMemberTypes().size());
        for (BType memberType : bUnionType.getMemberTypes()) {
            writeTypeCpIndex(memberType);
        }
    }

    @Override
    public void visit(BRecordType bRecordType) {
        BRecordTypeSymbol tsymbol = (BRecordTypeSymbol) bRecordType.tsymbol;

        // Write the package details in the form of constant pool entry TODO find a better approach
        int orgCPIndex = addStringCPEntry(tsymbol.pkgID.orgName.value);
        int nameCPIndex = addStringCPEntry(tsymbol.pkgID.name.value);
        int versionCPIndex = addStringCPEntry(tsymbol.pkgID.version.value);
        int pkgIndex = cp.addCPEntry(new PackageCPEntry(orgCPIndex, nameCPIndex, versionCPIndex));
        buff.writeInt(pkgIndex);

        buff.writeInt(addStringCPEntry(tsymbol.name.value));
        buff.writeBoolean(bRecordType.sealed);
        writeTypeCpIndex(bRecordType.restFieldType);

        BAttachedFunction initializerFunc = tsymbol.initializerFunc;
        Set<Map.Entry<Name, Scope.ScopeEntry>> recordSymbols = tsymbol.scope.entries.entrySet();

        buff.writeInt(recordSymbols.size() - 1); // recordSymbols = 1 initializer + n fields
        for (Map.Entry<Name, Scope.ScopeEntry> entry : recordSymbols) {
            BSymbol symbol = entry.getValue().symbol;
            String fieldName = entry.getKey().value;
            if (symbol != initializerFunc.symbol) {
                buff.writeInt(addStringCPEntry(fieldName));
                buff.writeByte(getVisibility(symbol).value());
                writeTypeCpIndex(symbol.type);
            }
        }

        buff.writeInt(addStringCPEntry(initializerFunc.funcName.value));
        buff.writeByte(getVisibility(initializerFunc.symbol).value());
        writeTypeCpIndex(initializerFunc.type);
    }

    @Override
    public void visit(BObjectType bObjectType) {
        //This is to say this is an object, this is a temporary fix object - 1, service - 0,
        // ideal fix would be to use the type tag to
        // differentiate. TODO fix later
        buff.writeByte(0);

        writeObjectAndServiceTypes(bObjectType);
    }

    private void writeObjectAndServiceTypes(BObjectType bObjectType) {
        BTypeSymbol tSymbol = bObjectType.tsymbol;

        // Write the package details in the form of constant pool entry TODO find a better approach
        int orgCPIndex = addStringCPEntry(tSymbol.pkgID.orgName.value);
        int nameCPIndex = addStringCPEntry(tSymbol.pkgID.name.value);
        int versionCPIndex = addStringCPEntry(tSymbol.pkgID.version.value);
        int pkgIndex = cp.addCPEntry(new PackageCPEntry(orgCPIndex, nameCPIndex, versionCPIndex));
        buff.writeInt(pkgIndex);

        buff.writeInt(addStringCPEntry(tSymbol.name.value));
        //TODO below two line are a temp solution, introduce a generic concept
        buff.writeBoolean(Symbols.isFlagOn(tSymbol.flags, Flags.ABSTRACT)); // Abstract object or not
        buff.writeBoolean(Symbols.isFlagOn(tSymbol.flags, Flags.CLIENT));
        buff.writeInt(bObjectType.fields.size());
        for (BField field : bObjectType.fields) {
            buff.writeInt(addStringCPEntry(field.name.value));
            // TODO add position
            buff.writeByte(getVisibility(field.symbol).value());
            writeTypeCpIndex(field.type);
        }
        List<BAttachedFunction> attachedFuncs;
        //TODO cleanup, there cannot be objects without attached function list and symbol kind other than object
        if (tSymbol.kind == SymbolKind.OBJECT) {
            Map<Boolean, List<BAttachedFunction>> partitions = ((BObjectTypeSymbol) tSymbol).attachedFuncs.stream()
                    .collect(Collectors.partitioningBy(n -> n.funcName.equals(Names.OBJECT_INIT_SUFFIX)));
            attachedFuncs = partitions.get(false);
            List<BAttachedFunction> constructor = partitions.get(true);
            if (constructor.size() != 0) {
                buff.writeByte(1); // constructor present
                writeAttachFunction(partitions.get(true).get(0));
            } else {
                buff.writeByte(0); // constructor not present
            }
        } else {
            attachedFuncs = new ArrayList<>();
            buff.writeByte(0); // constructor not present
        }
        buff.writeInt(attachedFuncs.size());
        for (BAttachedFunction attachedFunc : attachedFuncs) {
            writeAttachFunction(attachedFunc);
        }
    }

    private void writeAttachFunction(BAttachedFunction attachedFunc) {
        buff.writeInt(addStringCPEntry(attachedFunc.funcName.value));
        buff.writeByte(getVisibility(attachedFunc.symbol).value());
        writeTypeCpIndex(attachedFunc.type);
    }

    @Override
    public void visit(BType bType) {
        // Nothing to do
    }

    @Override
    public void visit(BXMLType bxmlType) {
        // Nothing to do
    }

    private void throwUnimplementedError(BType bType) {
        throw new AssertionError("Type serialization is not implemented for " + bType.getClass());
    }

    private int addStringCPEntry(String value) {
        return cp.addCPEntry(new StringCPEntry(value));
    }

    private int addIntCPEntry(long value) {
        return cp.addCPEntry(new IntegerCPEntry(value));
    }

    private int addFloatCPEntry(double value) {
        return cp.addCPEntry(new FloatCPEntry(value));
    }

    private int addByteCPEntry(int value) {
        return cp.addCPEntry(new ByteCPEntry(value));
    }

    private Visibility getVisibility(BSymbol symbol) {
        if (Symbols.isPublic(symbol)) {
            return Visibility.PUBLIC;
        } else if (Symbols.isPrivate(symbol)) {
            return Visibility.PRIVATE;
        } else {
            return Visibility.PACKAGE_PRIVATE;
        }
    }

    private void writeValue(Object value, BType typeOfValue) {
        switch (typeOfValue.tag) {
            case TypeTags.INT:
                buff.writeInt(addIntCPEntry((Long) value));
                break;
            case TypeTags.BYTE:
                int byteValue = ((Number) value).intValue();
                buff.writeInt(addByteCPEntry(byteValue));
                break;
            case TypeTags.FLOAT:
                // TODO:Remove the instanceof check by converting the float literal instance in Semantic analysis phase
                double doubleVal = value instanceof String ? Double.parseDouble((String) value) : (Double) value;
                buff.writeInt(addFloatCPEntry(doubleVal));
                break;
            case TypeTags.STRING:
            case TypeTags.DECIMAL:
                buff.writeInt(addStringCPEntry((String) value));
                break;
            case TypeTags.BOOLEAN:
                buff.writeByte((Boolean) value ? 1 : 0);
                break;
            case TypeTags.NIL:
                break;
            default:
                throw new UnsupportedOperationException("finite type value is not supported for type: " + typeOfValue);
        }
    }
}
