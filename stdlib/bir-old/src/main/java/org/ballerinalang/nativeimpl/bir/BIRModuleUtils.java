/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.bir;

import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.util.Names;

/**
 * This class contains utility functions to create BIRContext objects.
 *
 * @since 0.995.0
 */
public class BIRModuleUtils {

    static final String COMPILER_MODULE_CACHE = "compiler.module.cache";
    static final String COMPILER_NAMES = "compiler.names";
    static final String COMPILER_PACKAGE_LOADER = "compiler.package.loader";

    static final String BIR_MODULE_NAME = "ballerina/bir";
    static final String BIR_CONTEXT_RECORD_NAME = "BIRContext";
    static final String BIR_MODULE_ID_RECORD_NAME = "ModuleID";


    public static BMap<String, BValue> createBIRContext(ProgramFile programFile, PackageLoader modCache, Names names) {
        BMap<String, BValue> birContextObj = BLangConnectorSPIUtil.createBStruct(programFile,
                BIR_MODULE_NAME, BIR_CONTEXT_RECORD_NAME);
        birContextObj.addNativeData(COMPILER_PACKAGE_LOADER, modCache);
        birContextObj.addNativeData(COMPILER_NAMES, names);
        return birContextObj;
    }

    public static BMap<String, BValue> createModuleID(ProgramFile programFile,
                                                      String org,
                                                      String name,
                                                      String version,
                                                      boolean isUnnamed,
                                                      String sourceFilename) {
        BMap<String, BValue> moduleIdRec = BLangConnectorSPIUtil.createBStruct(programFile,
                BIR_MODULE_NAME, BIR_MODULE_ID_RECORD_NAME);
        moduleIdRec.put("org", new BString(org));
        moduleIdRec.put("name", new BString(name));
        moduleIdRec.put("modVersion", new BString(version));
        moduleIdRec.put("isUnnamed", new BBoolean(isUnnamed));
        moduleIdRec.put("sourceFilename", new BString(sourceFilename));
        return moduleIdRec;
    }
}
