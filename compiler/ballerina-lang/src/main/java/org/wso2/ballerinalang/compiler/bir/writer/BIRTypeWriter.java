package org.wso2.ballerinalang.compiler.bir.writer;

import io.netty.buffer.ByteBuf;
import org.wso2.ballerinalang.compiler.bir.model.Visibility;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLAttributesType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;

/**
 * Writes bType to a Byte Buffer in binary format.
 * A ConstPool is used to store string typed information.
 */
public class BIRTypeWriter implements TypeVisitor {
    private final ByteBuf buff;

    private final ConstantPool cp;

    public BIRTypeWriter(ByteBuf buff, ConstantPool cp) {
        this.buff = buff;
        this.cp = cp;
    }

    @Override
    public void visit(BAnnotationType bAnnotationType) {
        throwUnimplementedError(bAnnotationType);
    }

    @Override
    public void visit(BArrayType bArrayType) {
        buff.writeByte(bArrayType.tag);
        bArrayType.getElementType().accept(this);
    }

    @Override
    public void visit(BBuiltInRefType bBuiltInRefType) {
        throwUnimplementedError(bBuiltInRefType);
    }

    @Override
    public void visit(BErrorType bErrorType) {
        throwUnimplementedError(bErrorType);
    }

    @Override
    public void visit(BFiniteType bFiniteType) {
        throwUnimplementedError(bFiniteType);
    }

    @Override
    public void visit(BIntermediateCollectionType bIntermediateCollectionType) {
        throwUnimplementedError(bIntermediateCollectionType);
    }

    @Override
    public void visit(BInvokableType bInvokableType) {
        buff.writeByte(bInvokableType.tag);
        buff.writeInt(bInvokableType.paramTypes.size());
        for (BType params : bInvokableType.paramTypes) {
            params.accept(this);
        }
        bInvokableType.retType.accept(this);
    }

    @Override
    public void visit(BJSONType bjsonType) {
        throwUnimplementedError(bjsonType);
    }

    @Override
    public void visit(BMapType bMapType) {
        buff.writeByte(bMapType.tag);
        bMapType.constraint.accept(this);
    }

    @Override
    public void visit(BNilType bNilType) {
        buff.writeByte(bNilType.tag);
    }

    @Override
    public void visit(BNoType bNoType) {
        buff.writeByte(bNoType.tag);
    }

    @Override
    public void visit(BPackageType bPackageType) {
        throwUnimplementedError(bPackageType);
    }

    @Override
    public void visit(BServiceType bServiceType) {
        throwUnimplementedError(bServiceType);
    }

    @Override
    public void visit(BStructureType bStructureType) {

        throwUnimplementedError(bStructureType);
    }

    @Override
    public void visit(BTupleType bTupleType) {
        throwUnimplementedError(bTupleType);
    }

    @Override
    public void visit(BUnionType bUnionType) {
        buff.writeByte(bUnionType.tag);
        buff.writeInt(bUnionType.memberTypes.size());
        for (BType memberType : bUnionType.memberTypes) {
            memberType.accept(this);
        }
    }

    @Override
    public void visit(BRecordType bRecordType) {
        buff.writeByte(bRecordType.tag);
        buff.writeBoolean(bRecordType.sealed);
        bRecordType.restFieldType.accept(this);
        buff.writeInt(bRecordType.fields.size());
        for (BField field : bRecordType.fields) {
            // TODO add position
            buff.writeInt(addStringCPEntry(field.name.value));
            field.type.accept(this);
        }
    }

    @Override
    public void visit(BObjectType bObjectType) {
        buff.writeByte(bObjectType.tag);
        buff.writeInt(bObjectType.fields.size());
        for (BField field : bObjectType.fields) {
            buff.writeInt(addStringCPEntry(field.name.value));
            // TODO add position
            buff.writeByte(getVisibility(field.symbol).value());
            field.type.accept(this);
        }
    }

    @Override
    public void visit(BType bType) {
        buff.writeByte(bType.tag);
    }

    @Override
    public void visit(BXMLAttributesType bxmlAttributesType) {
        throwUnimplementedError(bxmlAttributesType);
    }

    @Override
    public void visit(BXMLType bxmlType) {
        throwUnimplementedError(bxmlType);
    }

    private void throwUnimplementedError(BType bType) {
        throw new AssertionError("Type serialization is not implemented for " + bType.getClass());
    }

    private int addStringCPEntry(String value) {
        return cp.addCPEntry(new StringCPEntry(value));
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
}
