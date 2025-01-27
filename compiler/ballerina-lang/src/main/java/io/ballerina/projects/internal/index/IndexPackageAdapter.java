/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.projects.internal.index;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserializer for {@link IndexPackage}.
 *
 * @since 2201.12.0
 */
public class IndexPackageAdapter implements JsonDeserializer<IndexPackage> {

    /**
     * Deserialize the Json data to an {@link IndexPackage} object.
     *
     * @param json The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context The context which deserialization is taking place
     * @return The deserialized {@link IndexPackage} object
     */
    @Override
    public IndexPackage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();

        PackageDescriptor packageDescriptor = deserializePackageDescriptor(json);

        String supportedPlatform = jsonObject.get("platform").getAsString();
        String ballerinaVersion = jsonObject.get("ballerina_version").getAsString();
        boolean isDeprecated = jsonObject.get("is_deprecated").getAsBoolean();
        String deprecationMsg = jsonObject.get("deprecation_message").getAsString();

        List<PackageDescriptor> dependencies = deserializeDependencies(jsonObject.get("dependencies"));
        List<IndexPackage.Module> modules = deserializeModules(jsonObject.get("modules"));

        return IndexPackage.from(
                packageDescriptor,
                supportedPlatform,
                ballerinaVersion,
                dependencies,
                modules,
                isDeprecated,
                deprecationMsg);
    }

    private List<PackageDescriptor> deserializeDependencies(JsonElement json) {
        List<PackageDescriptor> list = new ArrayList<>();
        JsonArray jsonArray = json.getAsJsonArray();
        for (JsonElement element : jsonArray) {
            list.add(deserializePackageDescriptor(element));
        }
        return list;
    }

    private PackageDescriptor deserializePackageDescriptor(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        PackageOrg packageOrg = PackageOrg.from(jsonObject.get("org").getAsString());
        PackageName packageName = PackageName.from(jsonObject.get("name").getAsString());
        PackageVersion packageVersion = PackageVersion.from(jsonObject.get("version").getAsString());
        return PackageDescriptor.from(packageOrg, packageName, packageVersion);
    }

    private List<IndexPackage.Module> deserializeModules(JsonElement modules) {
        List<IndexPackage.Module> list = new ArrayList<>();
        JsonArray jsonArray = modules.getAsJsonArray();
        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            String moduleName = jsonObject.get("name").getAsString();
            list.add(new IndexPackage.Module(moduleName));
        }
        return list;
    }
}
