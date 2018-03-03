/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.StructInfo;

/**
 * {@code StructHelper} Utilities related to connector processing.
 *
 * @since 0.94
 */
public class StructHelper {

    protected static BStruct createAndGetStruct(Resource resource, String packageName, String structName) {
        PackageInfo packageInfo = getPackageInfo(resource);
        ProgramFile programFile = packageInfo.getProgramFile();

        PackageInfo structPackageInfo = programFile.getPackageInfo(packageName);
        StructInfo structInfo = structPackageInfo.getStructInfo(structName);
        BStructType structType = structInfo.getType();
        return new BStruct(structType);
    }

    protected static PackageInfo getPackageInfo(Resource resource) {
        ResourceInfo resourceInfo = ((BResource) resource).getResourceInfo();
        ServiceInfo serviceInfo = resourceInfo.getServiceInfo();
        return serviceInfo.getPackageInfo();
    }
}
