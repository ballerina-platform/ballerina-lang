package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.wso2.ballerinalang.compiler.semantics.model.UniqueTypeVisitor;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.HashSet;

/**
 * IsAnydataUniqueVisitor to check if a type is anydata.
 *
 * This is introduced to handle cyclic unions.
 * @since slp4
 */
public class IsAnydataUniqueVisitor implements UniqueTypeVisitor<Boolean> {

    private final HashSet<BType> visited;
    private final boolean isAnydata;

    public IsAnydataUniqueVisitor() {
        visited = new HashSet<>();
        isAnydata = true;
    }

    public IsAnydataUniqueVisitor(HashSet<BType> visited) {
        this.visited = visited;
        isAnydata = true;
    }

    private boolean isAnydata(BType type) {
        return switch (Types.getImpliedType(type).tag) {
            case TypeTags.INT,
                 TypeTags.BYTE,
                 TypeTags.FLOAT,
                 TypeTags.DECIMAL,
                 TypeTags.STRING,
                 TypeTags.CHAR_STRING,
                 TypeTags.BOOLEAN,
                 TypeTags.JSON,
                 TypeTags.XML,
                 TypeTags.XML_TEXT,
                 TypeTags.XML_ELEMENT,
                 TypeTags.XML_COMMENT,
                 TypeTags.XML_PI,
                 TypeTags.NIL,
                 TypeTags.NEVER,
                 TypeTags.ANYDATA,
                 TypeTags.SIGNED8_INT,
                 TypeTags.SIGNED16_INT,
                 TypeTags.SIGNED32_INT,
                 TypeTags.UNSIGNED8_INT,
                 TypeTags.UNSIGNED16_INT,
                 TypeTags.UNSIGNED32_INT,
                 TypeTags.REGEXP -> true;
            default -> false;
        };
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
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BArrayType type) {
        if (isVisited(type)) {
            return isAnydata;
        }
        visited.add(type);
        return visit(type.eType);
    }

    @Override
    public Boolean visit(BBuiltInRefType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BAnyType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BAnydataType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BErrorType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BInvokableType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BJSONType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BMapType type) {
        if (isVisited(type)) {
            return isAnydata;
        }
        visited.add(type);
        return visit(type.constraint);
    }

    @Override
    public Boolean visit(BStreamType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BTypedescType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BParameterizedType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BNeverType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BNilType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BNoType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BPackageType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BStructureType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BTupleType type) {
        if (type.isAnyData != null) {
            return type.isAnyData;
        }
        if (!visited.add(type)) {
            return isAnydata;
        }
        for (BType memberType : type.getTupleTypes()) {
            if (!visit(memberType)) {
                type.isAnyData = false;
                return false;
            }
        }
        type.isAnyData = (type.restType == null) || visit(type.restType);
        return isAnydata;
    }

    @Override
    public Boolean visit(BIntersectionType type) {
        return visit(type.effectiveType);
    }

    @Override
    public Boolean visit(BTypeReferenceType type) {
        return visit(type.referredType);
    }

    @Override
    public Boolean visit(BXMLType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BTableType type) {
        return visit(type.constraint);
    }

    @Override
    public Boolean visit(BFiniteType type) {
        if (type.isAnyData != null) {
            return type.isAnyData;
        }
        for (BLangExpression value : type.getValueSpace()) {
            if (!visit(value.getBType())) {
                type.isAnyData = false;
                return false;
            }
        }
        type.isAnyData = true;
        return isAnydata;
    }

    @Override
    public Boolean visit(BObjectType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BUnionType type) {
        if (type.isAnyData != null) {
            return type.isAnyData;
        }
        if (isVisited(type)) {
            return isAnydata;
        }
        visited.add(type);
        for (BType member : type.getMemberTypes()) {
            if (!visit(member)) {
                type.isAnyData = false;
                return false;
            }
        }
        type.isAnyData = isAnydata;
        return isAnydata;
    }

    @Override
    public Boolean visit(BRecordType type) {
        if (type.isAnyData != null) {
            return type.isAnyData;
        }
        if (isVisited(type)) {
            return isAnydata;
        }
        visited.add(type);
        for (BField field : type.fields.values()) {
            if (!visit(field.type)) {
                type.isAnyData = false;
                return false;
            }
        }

        if (!type.sealed && (type.restFieldType == null)) {
            return false;
        }

        type.isAnyData = type.sealed || visit(type.restFieldType);
        return type.isAnyData;
    }

    @Override
    public Boolean visit(BType type) {
        return switch (type.tag) {
            case TypeTags.TABLE -> visit((BTableType) type);
            case TypeTags.ANYDATA -> visit((BAnydataType) type);
            case TypeTags.RECORD -> visit((BRecordType) type);
            case TypeTags.ARRAY -> visit((BArrayType) type);
            case TypeTags.UNION -> visit((BUnionType) type);
            case TypeTags.TYPEDESC -> visit((BTypedescType) type);
            case TypeTags.MAP -> visit((BMapType) type);
            case TypeTags.FINITE -> visit((BFiniteType) type);
            case TypeTags.TUPLE -> visit((BTupleType) type);
            case TypeTags.INTERSECTION -> visit((BIntersectionType) type);
            case TypeTags.TYPEREFDESC -> visit((BTypeReferenceType) type);
            case TypeTags.SIGNED8_INT,
                 TypeTags.SIGNED16_INT,
                 TypeTags.SIGNED32_INT,
                 TypeTags.UNSIGNED8_INT,
                 TypeTags.UNSIGNED16_INT,
                 TypeTags.UNSIGNED32_INT -> visit((BIntSubType) type);
            case TypeTags.XML_ELEMENT,
                 TypeTags.XML_PI,
                 TypeTags.XML_COMMENT,
                 TypeTags.XML_TEXT -> visit((BXMLSubType) type);
            default -> isAnydata(type);
        };
    }

    @Override
    public Boolean visit(BFutureType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BHandleType type) {
        return isAnydata(type);
    }

    @Override
    public Boolean visit(BIntSubType bHandleType) {
        return true;
    }

    @Override
    public Boolean visit(BXMLSubType bxmlSubType) {
        return true;
    }
}
