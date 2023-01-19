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
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.semantics.analyzer.IsAnydataUniqueVisitor;
import org.wso2.ballerinalang.compiler.semantics.analyzer.IsPureTypeUniqueVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEnumSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
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
import java.util.Objects;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.bir.writer.BIRWriterUtils.getBIRAnnotAttachments;

/**
 * Writes bType to a Byte Buffer in binary format.
 * A ConstPool is used to store string typed information.
 * 
 * @since 0.995.0
 */
public class BIRTypeWriter implements TypeVisitor {
    private final ByteBuf buff;

    private final ConstantPool cp;
    private static IsPureTypeUniqueVisitor isPureTypeUniqueVisitor = new IsPureTypeUniqueVisitor();
    private static IsAnydataUniqueVisitor isAnydataUniqueVisitor = new IsAnydataUniqueVisitor();

    public BIRTypeWriter(ByteBuf buff, ConstantPool cp) {
        this.buff = buff;
        this.cp = cp;
    }

    public void visitType(BType type) {
        buff.writeByte(type.tag);
        buff.writeInt(addStringCPEntry(type.name.getValue()));
        buff.writeLong(type.flags);
        isPureTypeUniqueVisitor.reset();
        isAnydataUniqueVisitor.reset();
        buff.writeInt(TypeFlags.asMask(type.isNullable(), isAnydataUniqueVisitor.visit(type),
                isPureTypeUniqueVisitor.visit(type)));
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
        int pkgIndex = BIRWriterUtils.addPkgCPEntry(bErrorType.tsymbol.pkgID, cp);
        buff.writeInt(pkgIndex);
        buff.writeInt(addStringCPEntry(bErrorType.tsymbol.name.value));
        // Write detail types.
        writeTypeCpIndex(bErrorType.detailType);
        writeTypeIds(bErrorType.typeIdSet);
    }

    private void writeTypeIds(BTypeIdSet typeIdSet) {
        buff.writeInt(typeIdSet.getPrimary().size());
        for (BTypeIdSet.BTypeId bTypeId : typeIdSet.getPrimary()) {
            writeTypeId(bTypeId);
        }
        buff.writeInt(typeIdSet.getSecondary().size());
        for (BTypeIdSet.BTypeId bTypeId : typeIdSet.getSecondary()) {
            writeTypeId(bTypeId);
        }
    }

    private void writeTypeId(BTypeIdSet.BTypeId bTypeId) {
        int pkgIndex = BIRWriterUtils.addPkgCPEntry(bTypeId.packageID, cp);
        buff.writeInt(pkgIndex);
        buff.writeInt(addStringCPEntry(bTypeId.name));
        buff.writeBoolean(bTypeId.publicId);
    }

    @Override
    public void visit(BFiniteType bFiniteType) {
        BTypeSymbol tsymbol = bFiniteType.tsymbol;
        buff.writeInt(addStringCPEntry(tsymbol.name.value));
        buff.writeLong(tsymbol.flags);
        buff.writeInt(bFiniteType.getValueSpace().size());
        for (BLangExpression valueLiteral : bFiniteType.getValueSpace()) {
            if (!(valueLiteral instanceof BLangLiteral)) {
                throw new AssertionError(
                        "Type serialization is not implemented for finite type with value: " + valueLiteral.getKind());
            }
            writeTypeCpIndex(valueLiteral.getBType());
            writeValue(((BLangLiteral) valueLiteral).value, valueLiteral.getBType());
        }
    }

    @Override
    public void visit(BInvokableType bInvokableType) {
        boolean isAnyFunction = Symbols.isFlagOn(bInvokableType.flags, Flags.ANY_FUNCTION);
        buff.writeBoolean(isAnyFunction); // write 1 if it’s an any function if not write 0

        if (isAnyFunction) {
            return;
        }

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
        boolean hasTSymbol = bInvokableType.tsymbol != null;
        buff.writeBoolean(hasTSymbol);
        if (!hasTSymbol) {
            return;
        }

        BInvokableTypeSymbol invokableTypeSymbol = (BInvokableTypeSymbol) bInvokableType.tsymbol;
        buff.writeInt(invokableTypeSymbol.params.size());
        for (BVarSymbol symbol : invokableTypeSymbol.params) {
            buff.writeInt(addStringCPEntry(symbol.name.value));
            buff.writeLong(symbol.flags);
            writeMarkdownDocAttachment(buff, symbol.markdownDocumentation);
            writeTypeCpIndex(symbol.type);
        }

        BVarSymbol restParam = invokableTypeSymbol.restParam;
        boolean restParamExists = restParam != null;
        buff.writeBoolean(restParamExists);
        if (restParamExists) {
            buff.writeInt(addStringCPEntry(restParam.name.value));
            buff.writeLong(restParam.flags);
            writeMarkdownDocAttachment(buff, restParam.markdownDocumentation);
            writeTypeCpIndex(restParam.type);
        }

        buff.writeInt(invokableTypeSymbol.defaultValues.size());
        invokableTypeSymbol.defaultValues.forEach((k, v) -> {
            buff.writeInt(addStringCPEntry(k));
            writeSymbolOfClosure(v);
        });
    }

    private void writeSymbolOfClosure(BInvokableSymbol invokableSymbol) {
        buff.writeInt(addStringCPEntry(invokableSymbol.name.value));
        buff.writeLong(invokableSymbol.flags);
        writeTypeCpIndex(invokableSymbol.type);
        writePackageIndex(invokableSymbol.type.tsymbol);

        buff.writeInt(invokableSymbol.params.size());
        for (BVarSymbol symbol : invokableSymbol.params) {
            buff.writeInt(addStringCPEntry(symbol.name.value));
            buff.writeLong(symbol.flags);
            writeMarkdownDocAttachment(buff, symbol.markdownDocumentation);
            writeTypeCpIndex(symbol.type);
        }
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
        writeTypeCpIndex(bStreamType.completionType);
    }

    @Override
    public void visit(BTypedescType typedescType) {

        writeTypeCpIndex(typedescType.constraint);
    }

    @Override
    public void visit(BTypeReferenceType typeReferenceType) {
        BTypeSymbol tsymbol = typeReferenceType.tsymbol;

        writePackageIndex(tsymbol);

        buff.writeInt(addStringCPEntry(typeReferenceType.definitionName));
        writeTypeCpIndex(typeReferenceType.referredType);
    }

    @Override
    public void visit(BParameterizedType type) {
        writeTypeCpIndex(type.paramValueType);
        buff.writeInt(type.paramIndex);
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
    public void visit(BStructureType bStructureType) {
        throwUnimplementedError(bStructureType);
    }

    @Override
    public void visit(BTupleType bTupleType) {
        List<BTupleMember> members = bTupleType.getMembers();
        buff.writeInt(members.size());
        for (int i = 0; i < members.size(); i++) {
            BTupleMember memberType = members.get(i);
            buff.writeInt(addStringCPEntry(Integer.toString(i)));
            buff.writeLong(memberType.symbol.flags);
            writeTypeCpIndex(memberType.type);
            BIRWriterUtils.writeAnnotAttachments(cp, buff,
                    getBIRAnnotAttachments(memberType.symbol.getAnnotations()));
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
        buff.writeBoolean(bUnionType.isCyclic);
        BTypeSymbol tsymbol = bUnionType.tsymbol;
        if (bUnionType.isCyclic && (tsymbol != null && !tsymbol.name.getValue().isEmpty())) {
            buff.writeBoolean(true);
            writePackageIndex(tsymbol);
            buff.writeInt(addStringCPEntry(bUnionType.tsymbol.name.value));
        } else {
            buff.writeBoolean(false);
        }
        writeMembers(bUnionType.getMemberTypes());
        writeMembers(bUnionType.getOriginalMemberTypes());

        if (tsymbol instanceof BEnumSymbol) {
            buff.writeBoolean(true);
            writeEnumSymbolInfo((BEnumSymbol) tsymbol);
        } else {
            buff.writeBoolean(false);
        }
    }

    private void writeMembers(Set<BType> memberTypes) {
        buff.writeInt(memberTypes.size());
        for (BType memberType : memberTypes) {
            writeTypeCpIndex(memberType);
        }
    }

    private void writeEnumSymbolInfo(BEnumSymbol symbol) {
        writePackageIndex(symbol);

        buff.writeInt(addStringCPEntry(symbol.name.value));

        buff.writeInt(symbol.members.size());
        for (BConstantSymbol member : symbol.members) {
            buff.writeInt(addStringCPEntry(member.name.value));
        }
    }

    private void writePackageIndex(BTypeSymbol tsymbol) {
        int pkgIndex = BIRWriterUtils.addPkgCPEntry(tsymbol.pkgID, cp);
        buff.writeInt(pkgIndex);
    }

    @Override
    public void visit(BIntersectionType bIntersectionType) {
        writeMembers(bIntersectionType.getConstituentTypes());
        writeTypeCpIndex(bIntersectionType.effectiveType);
    }

    @Override
    public void visit(BRecordType bRecordType) {
        BRecordTypeSymbol tsymbol = (BRecordTypeSymbol) bRecordType.tsymbol;

        // Write the package details in the form of constant pool entry TODO find a better approach
        writePackageIndex(tsymbol);

        BTypeDefinitionSymbol typDefSymbol = tsymbol.typeDefinitionSymbol;
        buff.writeInt(addStringCPEntry(Objects.requireNonNullElse(typDefSymbol, tsymbol).name.value));

        buff.writeBoolean(bRecordType.sealed);
        writeTypeCpIndex(bRecordType.restFieldType);

        buff.writeInt(bRecordType.fields.size());
        for (BField field : bRecordType.fields.values()) {
            BSymbol symbol = field.symbol;
            buff.writeInt(addStringCPEntry(symbol.name.value));
            buff.writeLong(symbol.flags);
            writeMarkdownDocAttachment(buff, field.symbol.markdownDocumentation);
            writeTypeCpIndex(field.type);
            BIRWriterUtils.writeAnnotAttachments(cp, buff,
                    getBIRAnnotAttachments(field.symbol.getAnnotations()));
        }

        BAttachedFunction initializerFunc = tsymbol.initializerFunc;
        if (initializerFunc == null) {
            buff.writeByte(0);
            return;
        }

        buff.writeByte(1);
        buff.writeInt(addStringCPEntry(initializerFunc.funcName.value));
        buff.writeLong(initializerFunc.symbol.flags);
        writeTypeCpIndex(initializerFunc.type);

        writeTypeInclusions(bRecordType.typeInclusions);
    }

    @Override
    public void visit(BObjectType bObjectType) {
        //This is to say this is an object, this is a temporary fix object - 1, service - 0,
        // ideal fix would be to use the type tag to
        // differentiate. TODO fix later
        if ((bObjectType.flags & Flags.SERVICE) == Flags.SERVICE) {
            buff.writeByte(1);
        } else {
            buff.writeByte(0);
        }
        writeObjectAndServiceTypes(bObjectType);
        writeTypeIds(bObjectType.typeIdSet);
    }

    private void writeObjectAndServiceTypes(BObjectType bObjectType) {
        BTypeSymbol tSymbol = bObjectType.tsymbol;

        // Write the package details in the form of constant pool entry TODO find a better approach
        writePackageIndex(tSymbol);

        BTypeDefinitionSymbol typDefSymbol = ((BObjectTypeSymbol) tSymbol).typeDefinitionSymbol;
        buff.writeInt(addStringCPEntry(Objects.requireNonNullElse(typDefSymbol, tSymbol).name.value));

        //TODO below two line are a temp solution, introduce a generic concept
        buff.writeBoolean(Symbols.isFlagOn(tSymbol.flags, Flags.CLASS)); // Abstract object or not
        buff.writeBoolean(Symbols.isFlagOn(tSymbol.flags, Flags.CLIENT));
        buff.writeInt(bObjectType.fields.size());
        for (BField field : bObjectType.fields.values()) {
            buff.writeInt(addStringCPEntry(field.name.value));
            // TODO add position
            buff.writeLong(field.symbol.flags);
            buff.writeBoolean(field.symbol.isDefaultable);
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

        writeTypeInclusions(bObjectType.typeInclusions);
    }

    private void writeAttachFunction(BAttachedFunction attachedFunc) {
        buff.writeInt(addStringCPEntry(attachedFunc.funcName.value));
        buff.writeInt(addStringCPEntry(attachedFunc.symbol.getOriginalName().getValue()));
        buff.writeLong(attachedFunc.symbol.flags);
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
        buff.writeBoolean(!bTableType.fieldNameList.isEmpty());
        if (!bTableType.fieldNameList.isEmpty()) {
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
            writeParamDocs(birbuf, markdownDocAttachment.parameters);

            if (markdownDocAttachment.deprecatedDocumentation != null) {
                birbuf.writeInt(addStringCPEntry(markdownDocAttachment.deprecatedDocumentation));
            } else {
                birbuf.writeInt(-1);
            }

            writeParamDocs(birbuf, markdownDocAttachment.deprecatedParams);
        }
        int length = birbuf.nioBuffer().limit();
        buf.writeInt(length);
        buf.writeBytes(birbuf.nioBuffer().array(), 0, length);
    }

    private void writeParamDocs(ByteBuf birBuf, List<MarkdownDocAttachment.Parameter> params) {
        birBuf.writeInt(params.size());
        for (MarkdownDocAttachment.Parameter param : params) {
            birBuf.writeInt(addStringCPEntry(param.name));
            birBuf.writeInt(addStringCPEntry(param.description));
        }
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

    private void writeTypeInclusions(List<BType> inclusions) {
        buff.writeInt(inclusions.size());
        for (BType inclusion : inclusions) {
            writeTypeCpIndex(inclusion);
        }
    }
}
