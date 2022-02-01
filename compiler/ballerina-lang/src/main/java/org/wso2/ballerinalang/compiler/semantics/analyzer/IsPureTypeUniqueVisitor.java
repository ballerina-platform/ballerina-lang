package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.wso2.ballerinalang.compiler.semantics.model.UniqueTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BHandleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntSubType;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.HashSet;

/**
 * IsPureTypeUniqueVisitor to check if a type is pure data.
 *
 * This is introduced to handle cyclic unions.
 * @since slp4
 */
public class IsPureTypeUniqueVisitor implements UniqueTypeVisitor<Boolean> {

    private HashSet<BType> visited;
    private boolean isPureType;

    public IsPureTypeUniqueVisitor() {
        visited = new HashSet<>();
        isPureType = true;
    }

    public IsPureTypeUniqueVisitor(HashSet<BType> visited) {
        this.visited = visited;
        isPureType = true;
    }

    private boolean isAnydata(BType type) {
        switch (type.tag) {
            case TypeTags.INT:
            case TypeTags.BYTE:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.STRING:
            case TypeTags.BOOLEAN:
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.XML_TEXT:
            case TypeTags.TABLE:
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.ANYDATA:
            case TypeTags.SIGNED8_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED32_INT:
            case TypeTags.UNSIGNED8_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.CHAR_STRING:
                return true;
            case TypeTags.TYPEREFDESC:
                return isAnydata(((BTypeReferenceType) type).referredType);
            default:
                return false;
        }
    }

    private boolean isAnyData(BType type) {
        return type.tag == TypeTags.ANYDATA || isAnydata(type);
    }

    @Override
    public boolean isVisited(BType type) {
        return visited.contains(type);
    }

    @Override
    public void reset() {
        visited.clear();
    }

    @Override
    public Boolean visit(BAnnotationType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BArrayType type) {
        if (isVisited(type)) {
            return isPureType;
        }
        visited.add(type);
        return visit(type.eType);
    }

    @Override
    public Boolean visit(BBuiltInRefType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BAnyType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BAnydataType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BErrorType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BFiniteType type) {
        IsAnydataUniqueVisitor isAnydataUniqueVisitor = new IsAnydataUniqueVisitor(visited);
        return isAnydataUniqueVisitor.visit(type);
    }

    @Override
    public Boolean visit(BInvokableType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BJSONType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BMapType type) {
        if (isVisited(type)) {
            return isPureType;
        }
        visited.add(type);
        return visit(type.constraint);
    }

    @Override
    public Boolean visit(BStreamType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BTypedescType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BParameterizedType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BNeverType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BNilType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BNoType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BPackageType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BStructureType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BTupleType type) {
        IsAnydataUniqueVisitor isAnydataUniqueVisitor = new IsAnydataUniqueVisitor(visited);
        return isAnydataUniqueVisitor.visit(type);
    }

    @Override
    public Boolean visit(BUnionType type) {
        if (type.isPureType != null) {
            return type.isPureType;
        }
        if (isVisited(type)) {
            return isPureType;
        }
        visited.add(type);
        for (BType member : type.getMemberTypes()) {
            if (!visit(member)) {
                type.isPureType = false;
                return false;
            }
        }
        type.isPureType = isPureType;
        return isPureType;
    }

    @Override
    public Boolean visit(BIntersectionType type) {
        return visit(type.effectiveType);
    }

    public Boolean visit(BTypeReferenceType type) {
        return visit(type.referredType);
    }

    @Override
    public Boolean visit(BXMLType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BTableType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BRecordType type) {

        IsAnydataUniqueVisitor isAnydataUniqueVisitor = new IsAnydataUniqueVisitor(visited);
        return isAnydataUniqueVisitor.visit(type);
    }

    @Override
    public Boolean visit(BObjectType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BType type) {
        switch (type.tag) {
            case TypeTags.TABLE:
                return visit((BTableType) type);
            case TypeTags.ANYDATA:
                return visit((BAnydataType) type);
            case TypeTags.RECORD:
                return visit((BRecordType) type);
            case TypeTags.ARRAY:
                return visit((BArrayType) type);
            case TypeTags.UNION:
                return visit((BUnionType) type);
            case TypeTags.TYPEDESC:
                return visit((BTypedescType) type);
            case TypeTags.MAP:
                return visit((BMapType) type);
            case TypeTags.FINITE:
                return visit((BFiniteType) type);
            case TypeTags.TUPLE:
                return visit((BTupleType) type);
            case TypeTags.INTERSECTION:
                return visit((BIntersectionType) type);
            case TypeTags.TYPEREFDESC:
                return visit((BTypeReferenceType) type);
            case TypeTags.SIGNED8_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED32_INT:
            case TypeTags.UNSIGNED8_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED32_INT:
                return visit((BIntSubType) type);
        }
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BFutureType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BHandleType type) {
        return isAnyData(type);
    }

    @Override
    public Boolean visit(BIntSubType type) {
        return true;
    }

    @Override
    public Boolean visit(BXMLSubType bxmlSubType) {
        return isAnyData(bxmlSubType);
    }
}
