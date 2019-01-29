package org.wso2.ballerinalang.compiler.semantics.model;

import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntermediateCollectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLAttributesType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;

/**
 * Visit ballerina types and maps them to instances T.
 */
public interface TypeVisitor {
    void visit(BAnnotationType bAnnotationType);

    void visit(BArrayType bArrayType);

    void visit(BBuiltInRefType bBuiltInRefType);

    void visit(BErrorType bErrorType);

    void visit(BFiniteType bFiniteType);

    void visit(BIntermediateCollectionType bIntermediateCollectionType);

    void visit(BInvokableType bInvokableType);

    void visit(BJSONType bjsonType);

    void visit(BMapType bMapType);

    void visit(BNilType bNilType);

    void visit(BNoType bNoType);

    void visit(BPackageType bPackageType);

    void visit(BServiceType bServiceType);

    void visit(BStructureType bStructureType);

    void visit(BTupleType bTupleType);

    void visit(BUnionType bUnionType);

    void visit(BXMLAttributesType bxmlAttributesType);

    void visit(BXMLType bxmlType);

    void visit(BType bType);
}
