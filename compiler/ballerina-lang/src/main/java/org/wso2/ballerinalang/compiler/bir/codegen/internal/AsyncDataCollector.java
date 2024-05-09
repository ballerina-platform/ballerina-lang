/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen.internal;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_GENERATED_LAMBDAS_PER_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_LAMBDAS_CLASS_NAME;

/**
 * A data holder to keep async invocation related data which will be used by class generation.
 *
 * @since 1.3.0
 */
public class AsyncDataCollector {

    private final Map<String, LambdaClass> lambdas;
    private final Map<String, String> lambdaVsClassMap;
    private final Map<String, ScheduleFunctionInfo> strandMetaDataMap;
    private final PackageID packageID;
    private String currentSourceFileWithoutExt = null;
    private String currentSourceFileName = null;
    private static int classIndex = -1;
    private int lambdaIndex = 0;

    public AsyncDataCollector(BIRNode.BIRPackage module) {
        this.lambdas = new HashMap<>();
        this.lambdaVsClassMap = new HashMap<>();
        this.strandMetaDataMap = new HashMap<>();
        this.packageID = module.packageID;
    }

    public int getLambdaIndex() {
        return lambdaIndex;
    }

    public void add(String lambdaName, BIRInstruction callInstruction) {
        if (lambdaVsClassMap.containsKey(lambdaName)) {
            return;
        }
        String enclosingClass = getModuleLevelClassName(packageID, MODULE_LAMBDAS_CLASS_NAME + classIndex,
                currentSourceFileWithoutExt, "/");
        LambdaClass lambdaClass = lambdas.get(enclosingClass);
        if (lambdaClass == null || lambdaClass.lambdaFunctionList.size() > MAX_GENERATED_LAMBDAS_PER_CLASS) {
            enclosingClass = getModuleLevelClassName(packageID, MODULE_LAMBDAS_CLASS_NAME + ++classIndex,
                    currentSourceFileWithoutExt, "/");
            lambdaClass = new LambdaClass(currentSourceFileName);
            lambdas.put(enclosingClass, lambdaClass);
        }
        lambdaClass.lambdaFunctionList.add(new LambdaFunction(lambdaName, callInstruction));
        lambdaVsClassMap.put(lambdaName, enclosingClass);
        lambdaIndex++;
    }

    public String getEnclosingClass(String lambdaName) {
        return lambdaVsClassMap.get(lambdaName);
    }

    public Map<String, LambdaClass> getLambdaClasses() {
        return lambdas;
    }

    public void setCurrentSourceFileName(String currentSourceFileName) {
        this.currentSourceFileName = currentSourceFileName;
    }

    public void setCurrentSourceFileWithoutExt(String currentSourceFileWithoutExt) {
        this.currentSourceFileWithoutExt = currentSourceFileWithoutExt;
    }

    public Map<String, ScheduleFunctionInfo> getStrandMetadata() {
        return strandMetaDataMap;
    }
}
