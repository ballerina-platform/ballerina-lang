package org.wso2.ballerinalang.compiler.bir;

import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;

import java.util.HashMap;

public class TypeDefAnalyzer implements TypeVisitor {

    private static TypeDefAnalyzer typeDefAnalyzerInstance;
    private BIRNode.BIRTypeDefinition currentParentTypeDef;
    public final HashMap<BType, BIRNode.BIRTypeDefinition> typeDefPool = new HashMap<>();

    private TypeDefAnalyzer() {

    }

    public static TypeDefAnalyzer getInstance() {
        if (typeDefAnalyzerInstance == null) {
            typeDefAnalyzerInstance = new TypeDefAnalyzer();
        }
        return typeDefAnalyzerInstance;
    }

    public void analyze(BIRNode.BIRPackage birPackage) {
        populateTypeDefPool(birPackage);
        birPackage.typeDefs.forEach(typeDef -> {
            currentParentTypeDef = typeDef;
            typeDef.type.accept(this);
        });
    }

    private void addDependency(BType bType) {
        BIRNode.BIRTypeDefinition childNode = getBIRTypeDef(bType);
        if (childNode == null || currentParentTypeDef == childNode) {
            return;
        }
        currentParentTypeDef.addChildNode(childNode);
        currentParentTypeDef = childNode;
    }

    private BIRNode.BIRTypeDefinition getBIRTypeDef(BType bType) {
        if (typeDefPool.containsKey(bType)) {
            return typeDefPool.get(bType);
        }
        return null;
    }

    private void populateTypeDefPool(BIRNode.BIRPackage birPackage) {
        birPackage.typeDefs.forEach(typeDef -> typeDefPool.putIfAbsent(typeDef.type, typeDef));
    }

    @Override
    public void visit(BAnnotationType bAnnotationType) {
        addDependency(bAnnotationType);
    }

    @Override
    public void visit(BArrayType bArrayType) {
        addDependency(bArrayType);
    }

    @Override
    public void visit(BBuiltInRefType bBuiltInRefType) {
        addDependency(bBuiltInRefType);
    }

    @Override
    public void visit(BAnyType bAnyType) {
        addDependency(bAnyType);
    }

    @Override
    public void visit(BAnydataType bAnydataType) {
        addDependency(bAnydataType);
    }

    @Override
    public void visit(BErrorType bErrorType) {
        addDependency(bErrorType);
    }

    @Override
    public void visit(BFiniteType bFiniteType) {
        addDependency(bFiniteType);
    }

    @Override
    public void visit(BInvokableType bInvokableType) {
        addDependency(bInvokableType);
    }

    @Override
    public void visit(BJSONType bjsonType) {
        addDependency(bjsonType);
    }

    @Override
    public void visit(BMapType bMapType) {
        addDependency(bMapType);
    }

    @Override
    public void visit(BStreamType bStreamType) {
        addDependency(bStreamType);
    }

    @Override
    public void visit(BTypedescType bTypedescType) {
        addDependency(bTypedescType);
    }

    @Override
    public void visit(BTypeReferenceType bTypeReferenceType) {
        addDependency(bTypeReferenceType);
        bTypeReferenceType.referredType.accept(this);
    }

    @Override
    public void visit(BParameterizedType bTypedescType) {
        addDependency(bTypedescType);
    }

    @Override
    public void visit(BNeverType bNeverType) {
        addDependency(bNeverType);
    }

    @Override
    public void visit(BNilType bNilType) {
        addDependency(bNilType);
    }

    @Override
    public void visit(BNoType bNoType) {
        addDependency(bNoType);
    }

    @Override
    public void visit(BPackageType bPackageType) {
        addDependency(bPackageType);
    }

    @Override
    public void visit(BStructureType bStructureType) {
        addDependency(bStructureType);
        bStructureType.fields.values().forEach(bType -> bType.type.accept(this));
    }

    @Override
    public void visit(BTupleType bTupleType) {
        addDependency(bTupleType);
        bTupleType.getMembers().forEach(bTupleMember -> bTupleMember.type.accept(this));
    }

    @Override
    public void visit(BUnionType bUnionType) {
        addDependency(bUnionType);
        bUnionType.getMemberTypes().forEach(member -> member.accept(this));
    }

    @Override
    public void visit(BIntersectionType bIntersectionType) {
        addDependency(bIntersectionType);
    }

    @Override
    public void visit(BXMLType bxmlType) {
        addDependency(bxmlType);
    }

    @Override
    public void visit(BTableType bTableType) {
        addDependency(bTableType);
    }

    @Override
    public void visit(BRecordType bRecordType) {
        addDependency(bRecordType);
        // TODO check whether the following foreach is correct
        bRecordType.fields.values().forEach(bType -> {
            bType.type.accept(this);
        });

    }

    @Override
    public void visit(BObjectType bObjectType) {
        addDependency(bObjectType);
        bObjectType.fields.values().forEach(bType -> bType.type.accept(this));
    }

    @Override
    public void visit(BType bType) {
        addDependency(bType);
    }

    @Override
    public void visit(BFutureType bFutureType) {
        addDependency(bFutureType);
    }

    @Override
    public void visit(BHandleType bHandleType) {
        addDependency(bHandleType);
    }
}
