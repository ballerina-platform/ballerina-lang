/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.wso2.ballerinalang.compiler.semantics.model.UniqueTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BReadonlyType;
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
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import static java.util.Objects.hash;

/**
 * BType hash visitor to visit and generate a hash code for a given BType.
 *
 * @since 2.0.0
 */
public class TypeHashVisitor implements UniqueTypeVisitor<Integer> {
    private Map<BType, Integer> visited;
    private Map<Integer, Integer> generated;
    private Stack<BType> visiting;
    private Set<BType> unresolvedTypes;
    private Map<BType, Integer> cache;

    public TypeHashVisitor() {
        visited = new HashMap<>();
        generated = new HashMap<>();
        visiting = new Stack<>();
        unresolvedTypes = new HashSet<>();
        cache = new HashMap<>();
    }

    @Override
    public boolean isVisited(BType type) {
        return visited.containsKey(type);
    }

    @Override
    public void reset() {
        visiting.clear();
        visited.clear();
        generated.clear();
        unresolvedTypes.clear();
    }

    public Integer getHash(BType type) {
        Integer hash = cache.get(type);
        if (hash != null) {
            return hash;
        }
        hash = visit(type);
        cache.put(type, hash);
        return hash;
    }

    @Override
    public Integer visit(BType type) {
        if (type == null) {
            return 0;
        }

        switch (type.tag) {
            case TypeTags.ANY:
                return visit((BAnyType) type);
            case TypeTags.NIL:
                return visit((BNilType) type);
            case TypeTags.NEVER:
                return visit((BNeverType) type);
            case TypeTags.ANYDATA:
                return visit((BAnydataType) type);
            case TypeTags.ANNOTATION:
                return visit((BAnnotationType) type);
            case TypeTags.RECORD:
                return visit((BRecordType) type);
            case TypeTags.ERROR:
                return visit((BErrorType) type);
            case TypeTags.OBJECT:
                return visit((BObjectType) type);
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
            case TypeTags.TABLE:
                return visit((BTableType) type);
            case TypeTags.STREAM:
                return visit((BStreamType) type);
            case TypeTags.INTERSECTION:
                return visit((BIntersectionType) type);
            case TypeTags.READONLY:
                return visit((BReadonlyType) type);
            case TypeTags.PARAMETERIZED_TYPE:
                return visit((BParameterizedType) type);
            case TypeTags.SIGNED8_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED32_INT:
            case TypeTags.UNSIGNED8_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED32_INT:
                return visit((BIntSubType) type);
            case TypeTags.XML:
                return visit((BXMLType) type);
            case TypeTags.XML_ELEMENT:
            case TypeTags.XML_PI:
            case TypeTags.XML_COMMENT:
            case TypeTags.XML_TEXT:
                return visit((BXMLSubType) type);
            case TypeTags.TYPEREFDESC:
                return visit((BTypeReferenceType) type);
            case TypeTags.NONE:
                return 0;
            default: {
                if (isVisited(type)) {
                    return visited.get(type);
                }

                if (isCyclic(type)) {
                    return 0;
                }

                return addToVisited(type, baseHash(type));
            }
        }
    }

    @Override
    public Integer visit(BAnnotationType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), type.getKind().typeName());
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BArrayType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), type.size, type.state.getValue(), visit(type.eType));
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BBuiltInRefType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), type.getKind().typeName());
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BAnyType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), type.getKind().typeName());
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BFutureType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), visit(type.constraint));
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BHandleType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), type.getKind().typeName());
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BMapType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), visit(type.constraint));
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BStreamType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), visit(type.constraint), visit(type.completionType));
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BTypedescType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), visit(type.constraint));
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BXMLType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), visit(type.constraint));
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BAnydataType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), type.getKind().typeName());
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BErrorType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), type.typeIdSet, visit(type.detailType));
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BInvokableType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        List<Integer> paramTypesHashes = getTypesHashes(type.paramTypes);
        Integer hash = hash(baseHash(type), paramTypesHashes, visit(type.restType), visit(type.retType));
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BJSONType type) {
        return visit((BUnionType) type);
    }

    @Override
    public Integer visit(BParameterizedType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), type.paramIndex, visit(type.paramValueType));
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BNeverType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), Names.NEVER.value);
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BNilType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), Names.NIL_VALUE.value);
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BNoType type) {
        return 0;
    }

    @Override
    public Integer visit(BPackageType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), type.getKind().typeName());
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BTupleType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        List<Integer> tupleTypesHashes = getOrderedTypesHashes(type.getTupleTypes());
        Integer hash = hash(baseHash(type), tupleTypesHashes, visit(type.restType), type.flags, type.tsymbol);
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BIntersectionType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), visit(type.effectiveType), getTypesHashes(type.getConstituentTypes()));
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BTypeReferenceType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), visit(type.referredType), type.definitionName);
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BTableType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), type.fieldNameList,
                visit(type.constraint), visit(type.keyTypeConstraint));
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BFiniteType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        List<String> toSort = new ArrayList<>();
        for (BLangExpression bLangExpression : type.getValueSpace()) {
            String toString = bLangExpression.toString();
            toSort.add(toString);
        }
        toSort.sort(null);
        List<Integer> valueSpaceHashes = new ArrayList<>();
        for (String toString : toSort) {
            Integer hashCode = toString.hashCode();
            valueSpaceHashes.add(hashCode);
        }
        Integer hash = hash(baseHash(type), valueSpaceHashes);
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BStructureType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        List<Integer> fieldsHashes = getFieldsHashes(type.fields);
        List<Integer> typeInclHashes = getTypesHashes(type.typeInclusions);
        Integer hash = hash(baseHash(type), type.flags, fieldsHashes, typeInclHashes);
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BObjectType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        List<Integer> fieldsHashes = getFieldsHashes(type.fields);
        List<Integer> typeInclHashes = getTypesHashes(type.typeInclusions);
        List<Integer> attachedFunctionsHashes = getFunctionsHashes(((BObjectTypeSymbol) type.tsymbol).attachedFuncs);
        Integer hash = hash(baseHash(type), type.flags, fieldsHashes, typeInclHashes,
                attachedFunctionsHashes, type.typeIdSet);
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BRecordType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        List<Integer> fieldsHashes = getFieldsHashes(type.fields);
        List<Integer> typeInclHashes = getTypesHashes(type.typeInclusions);
        Integer hash = hash(baseHash(type), type.flags, type.sealed, fieldsHashes, typeInclHashes,
                visit(type.restFieldType));
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BUnionType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), type.isCyclic, getTypesHashes(type.getMemberTypes()), type.flags);
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BIntSubType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), Names.INT.value, type.name.getValue());
        return addToVisited(type, hash);
    }

    @Override
    public Integer visit(BXMLSubType type) {
        if (isVisited(type)) {
            return visited.get(type);
        }
        if (isCyclic(type)) {
            return 0;
        }
        Integer hash = hash(baseHash(type), Names.XML.value, type.name.getValue());
        return addToVisited(type, hash);
    }

    private boolean isCyclic(BType type) {
        if (unresolvedTypes.contains(type)) {
            return true;
        }
        visiting.push(type);
        unresolvedTypes.add(type);
        return false;
    }

    private Integer addToVisited(BType type, Integer hash) {
        Integer existing = Optional.ofNullable(generated.get(hash)).orElse(0);

        generated.put(hash, existing + 1);
        if (existing > 0 && !visited.containsKey(type)) {
            hash += existing;
        }
        assert visiting.pop() == type;
        visited.put(type, hash);
        return hash;
    }

    private Integer baseHash(BType type) {
        return hash(type.tag);
    }

    private List<Integer> getTypesHashes(Collection<BType> types) {
        List<Integer> list = new ArrayList<>();
        for (BType type : types) {
            Integer visit = visit(type);
            list.add(visit);
        }
        list.sort(Comparator.comparingInt(Integer::intValue));
        return list;
    }

    private List<Integer> getOrderedTypesHashes(List<BType> tupleTypes) {
        List<Integer> list = new ArrayList<>();
        for (BType tupleType : tupleTypes) {
            Integer visit = visit(tupleType);
            list.add(visit);
        }
        return list;
    }

    private List<Integer> getFieldsHashes(Map<String, BField> fields) {
        List<Integer> list = new ArrayList<>();
        for (BField f : fields.values()) {
            Integer hash = hash(f.name.value, f.symbol != null ? f.symbol.flags : null, visit(f.type));
            list.add(hash);
        }
        list.sort(Comparator.comparingInt(Integer::intValue));
        return list;
    }

    private List<Integer> getFunctionsHashes(List<BAttachedFunction> attachedFunctions) {
        List<Integer> list = new ArrayList<>();
        for (BAttachedFunction attachedFunction : attachedFunctions) {
            Integer functionHash = getFunctionHash(attachedFunction);
            list.add(functionHash);
        }
        list.sort(Comparator.comparingInt(Integer::intValue));
        return list;
    }

    private int getFunctionHash(BAttachedFunction attachedFunction) {
        if (attachedFunction == null) {
            return 0;
        }
        return hash(attachedFunction.funcName.value,
                attachedFunction.symbol != null ? attachedFunction.symbol.flags : null, visit(attachedFunction.type));
    }

}
