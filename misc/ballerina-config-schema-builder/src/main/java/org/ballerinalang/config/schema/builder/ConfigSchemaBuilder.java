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
package org.ballerinalang.config.schema.builder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.configurations.ConfigModuleDetails;
import io.ballerina.projects.configurations.ConfigVariable;

import java.util.List;
import java.util.Map;

import static org.ballerinalang.config.schema.builder.TypeConverter.PROPERTIES;

/**
 * Util to build JSON schema for configurable variables.
 *
 * @since 2.0.0
 */
public class ConfigSchemaBuilder {

    /**
     * Returns string content of config schema JSON.
     *
     * @param configDetails Map of configurable variables against module
     * @return config schema JSON content as String
     */
    public static String getConfigSchemaContent(Map<ConfigModuleDetails, List<ConfigVariable>> configDetails) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(getRootNode(configDetails));
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
        root.addProperty(TypeConverter.TYPE, "object");
        root.add(PROPERTIES, rootNode);
        root.addProperty(TypeConverter.ADDITIONAL_PROPERTIES, false);
        for (Map.Entry<ConfigModuleDetails, List<ConfigVariable>> configModuleDetails : configDetails.entrySet()) {
            List<ConfigVariable> configVariables = configModuleDetails.getValue();
            if (ProjectKind.SINGLE_FILE_PROJECT.equals(configModuleDetails.getKey().projectKind())) {
                // Set configurable variables at package level
                setConfigVariables(configVariables, rootNode);
                setRequiredConfigs(configVariables, root);
            } else {
                // Get org node
                JsonObject orgNode;
                JsonElement orgJsonNode = rootNode.get(configModuleDetails.getKey().orgName());
                if (orgJsonNode == null) {
                    orgNode = TypeConverter.createObjNode(new JsonObject());
                } else {
                    orgNode = (JsonObject) orgJsonNode;
                }
                // Get package node
                JsonObject pkgNode;
                JsonObject orgPropertiesNode = (JsonObject) orgNode.get(PROPERTIES);
                JsonElement pkgJsonNode = orgPropertiesNode.get(configModuleDetails.getKey().packageName());
                if (pkgJsonNode == null) {
                    pkgNode = TypeConverter.createObjNode(new JsonObject());
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
                    JsonObject modObjNode = TypeConverter.createObjNode(setConfigVariables(configVariables,
                            modVarNode));
                    setRequiredConfigs(configVariables, modObjNode);
                    packagePropertiesNode.add(configModuleDetails.getKey().moduleName(), modObjNode);
                }
                rootNode.add(configModuleDetails.getKey().orgName(), orgNode);
            }
        }

        return root;
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
            JsonObject typeNode = TypeConverter.getType(configVariable.type());
            typeNode.addProperty("description", configVariable.description());
            node.add(configVariable.name(), typeNode);
        }
        return node;
    }

}
