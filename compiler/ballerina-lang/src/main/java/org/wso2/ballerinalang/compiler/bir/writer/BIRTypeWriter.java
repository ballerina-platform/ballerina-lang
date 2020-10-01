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
import io.netty.buffer.Unpooled;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.ByteCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.FloatCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.IntegerCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.PackageCPEntry;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BHandleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNeverType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.semantics.model.types.TypeFlags;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

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
        buff.writeInt(addStringCPEntry(type.name.getValue()));
        buff.writeInt(type.flags);
        buff.writeInt(TypeFlags.asMask(type.isNullable(), type.isAnydata(), type.isPureType()));
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
        // Write the error package and type name
        int orgCPIndex = addStringCPEntry(bErrorType.tsymbol.pkgID.orgName.value);
        int nameCPIndex = addStringCPEntry(bErrorType.tsymbol.pkgID.name.value);
        int versionCPIndex = addStringCPEntry(bErrorType.tsymbol.pkgID.version.value);
        int pkgIndex = cp.addCPEntry(new PackageCPEntry(orgCPIndex, nameCPIndex, versionCPIndex));
        buff.writeInt(pkgIndex);
        buff.writeInt(addStringCPEntry(bErrorType.tsymbol.name.value));
        // Write detail types.
        writeTypeCpIndex(bErrorType.detailType);
        writeTypeIds(bErrorType.typeIdSet);
    }

    private void writeTypeIds(BTypeIdSet typeIdSet) {
        buff.writeInt(typeIdSet.primary.size());
        for (BTypeIdSet.BTypeId bTypeId : typeIdSet.primary) {
            writeTypeId(bTypeId);
        }
        buff.writeInt(typeIdSet.secondary.size());
        for (BTypeIdSet.BTypeId bTypeId : typeIdSet.secondary) {
            writeTypeId(bTypeId);
        }
    }

    private void writeTypeId(BTypeIdSet.BTypeId bTypeId) {
        int orgCPIndex = addStringCPEntry(bTypeId.packageID.orgName.value);
        int nameCPIndex = addStringCPEntry(bTypeId.packageID.name.value);
        int versionCPIndex = addStringCPEntry(bTypeId.packageID.version.value);
        int pkgIndex = cp.addCPEntry(new PackageCPEntry(orgCPIndex, nameCPIndex, versionCPIndex));
        buff.writeInt(pkgIndex);
        buff.writeInt(addStringCPEntry(bTypeId.name));
        buff.writeBoolean(bTypeId.publicId);
    }

    @Override
    public void visit(BFiniteType bFiniteType) {
        BTypeSymbol tsymbol = bFiniteType.tsymbol;
        buff.writeInt(addStringCPEntry(tsymbol.name.value));
        buff.writeInt(tsymbol.flags);
        buff.writeInt(bFiniteType.getValueSpace().size());
        for (BLangExpression valueLiteral : bFiniteType.getValueSpace()) {
            if (!(valueLiteral instanceof BLangLiteral)) {
                throw new AssertionError(
                        "Type serialization is not implemented for finite type with value: " + valueLiteral.getKind());
            }
            writeTypeCpIndex(valueLiteral.type);
            writeValue(((BLangLiteral) valueLiteral).value, valueLiteral.type);
        }
    }

    @Override
    public void visit(BInvokableType bInvokableType) {
        buff.writeInt(bInvokableType.paramTypes.size());
        for (BType params : bInvokableType.paramTypes) {
            writeTypeCpIndex(params);
        }

        boolean restTypeExist = bInvokableType.restType != null;
        buff.writeBoolean(restTypeExist);
        if (restTypeExist) {
            writeTypeCpIndex(bInvokableType.restType);
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
    public void visit(BStreamType bStreamType) {
        writeTypeCpIndex(bStreamType.constraint);
        if (bStreamType.error != null) {
            buff.writeBoolean(true);
            writeTypeCpIndex(bStreamType.error);
        } else {
            buff.writeBoolean(false);
        }
    }

    @Override
    public void visit(BTypedescType typedescType) {

        writeTypeCpIndex(typedescType.constraint);
    }

    @Override
    public void visit(BParameterizedType type) {
        writeTypeCpIndex(type.paramValueType);
    }

    @Override
    public void visit(BFutureType bFutureType) {
        writeTypeCpIndex(bFutureType.constraint);
    }

    @Override
    public void visit(BHandleType bHandleType) {
    }

    @Override
    public void visit(BNeverType bNeverType) {
        // Nothing to do
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
        writeTypeIds(bServiceType.typeIdSet);
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
        if (bTupleType.restType != null) {
            buff.writeBoolean(true);
            writeTypeCpIndex(bTupleType.restType);
        } else {
            buff.writeBoolean(false);
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
    public void visit(BIntersectionType bIntersectionType) {
        buff.writeInt(bIntersectionType.getConstituentTypes().size());
        for (BType constituentType : bIntersectionType.getConstituentTypes()) {
            writeTypeCpIndex(constituentType);
        }

        writeTypeCpIndex(bIntersectionType.effectiveType);
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

        buff.writeInt(bRecordType.fields.size());
        for (BField field : bRecordType.fields.values()) {
            BSymbol symbol = field.symbol;
            buff.writeInt(addStringCPEntry(symbol.name.value));
            buff.writeInt(symbol.flags);
            writeMarkdownDocAttachment(buff, field.symbol.markdownDocumentation);
            writeTypeCpIndex(field.type);
        }

        BAttachedFunction initializerFunc = tsymbol.initializerFunc;
        if (initializerFunc == null) {
            buff.writeByte(0);
            return;
        }

        buff.writeByte(1);
        buff.writeInt(addStringCPEntry(initializerFunc.funcName.value));
        buff.writeInt(initializerFunc.symbol.flags);
        writeTypeCpIndex(initializerFunc.type);
    }

    @Override
    public void visit(BObjectType bObjectType) {
        //This is to say this is an object, this is a temporary fix object - 1, service - 0,
        // ideal fix would be to use the type tag to
        // differentiate. TODO fix later
        buff.writeByte(0);
        writeObjectAndServiceTypes(bObjectType);
        writeTypeIds(bObjectType.typeIdSet);
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
        buff.writeBoolean(Symbols.isFlagOn(tSymbol.flags, Flags.CLASS)); // Abstract object or not
        buff.writeBoolean(Symbols.isFlagOn(tSymbol.flags, Flags.CLIENT));
        buff.writeInt(bObjectType.fields.size());
        for (BField field : bObjectType.fields.values()) {
            buff.writeInt(addStringCPEntry(field.name.value));
            // TODO add position
            buff.writeInt(field.symbol.flags);
            writeMarkdownDocAttachment(buff, field.symbol.markdownDocumentation);
            writeTypeCpIndex(field.type);
        }
        List<BAttachedFunction> attachedFuncs;
        //TODO cleanup, there cannot be objects without attached function list and symbol kind other than object
        if (tSymbol.kind == SymbolKind.OBJECT) {
            attachedFuncs = new ArrayList<>(((BObjectTypeSymbol) tSymbol).attachedFuncs);
            if (((BObjectTypeSymbol) tSymbol).generatedInitializerFunc != null) {
                buff.writeByte(1);
                writeAttachFunction(((BObjectTypeSymbol) tSymbol).generatedInitializerFunc);
            } else {
                buff.writeByte(0);
            }
            if (((BObjectTypeSymbol) tSymbol).initializerFunc != null) {
                buff.writeByte(1);
                writeAttachFunction(((BObjectTypeSymbol) tSymbol).initializerFunc);
            } else {
                buff.writeByte(0);
            }
        } else {
            attachedFuncs = new ArrayList<>();
            buff.writeByte(0);
            buff.writeByte(0);
        }
        buff.writeInt(attachedFuncs.size());
        for (BAttachedFunction attachedFunc : attachedFuncs) {
            writeAttachFunction(attachedFunc);
        }
    }

    private void writeAttachFunction(BAttachedFunction attachedFunc) {
        buff.writeInt(addStringCPEntry(attachedFunc.funcName.value));
        buff.writeInt(attachedFunc.symbol.flags);
        writeTypeCpIndex(attachedFunc.type);
    }

    @Override
    public void visit(BType bType) {
        // Nothing to do
    }

    @Override
    public void visit(BXMLType bxmlType) {
        writeTypeCpIndex(bxmlType.constraint);
    }

    @Override
    public void visit(BTableType bTableType) {
        writeTypeCpIndex(bTableType.constraint);
        buff.writeBoolean(bTableType.fieldNameList != null);
        if (bTableType.fieldNameList != null) {
            buff.writeInt(bTableType.fieldNameList.size());
            for (String fieldName : bTableType.fieldNameList) {
                buff.writeInt(addStringCPEntry(fieldName));
            }
        }
        buff.writeBoolean(bTableType.keyTypeConstraint != null);
        if (bTableType.keyTypeConstraint != null) {
            writeTypeCpIndex(bTableType.keyTypeConstraint);
        }
    }

    public void writeMarkdownDocAttachment(ByteBuf buf, MarkdownDocAttachment markdownDocAttachment) {
        ByteBuf birbuf = Unpooled.buffer();
        if (markdownDocAttachment == null) {
            birbuf.writeBoolean(false);
        } else {
            birbuf.writeBoolean(true);

            birbuf.writeInt(markdownDocAttachment.description == null ? -1
                    : addStringCPEntry(markdownDocAttachment.description));
            birbuf.writeInt(markdownDocAttachment.returnValueDescription == null ? -1
                    : addStringCPEntry(markdownDocAttachment.returnValueDescription));
            birbuf.writeInt(markdownDocAttachment.parameters.size());
            for (MarkdownDocAttachment.Parameter parameter : markdownDocAttachment.parameters) {
                birbuf.writeInt(parameter.name == null ? -1
                        : addStringCPEntry(parameter.name));
                birbuf.writeInt(parameter.description == null ? -1
                        : addStringCPEntry(parameter.description));
            }
        }
        int length = birbuf.nioBuffer().limit();
        buf.writeInt(length);
        buf.writeBytes(birbuf.nioBuffer().array(), 0, length);
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

    private void writeValue(Object value, BType typeOfValue) {
        ByteBuf byteBuf = Unpooled.buffer();
        switch (typeOfValue.tag) {
            case TypeTags.INT:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
                byteBuf.writeInt(addIntCPEntry((Long) value));
                break;
            case TypeTags.BYTE:
                int byteValue = ((Number) value).intValue();
                byteBuf.writeInt(addByteCPEntry(byteValue));
                break;
            case TypeTags.FLOAT:
                // TODO:Remove the instanceof check by converting the float literal instance in Semantic analysis phase
                double doubleVal =
                        value instanceof String ? Double.parseDouble((String) value) : ((Number) value).doubleValue();
                byteBuf.writeInt(addFloatCPEntry(doubleVal));
                break;
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
            case TypeTags.DECIMAL:
                byteBuf.writeInt(addStringCPEntry(String.valueOf(value)));
                break;
            case TypeTags.BOOLEAN:
                byteBuf.writeBoolean((Boolean) value);
                break;
            case TypeTags.NIL:
                break;
            default:
                throw new UnsupportedOperationException("finite type value is not supported for type: " + typeOfValue);
        }

        int length = byteBuf.nioBuffer().limit();
        buff.writeInt(length);
        buff.writeBytes(byteBuf.nioBuffer().array(), 0, length);
    }
}
