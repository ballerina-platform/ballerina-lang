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
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
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
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Writes bType to a Byte Buffer in binary format.
 * A ConstPool is used to store string typed information.
 * 
 * @since 0.995.0
 */
public class BIRTypeWriter implements TypeVisitor {
    public static final int TYPE_TAG_SELF = 50;
    private final ByteBuf buff;

    private final ConstantPool cp;
    private LinkedList<Object> compositeStack = new LinkedList<>();

    public BIRTypeWriter(ByteBuf buff, ConstantPool cp) {
        this.buff = buff;
        this.cp = cp;
    }

    public void visitType(BType type) {
        if (writeSelfRef(type.tsymbol)) {
            return;
        }
        //TODO improve below check, basically try to remove, doing this because symbol is null for them
        if (type.tag == TypeTags.RECORD || type.tag == TypeTags.OBJECT || type.tag == TypeTags.ERROR) {
            compositeStack.push(type.tsymbol);
        }
        buff.writeByte(type.tag);
        type.accept(this);

        if (type.tag == TypeTags.RECORD || type.tag == TypeTags.OBJECT || type.tag == TypeTags.ERROR) {
            Object popped = compositeStack.pop();
            assert popped == type.tsymbol;
        }
    }

    @Override
    public void visit(BAnnotationType bAnnotationType) {
        throwUnimplementedError(bAnnotationType);
    }

    @Override
    public void visit(BArrayType bArrayType) {
        buff.writeByte(bArrayType.state.getValue());
        buff.writeInt(bArrayType.size);
        visitType(bArrayType.getElementType());
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
        visitType(bErrorType.reasonType);
        visitType(bErrorType.detailType);
    }

    @Override
    public void visit(BFiniteType bFiniteType) {
        BTypeSymbol tsymbol = bFiniteType.tsymbol;
        buff.writeInt(addStringCPEntry(tsymbol.name.value));
        buff.writeInt(bFiniteType.valueSpace.size());
        for (BLangExpression valueLiteral : bFiniteType.valueSpace) {
            if (!(valueLiteral instanceof BLangLiteral)) {
                throw new AssertionError(
                        "Type serialization is not implemented for finite type with value: " + valueLiteral.getKind());
            }
            visitType(valueLiteral.type);
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
            visitType(params);
        }
        visitType(bInvokableType.retType);
    }

    @Override
    public void visit(BJSONType bjsonType) {
        // Nothing to do
    }

    @Override
    public void visit(BMapType bMapType) {
        visitType(bMapType.constraint);
    }

    @Override
    public void visit(BTableType bTableType) {
        visitType(bTableType.constraint);
    }

    @Override
    public void visit(BStreamType bStreamType) {
        visitType(bStreamType.constraint);
    }

    @Override
    public void visit(BFutureType bFutureType) {
        visitType(bFutureType.constraint);
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
            visitType(memberType);
        }
    }

    @Override
    public void visit(BUnionType bUnionType) {
        buff.writeInt(bUnionType.getMemberTypes().size());
        for (BType memberType : bUnionType.getMemberTypes()) {
            if (!writeSelfRef(memberType.tsymbol)) {
                visitType(memberType);
            }
        }
    }

    @Override
    public void visit(BRecordType bRecordType) {
        BRecordTypeSymbol tsymbol = (BRecordTypeSymbol) bRecordType.tsymbol;

        buff.writeInt(addStringCPEntry(tsymbol.name.value));
        buff.writeBoolean(bRecordType.sealed);
        visitType(bRecordType.restFieldType);
        buff.writeInt(bRecordType.fields.size());
        for (BField field : bRecordType.fields) {
            // TODO add position
            buff.writeInt(addStringCPEntry(field.name.value));
            buff.writeByte(getVisibility(field.symbol).value());
            visitType(field.type);
        }

        BAttachedFunction initializerFunc = tsymbol.initializerFunc;

        buff.writeInt(addStringCPEntry(initializerFunc.funcName.value));
        buff.writeByte(getVisibility(initializerFunc.symbol).value());
        visitType(initializerFunc.type);
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

        buff.writeInt(addStringCPEntry(tSymbol.name.value));
        buff.writeBoolean((tSymbol.flags & Flags.ABSTRACT) == Flags.ABSTRACT); // Abstract object or not
        buff.writeInt(bObjectType.fields.size());
        for (BField field : bObjectType.fields) {
            buff.writeInt(addStringCPEntry(field.name.value));
            // TODO add position
            buff.writeByte(getVisibility(field.symbol).value());
            visitType(field.type);
        }
        List<BAttachedFunction> attachedFuncs;
        if (tSymbol.kind == SymbolKind.OBJECT) {
            attachedFuncs = ((BObjectTypeSymbol) tSymbol).attachedFuncs;
        } else {
            attachedFuncs = new ArrayList<>();
        }
        buff.writeInt(attachedFuncs.size());
        for (BAttachedFunction attachedFunc : attachedFuncs) {
            buff.writeInt(addStringCPEntry(attachedFunc.funcName.value));
            buff.writeByte(getVisibility(attachedFunc.symbol).value());
            visitType(attachedFunc.type);
        }
    }

    private boolean writeSelfRef(BTypeSymbol tsymbol) {
        int i = compositeStack.indexOf(tsymbol);
        if (i >= 0) {
            buff.writeByte(TYPE_TAG_SELF);
            buff.writeInt(i);
            return true;
        }
        return false;
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
        if (Symbols.isOptional(symbol)) {
            return Visibility.OPTIONAL;
        } else if (Symbols.isPublic(symbol)) {
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
