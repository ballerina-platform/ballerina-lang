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

import io.ballerina.types.Atom;
import io.ballerina.types.AtomicType;
import io.ballerina.types.BasicTypeBitSet;
import io.ballerina.types.Bdd;
import io.ballerina.types.CellSemType;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.EnumerableCharString;
import io.ballerina.types.EnumerableDecimal;
import io.ballerina.types.EnumerableFloat;
import io.ballerina.types.EnumerableString;
import io.ballerina.types.FixedLengthArray;
import io.ballerina.types.FunctionAtomicType;
import io.ballerina.types.ListAtomicType;
import io.ballerina.types.MappingAtomicType;
import io.ballerina.types.ProperSubtypeData;
import io.ballerina.types.RecAtom;
import io.ballerina.types.SemType;
import io.ballerina.types.TypeAtom;
import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.CharStringSubtype;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.NonCharStringSubtype;
import io.ballerina.types.subtypedata.Range;
import io.ballerina.types.subtypedata.StringSubtype;
import io.ballerina.types.subtypedata.XmlSubtype;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.SemNamedType;
import org.wso2.ballerinalang.util.Flags;

import java.math.BigDecimal;
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
public class BIRTypeWriter extends TypeVisitor {
    private final ByteBuf buff;

    private final ConstantPool cp;

    public BIRTypeWriter(ByteBuf buff, ConstantPool cp) {
        this.buff = buff;
        this.cp = cp;
    }

    public void visitType(BType type) {
        writeSemType(type.semType());
        buff.writeByte(type.tag);
        buff.writeInt(addStringCPEntry(type.name.getValue()));
        buff.writeLong(type.flags);
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
        buff.writeInt(bFiniteType.valueSpace.length);
        for (SemNamedType semNamedType:bFiniteType.valueSpace) {
            writeSemNamedType(semNamedType);
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
    public void visitNilType(BType bType) {
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

        writeTypeInclusions(bRecordType.typeInclusions);

        buff.writeInt(tsymbol.defaultValues.size());
        tsymbol.defaultValues.forEach((k, v) -> {
            buff.writeInt(addStringCPEntry(k));
            writeSymbolOfClosure(v);
        });
    }

    @Override
    public void visit(BObjectType bObjectType) {
        writeObjectAndServiceTypes(bObjectType);
        writeTypeIds(bObjectType.typeIdSet);
    }

    private void writeObjectAndServiceTypes(BObjectType bObjectType) {
        BTypeSymbol tSymbol = bObjectType.tsymbol;

        // Write the package details in the form of constant pool entry TODO find a better approach
        writePackageIndex(tSymbol);

        BTypeDefinitionSymbol typDefSymbol = ((BObjectTypeSymbol) tSymbol).typeDefinitionSymbol;
        buff.writeInt(addStringCPEntry(Objects.requireNonNullElse(typDefSymbol, tSymbol).name.value));

        buff.writeLong(tSymbol.flags);
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

    private void writeTypeInclusions(List<BType> inclusions) {
        buff.writeInt(inclusions.size());
        for (BType inclusion : inclusions) {
            writeTypeCpIndex(inclusion);
        }
    }

    private void writeNullableString(String nullableString) {
        boolean hasNonNullString = nullableString != null;
        buff.writeBoolean(hasNonNullString);
        if (hasNonNullString) {
            buff.writeInt(addStringCPEntry(nullableString));
        }
    }

    private void writeSemNamedType(SemNamedType semNamedType) {
        writeSemType(semNamedType.semType());
        writeNullableString(semNamedType.optName().orElse(null));
    }

    // --------------------------------------- Writing SemType ----------------------------------------------

    private void writeSemType(SemType semType) {
        boolean hasSemType = semType != null;
        buff.writeBoolean(hasSemType);
        if (!hasSemType) {
            return;
        }

        boolean isUniformTypeBitSet = semType instanceof BasicTypeBitSet;
        buff.writeBoolean(isUniformTypeBitSet);

        if (isUniformTypeBitSet) {
            buff.writeInt(((BasicTypeBitSet) semType).bitset);
            return;
        }

        ComplexSemType complexSemType = (ComplexSemType) semType;
        buff.writeInt(complexSemType.all.bitset);
        buff.writeInt(complexSemType.some.bitset);

        ProperSubtypeData[] subtypeDataList = complexSemType.subtypeDataList;
        buff.writeByte(subtypeDataList.length);
        for (ProperSubtypeData psd: subtypeDataList) {
            writeProperSubtypeData(psd);
        }
    }

    private void writeProperSubtypeData(ProperSubtypeData psd) {
        if (psd instanceof Bdd bdd) {
            buff.writeByte(1);
            writeBdd(bdd);
        } else if (psd instanceof IntSubtype intSubtype) {
            buff.writeByte(2);
            writeIntSubtype(intSubtype);
        } else if (psd instanceof BooleanSubtype booleanSubtype) {
            buff.writeByte(3);
            buff.writeBoolean(booleanSubtype.value);
        } else if (psd instanceof FloatSubtype floatSubtype) {
            buff.writeByte(4);
            writeFloatSubtype(floatSubtype);
        } else if (psd instanceof DecimalSubtype decimalSubtype) {
            buff.writeByte(5);
            writeDecimalSubtype(decimalSubtype);
        } else if (psd instanceof StringSubtype stringSubtype) {
            buff.writeByte(6);
            writeStringSubtype(stringSubtype);
        } else if (psd instanceof XmlSubtype xmlSubtype) {
            buff.writeByte(7);
            buff.writeInt(xmlSubtype.primitives);
            writeBdd(xmlSubtype.sequence);
        } else {
            throw new IllegalStateException("Unknown ProperSubtypeData");
        }
    }

    private void writeBdd(Bdd bdd) {
        buff.writeBoolean(bdd instanceof BddNode);
        if (bdd instanceof BddNode bddNode) {
            writeBddNode(bddNode);
        } else {
            BddAllOrNothing bddAllOrNothing = (BddAllOrNothing) bdd;
            buff.writeBoolean(bddAllOrNothing.isAll());
        }
    }

    private void writeBddNode(BddNode bddNode) {
        Atom atom = bddNode.atom;
        boolean isRecAtom = atom instanceof RecAtom;
        buff.writeBoolean(isRecAtom);
        if (isRecAtom) {
            RecAtom recAtom = (RecAtom) atom;
            buff.writeInt(recAtom.index);
        } else {
            TypeAtom typeAtom = (TypeAtom) atom;
            buff.writeLong(typeAtom.index);
            AtomicType atomicType = typeAtom.atomicType;
            if (atomicType instanceof MappingAtomicType mappingAtomicType) {
                buff.writeByte(1);
                writeMappingAtomicType(mappingAtomicType);
            } else if (atomicType instanceof ListAtomicType listAtomicType) {
                buff.writeByte(2);
                writeListAtomicType(listAtomicType);
            } else {
                buff.writeByte(3);
                FunctionAtomicType fat = (FunctionAtomicType) atomicType;
                writeSemType(fat.paramType);
                writeSemType(fat.retType);
            }
        }
        writeBdd(bddNode.left);
        writeBdd(bddNode.middle);
        writeBdd(bddNode.right);
    }

    private void writeMappingAtomicType(MappingAtomicType mat) {
        String[] names = mat.names;
        buff.writeInt(names.length);
        for (String name : names) {
            buff.writeInt(addStringCPEntry(name));
        }
        CellSemType[] types = mat.types;
        buff.writeInt(types.length);
        for (CellSemType type : types) {
            writeSemType(type);
        }
        writeSemType(mat.rest);
    }

    private void writeListAtomicType(ListAtomicType lat) {
        FixedLengthArray fla = lat.members;
        List<CellSemType> initial = fla.initial;
        buff.writeInt(initial.size());
        for (SemType type : initial) {
            writeSemType(type);
        }
        buff.writeInt(fla.fixedLength);
        writeSemType(lat.rest);
    }

    private void writeIntSubtype(IntSubtype intSubtype) {
        Range[] ranges = intSubtype.ranges;
        buff.writeInt(ranges.length);
        for (Range range : ranges) {
            buff.writeLong(range.min);
            buff.writeLong(range.max);
        }
    }

    private void writeFloatSubtype(FloatSubtype floatSubtype) {
        buff.writeBoolean(floatSubtype.allowed);
        buff.writeInt(floatSubtype.values.length);
        for (EnumerableFloat ef : floatSubtype.values) {
            buff.writeDouble(ef.value);
        }
    }

    private void writeDecimalSubtype(DecimalSubtype decimalSubtype) {
        buff.writeBoolean(decimalSubtype.allowed);
        buff.writeInt(decimalSubtype.values.length);
        for (EnumerableDecimal ed : decimalSubtype.values) {
            BigDecimal bigDecimal = ed.value;
            buff.writeInt(bigDecimal.scale());
            byte[] unscaledValueBytes = bigDecimal.unscaledValue().toByteArray();
            buff.writeInt(unscaledValueBytes.length);
            buff.writeBytes(unscaledValueBytes);
        }
    }

    private void writeStringSubtype(StringSubtype stringSubtype) {
        CharStringSubtype charData = stringSubtype.getChar();
        buff.writeBoolean(charData.allowed);
        EnumerableCharString[] charValues = charData.values;
        buff.writeInt(charValues.length);
        for (EnumerableCharString ecs : charValues) {
            buff.writeInt(addStringCPEntry(ecs.value));
        }
        NonCharStringSubtype nonCharData = stringSubtype.getNonChar();
        buff.writeBoolean(nonCharData.allowed);
        EnumerableString[] nonCharValues = nonCharData.values;
        buff.writeInt(nonCharValues.length);
        for (EnumerableString ecs : nonCharValues) {
            buff.writeInt(addStringCPEntry(ecs.value));
        }
    }

    // --------------------------------------- End of SemType -----------------------------------------------
}
