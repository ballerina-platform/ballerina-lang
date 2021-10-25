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
package org.ballerinalang.configschemagenerator.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.projects.configurations.ConfigModuleDetails;
import io.ballerina.projects.configurations.ConfigVariable;
import io.ballerina.projects.util.ProjectConstants;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.util.TypeTags.BOOLEAN;
import static org.wso2.ballerinalang.compiler.util.TypeTags.BYTE;
import static org.wso2.ballerinalang.compiler.util.TypeTags.DECIMAL;
import static org.wso2.ballerinalang.compiler.util.TypeTags.FLOAT;

/**
 * Util to build JSON schema for configurable variables.
 *
 * @since 2.0.0
 */
public class ConfigSchemaBuilder {

    static final String PROPERTIES = "properties";
    static final String TYPE = "type";

    /**
     * Returns string content of config schema JSON.
     *
     * @param configDetails Map of configurable variables against module
     * @return config schema JSON content as String
     */
    public static String getConfigSchemaContent(Map<ConfigModuleDetails, List<ConfigVariable>> configDetails) {
        return getRootNode(configDetails).toString();
    }

    /**
     * Generate JSON schema node with configurable details.
     *
     * @param configDetails Map of configurable variables against module
     * @return JsonObject with configurable details
     */
    private static JsonObject getRootNode(Map<ConfigModuleDetails, List<ConfigVariable>> configDetails) {
        JsonObject rootNode = new JsonObject();
        // Create root node
        JsonObject root = new JsonObject();
        root.addProperty("$schema", "http://json-schema.org/draft-07/schema#");
        root.addProperty(TYPE, "object");
        root.add(PROPERTIES, rootNode);
        for (Map.Entry<ConfigModuleDetails, List<ConfigVariable>> configModuleDetails : configDetails.entrySet()) {
            List<ConfigVariable> configVariables = configModuleDetails.getValue();
            if (isSingleFileProject(configModuleDetails)) {
                // Set configurable variables at package level
                setConfigVariables(configVariables, rootNode);
                setRequiredConfigs(configVariables, root);
            } else {
                // Get org node
                JsonObject orgNode;
                JsonElement orgJsonNode = rootNode.get(configModuleDetails.getKey().orgName());
                if (orgJsonNode == null) {
                    orgNode = createObjNode(new JsonObject());
                } else {
                    orgNode = (JsonObject) orgJsonNode;
                }
                // Get package node
                JsonObject pkgNode;
                JsonObject orgPropertiesNode = (JsonObject) orgNode.get(PROPERTIES);
                JsonElement pkgJsonNode = orgPropertiesNode.get(configModuleDetails.getKey().packageName());
                if (pkgJsonNode == null) {
                    pkgNode = createObjNode(new JsonObject());
                    orgPropertiesNode.add(configModuleDetails.getKey().packageName(), pkgNode);
                } else {
                    pkgNode = (JsonObject) pkgJsonNode;
                }
                JsonObject packagePropertiesNode = (JsonObject) pkgNode.get(PROPERTIES);
                // Set configurable variables
                if (configModuleDetails.getKey().moduleName().equals(configModuleDetails.getKey().packageName())) {
                    // Set configurable variables at package level
                    setConfigVariables(configVariables, packagePropertiesNode);
                    setRequiredConfigs(configVariables, pkgNode);
                } else {
                    JsonObject modVarNode = new JsonObject();
                    JsonObject modObjNode = createObjNode(setConfigVariables(configVariables, modVarNode));
                    setRequiredConfigs(configVariables, modObjNode);
                    packagePropertiesNode.add(configModuleDetails.getKey().moduleName(), modObjNode);
                }
                rootNode.add(configModuleDetails.getKey().orgName(), orgNode);
            }
        }

        return root;
    }

    /**
     * Check if given module details are for a single file project.
     *
     * @param configModuleDetails key entry for config details map
     * @return boolean flag
     */
    private static boolean isSingleFileProject(Map.Entry<ConfigModuleDetails,
            List<ConfigVariable>> configModuleDetails) {
        ConfigModuleDetails moduleDetails = configModuleDetails.getKey();
        return moduleDetails.orgName().equals(ProjectConstants.ANON_ORG) &&
                moduleDetails.packageName().equals(ProjectConstants.DOT) &&
                moduleDetails.moduleName().equals(ProjectConstants.DOT);
    }

    /**
     * Add required configs to the given module object node.
     *
     * @param configVariables List of configurable variables
     * @param modObjNode      JsonObject representing the module
     */
    private static void setRequiredConfigs(List<ConfigVariable> configVariables, JsonObject modObjNode) {
        JsonArray requiredConfigs = getRequiredConfigs(configVariables);
        if (!requiredConfigs.isEmpty()) {
            modObjNode.add("required", requiredConfigs);
        }
    }

    /**
     * Get required configurable variables as a JsonArray.
     *
     * @param configVariables List of configurable variables
     * @return JsonArray with required configurable variables
     */
    private static JsonArray getRequiredConfigs(List<ConfigVariable> configVariables) {
        JsonArray requiredConfigs = new JsonArray();
        for (ConfigVariable configVariable : configVariables) {
            if (configVariable.isRequired()) {
                requiredConfigs.add(configVariable.name());
            }
        }
        return requiredConfigs;
    }

    /**
     * Set configurable variables in the given JSONObject.
     *
     * @param configVariables List of configurable variables
     * @param node            JSONObject to set the configurable details
     * @return JSONObject with configurable details
     */
    private static JsonObject setConfigVariables(List<ConfigVariable> configVariables,
                                                 JsonObject node) {
        for (ConfigVariable configVariable : configVariables) {
            JsonObject typeNode = getType(configVariable.type());
            typeNode.addProperty("description", configVariable.description());
            node.add(configVariable.name(), typeNode);
        }
        return node;
    }

    /**
     * Get the type as a JSONObject.
     *
     * @param type Ballerina type as BType
     * @return JsonObject with type details
     */
    private static JsonObject getType(BType type) {
        JsonObject typeNode = new JsonObject();
        if (TypeTags.isSimpleBasicType(type.tag)) {
            String typeVal = getSimpleType(type);
            typeNode.addProperty(TYPE, typeVal);
        } else {
            if (TypeTags.INTERSECTION == type.tag && type instanceof BIntersectionType) {
                Set<BType> subTypes = ((BIntersectionType) type).getConstituentTypes();
                for (BType subType : subTypes) {
                    if (TypeTags.READONLY == subType.tag) {
                        continue;
                    }
                    if (TypeTags.ARRAY == subType.tag && subType instanceof BArrayType) {
                        generateArrayType(typeNode, (BArrayType) subType);
                    }
                    if (TypeTags.RECORD == subType.tag && subType instanceof BRecordType) {
                        typeNode = generateRecordType((BRecordType) subType);
                    }
                    if (TypeTags.MAP == subType.tag && subType instanceof BMapType) {
                        generateMapType(typeNode, (BMapType) subType);
                    }
                    if (TypeTags.UNION == subType.tag && subType instanceof BUnionType) {
                        generateUnionType(typeNode, (BUnionType) subType);
                    }
                }
            }
        }
        return typeNode;
    }

    /**
     * Add array type details to the given type object.
     *
     * @param typeNode JsonObject representing type
     * @param subType  BArrayType with array details
     */
    private static void generateArrayType(JsonObject typeNode, BArrayType subType) {
        BType elementType = subType.getElementType();
        JsonObject subTypeNode = new JsonObject();
        if (TypeTags.isSimpleBasicType(elementType.tag)) {
            String typeVal = getSimpleType(elementType);
            subTypeNode.addProperty(TYPE, typeVal);
        }
        typeNode.addProperty(TYPE, "array");
        typeNode.add("items", subTypeNode);
    }

    /**
     * Generate a record type JSON object.
     *
     * @param subType BRecordType with record details
     * @return JsonObject with record details
     */
    private static JsonObject generateRecordType(BRecordType subType) {
        JsonObject typeNode;
        LinkedHashMap<String, BField> fieldLinkedHashMap = subType.getFields();
        JsonObject subTypeNode = new JsonObject();
        for (Map.Entry<String, BField> key : fieldLinkedHashMap.entrySet()) {
            BField field = key.getValue();
            JsonObject fieldTypeNode = new JsonObject();
            if (TypeTags.isSimpleBasicType(field.getType().tag)) {
                String typeVal = getSimpleType(field.getType());
                fieldTypeNode.addProperty(TYPE, typeVal);
            }
            subTypeNode.add(field.getName().getValue(), fieldTypeNode);
        }
        typeNode = createObjNode(subTypeNode);
        return typeNode;
    }

    /**
     * Add map type details to the given type object.
     *
     * @param typeNode JsonObject representing type
     * @param subType  BMapType with map details
     */
    private static void generateMapType(JsonObject typeNode, BMapType subType) {
        BType constraint = subType.getConstraint();
        typeNode.addProperty(TYPE, "object");
        JsonObject mapConstraintNode = new JsonObject();
        if (TypeTags.isSimpleBasicType(constraint.tag)) {
            String typeVal = getSimpleType(constraint);
            mapConstraintNode.addProperty(TYPE, typeVal);
        }
        typeNode.addProperty(TYPE, "object");
        typeNode.add("additionalProperties", mapConstraintNode);
    }

    /**
     * Add union type details to the given type object.
     *
     * @param typeNode JsonObject representing type
     * @param subType  BUnionType with union details
     */
    private static void generateUnionType(JsonObject typeNode, BUnionType subType) {
        LinkedHashSet<BType> members = subType.getMemberTypes();
        JsonArray memberArray = new JsonArray();
        for (BType member : members) {
            if (TypeTags.isSimpleBasicType(member.tag)) {
                String typeVal = getSimpleType(member);
                JsonObject memberObj = new JsonObject();
                memberObj.addProperty(TYPE, typeVal);
                memberArray.add(memberObj);
            }
        }
        typeNode.add("anyOf", memberArray);
    }

    /**
     * Create a JsonObject adding the given variable node as a property.
     *
     * @param modVarNode JsonObject representing module variable
     * @return JsonObject with variable node
     */
    private static JsonObject createObjNode(JsonObject modVarNode) {
        JsonObject node = new JsonObject();
        node.addProperty(TYPE, "object");
        node.add(PROPERTIES, modVarNode);
        return node;
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
                case BYTE:
                case FLOAT:
                case DECIMAL:
                    return "number";
                case BOOLEAN:
                    return "boolean";
                default:
                    return "";
            }
        }
    }
}
