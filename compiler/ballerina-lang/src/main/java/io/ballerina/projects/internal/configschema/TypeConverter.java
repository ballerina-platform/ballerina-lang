/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects.internal.configschema;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.util.TypeTags.BOOLEAN;
import static org.wso2.ballerinalang.compiler.util.TypeTags.BYTE;
import static org.wso2.ballerinalang.compiler.util.TypeTags.DECIMAL;
import static org.wso2.ballerinalang.compiler.util.TypeTags.FLOAT;

/**
 * Util to to convert Ballerina type to JSON type.
 *
 * @since 2.0.0
 */
public class TypeConverter {

    static final String PROPERTIES = "properties";
    static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    static final String TYPE = "type";
    // Stores already visited complex types against the type name
    private Map<String, VisitedType> visitedTypeMap = new HashMap<>();

    private VisitedType getVisitedType(String typeName) {
        if (visitedTypeMap.containsKey(typeName)) {
            return visitedTypeMap.get(typeName);
        }
        return null;
    }

    public void addVisitedTypeEntry(String typeName) {
        visitedTypeMap.put(typeName, new VisitedType());
    }

    private void completeVisitedTypeEntry(String typeName, JsonObject typeNode) {
        VisitedType visitedType = visitedTypeMap.get(typeName);
        visitedType.setCompleted(true);
        visitedType.setTypeNode(typeNode);
    }

    /**
     * Get the type as a JSONObject.
     *
     * @param type Ballerina type as BType
     * @return JsonObject with type details
     */
    JsonObject getType(BType type) {
        JsonObject typeNode = new JsonObject();
        type = Types.getReferredType(type);
        if (TypeTags.isSimpleBasicType(type.tag)) {
            String typeVal = getSimpleType(type);
            typeNode.addProperty(TYPE, typeVal);
        } else {
            if (TypeTags.INTERSECTION == type.tag && type instanceof BIntersectionType intersectionType) {
                BType effectiveType = Types.getImpliedType(type);
                if (TypeTags.isSimpleBasicType(effectiveType.tag)) {
                    String typeVal = getSimpleType(effectiveType);
                    typeNode.addProperty(TYPE, typeVal);
                    return typeNode;
                }
                
                VisitedType visitedType = getVisitedType(effectiveType.toString());
                if (visitedType != null) {
                    if (visitedType.isCompleted()) {
                        return visitedType.getTypeNode();
                    }

                    JsonObject nullType = new JsonObject();
                    nullType.addProperty(TYPE, "null");
                    return nullType;
                } else {
                    visitedTypeMap.put(effectiveType.toString(), new VisitedType());
                }

                if (TypeTags.ARRAY == effectiveType.tag && effectiveType instanceof BArrayType arrayType) {
                    generateArrayType(typeNode, arrayType);
                }
                if (TypeTags.RECORD == effectiveType.tag && effectiveType instanceof BRecordType recordType) {
                    typeNode = generateRecordType(recordType, intersectionType);
                }
                if (TypeTags.MAP == effectiveType.tag && effectiveType instanceof BMapType mapType) {
                    generateMapType(typeNode, mapType);
                }
                if (TypeTags.UNION == effectiveType.tag && effectiveType instanceof BUnionType unionType) {
                    generateUnionType(typeNode, unionType);
                }
                if (TypeTags.TABLE == effectiveType.tag && effectiveType instanceof BTableType tableType) {
                    generateTableType(typeNode, tableType);
                }
                completeVisitedTypeEntry(effectiveType.toString(), typeNode);
            } else if (TypeTags.UNION == type.tag && type instanceof BUnionType unionType) {
                // Handles enums
                generateUnionType(typeNode, unionType);
            }
            // When the type is a union of singletons
            if (TypeTags.FINITE == type.tag && type instanceof BFiniteType finiteType) {
                JsonArray enumArray = new JsonArray();
                // Singletons can be mapped to enum in JSON
                getEnumArray(enumArray, finiteType);
                typeNode.add("enum", enumArray);
            }
        }

        return typeNode;
    }

    private void generateTableType(JsonObject typeNode, BTableType effectiveType) {
        typeNode.addProperty(TYPE, "array");
        typeNode.add("items", getType(effectiveType.getConstraint()));
    }

    /**
     * Add array type details to the given type object.
     *
     * @param typeNode JsonObject representing type
     * @param effectiveType  BArrayType with array details
     */
    private void generateArrayType(JsonObject typeNode, BArrayType effectiveType) {
        typeNode.addProperty(TYPE, "array");
        typeNode.add("items", getType(effectiveType.getElementType()));
    }

    /**
     * Generate a record type JSON object.
     *
     * @param effectiveType BRecordType with record details
     * @return JsonObject with record details
     */
    private JsonObject generateRecordType(BRecordType effectiveType, BIntersectionType intersectionType) {
        JsonObject typeNode;
        LinkedHashMap<String, BField> fieldLinkedHashMap = effectiveType.getFields();
        JsonObject effectiveTypeNode = new JsonObject();
        JsonArray requiredFields = new JsonArray();
        for (Map.Entry<String, BField> key : fieldLinkedHashMap.entrySet()) {
            BField field = key.getValue();
            JsonObject fieldTypeNode = getType(field.getType());
            effectiveTypeNode.add(field.getName().getValue(), fieldTypeNode);
            if (!Symbols.isOptional(field.symbol)) {
                requiredFields.add(field.getName().getValue());
            }
        }
        typeNode = createObjNode(effectiveTypeNode);
        // Set required fields
        if (!requiredFields.isEmpty()) {
            typeNode.add("required", requiredFields);
        }
        BTypeSymbol intersectionSymbol = intersectionType.tsymbol;
        // The tsymbol name is implicitly empty
        if (intersectionSymbol.name != Names.EMPTY) {
            typeNode.addProperty("name", intersectionSymbol.toString().trim());
        } else {
            // Get record type and set the type name as a property
            for (BType bType : intersectionType.getConstituentTypes()) {
                // Does not consider anonymous records
                if (bType.tag == TypeTags.TYPEREFDESC) {
                    // Revisit with https://github.com/ballerina-platform/ballerina-lang/issues/24078
                    typeNode.addProperty("name", bType.toString().trim());
                }
            }
        }
        return typeNode;
    }

    /**
     * Add map type details to the given type object.
     *
     * @param typeNode JsonObject representing type
     * @param effectiveType  BMapType with map details
     */
    private void generateMapType(JsonObject typeNode, BMapType effectiveType) {
        typeNode.addProperty(TYPE, "object");
        typeNode.add(ADDITIONAL_PROPERTIES, getType(effectiveType.getConstraint()));
    }

    /**
     * Add union type details to the given type object.
     *
     * @param typeNode JsonObject representing type
     * @param effectiveType  BUnionType with union details
     */
    private void generateUnionType(JsonObject typeNode, BUnionType effectiveType) {
        LinkedHashSet<BType> members = effectiveType.getMemberTypes();
        JsonArray memberArray = new JsonArray();
        JsonArray enumArray = new JsonArray();
        // Check if this union is an enum
        updateUnionMembers(members, memberArray, enumArray);
        if (enumArray.isEmpty() && !memberArray.isEmpty()) {
            typeNode.add("anyOf", memberArray);
        } else if (!enumArray.isEmpty() && memberArray.isEmpty()) {
            typeNode.add("enum", enumArray);
        } else if (!enumArray.isEmpty() && !memberArray.isEmpty()) {
            JsonObject memberObj = new JsonObject();
            memberObj.add("enum", enumArray);
            memberArray.add(memberObj);
            typeNode.add("anyOf", memberArray);
        }
    }

    /**
     * Update the union member details into the member array and enum array.
     *
     * @param members the members of a union type
     * @param memberArray JSON array to hold non-enum union members
     * @param enumArray JSON array to hold enum union members
     */
    private void updateUnionMembers(LinkedHashSet<BType> members, JsonArray memberArray, JsonArray enumArray) {
        for (BType member : members) {
            if (TypeTags.isSimpleBasicType(member.tag) ||
                    (TypeTags.INTERSECTION == member.tag && member instanceof BIntersectionType)) {
                JsonObject memberObj = getType(member);
                memberArray.add(memberObj);
            } else if (TypeTags.FINITE == member.tag && member instanceof BFiniteType finiteType) {
                getEnumArray(enumArray, finiteType);
            } else if (TypeTags.TYPEREFDESC == member.tag && member instanceof BTypeReferenceType typeReferenceType) {
                // When union member refers to another union type, update those union members as well
                BType referredType = typeReferenceType.referredType;
                if (TypeTags.UNION == referredType.tag && referredType instanceof BUnionType unionType) {
                    LinkedHashSet<BType> subMembers = unionType.getMemberTypes();
                    updateUnionMembers(subMembers, memberArray, enumArray);
                }
            }
        }
    }

    /**
     * Add enum type details to the given JSON array.
     *
     * @param enumArray JSON array to add the enum values
     * @param finiteType BFiniteType to retrieve enum values from
     */
    private static void getEnumArray(JsonArray enumArray, BFiniteType finiteType) {
        Object[] values = finiteType.getValueSpace().toArray();
        for (Object finiteValue : values) {
            if (finiteValue instanceof BLangNumericLiteral numericLiteral) {
                BType bType = numericLiteral.getBType();
                // In the BLangNumericLiteral the integer typed values are represented as numeric values
                // while the decimal values are represented as String
                Object value = numericLiteral.getValue();
                if (TypeTags.isIntegerTypeTag(bType.tag)) {
                    // Any integer can be considered as a long and added as a numeric value to the enum array
                    if (value instanceof Long l) {
                        enumArray.add(l);
                    }
                } else {
                    enumArray.add(Double.parseDouble(value.toString()));
                }
            } else if (finiteValue instanceof BLangLiteral bLangLiteral) {
                enumArray.add(bLangLiteral.getValue().toString());
            }
        }
    }

    /**
     * Get simple type name as String.
     *
     * @param type BType representing a simple type
     * @return simple type name as String
     */
    private static String getSimpleType(BType type) {
        if (TypeTags.isIntegerTypeTag(type.tag)) {
            return "integer";
        } else if (TypeTags.isStringTypeTag(type.tag)) {
            return "string";
        } else {
            switch (type.tag) {
                case FLOAT:
                case DECIMAL:
                    return "number";
                case BOOLEAN:
                    return "boolean";
                case BYTE:
                    return "integer";
                default:
                    return "";
            }
        }
    }

    /**
     * Create a JsonObject adding the given variable node as a property.
     *
     * @param modVarNode JsonObject representing module variable
     * @return JsonObject with variable node
     */
    static JsonObject createObjNode(JsonObject modVarNode) {
        JsonObject node = new JsonObject();
        node.addProperty(TYPE, "object");
        node.add(PROPERTIES, modVarNode);
        node.addProperty(ADDITIONAL_PROPERTIES, false);
        return node;
    }

}
