/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.projectdesign;

import com.google.gson.JsonObject;
import io.ballerina.architecturemodelgenerator.ComponentModel;
import io.ballerina.projects.Package;

import java.util.Map;

/**
 * Provide utils functions for component model building.
 *
 * @since 2201.2.2
 */
public class Utils {
    public static String getQualifiedPackageName(ComponentModel.PackageId packageId) {

        return String.format("%s/%s:%s", packageId.getOrg(),
                packageId.getName(), packageId.getVersion());
    }

    public static boolean modelAlreadyExists(Map<String, JsonObject> componentModelMap, Package currentPackage) {

        ComponentModel.PackageId packageId = new ComponentModel.PackageId(currentPackage);
        return componentModelMap.containsKey(getQualifiedPackageName(packageId));
    }
}
