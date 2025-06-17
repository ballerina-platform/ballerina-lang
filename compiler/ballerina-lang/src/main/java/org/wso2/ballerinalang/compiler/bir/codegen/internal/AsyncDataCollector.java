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

import io.ballerina.identifier.Utils;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JVMModuleUtils.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LAMBDA_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_GENERATED_LAMBDAS_PER_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_LAMBDAS_CLASS_NAME;

/**
 * A data holder to keep async invocation related data which will be used by class generation.
 *
 * @since 1.3.0
 */
public class AsyncDataCollector {

    private final Map<String, LambdaClass> lambdas;
    private final PackageID packageID;
    private String currentSourceFileWithoutExt = null;
    private String currentSourceFileName = null;
    private int classIndex = 0;
    private int lambdaIndex = 0;

    public AsyncDataCollector(BIRNode.BIRPackage module) {
        this.lambdas = new HashMap<>();
        this.packageID = module.packageID;
    }

    public LambdaFunction addAndGetLambda(String funcName, BIRInstruction inst, boolean isAsync) {
        String encodedFuncName = Utils.encodeFunctionIdentifier(funcName);
        String enclosingClass = getModuleLevelClassName(packageID, MODULE_LAMBDAS_CLASS_NAME + classIndex);
        LambdaClass lambdaClass = lambdas.get(enclosingClass);
        if (lambdaClass == null) {
            enclosingClass = getModuleLevelClassName(packageID, MODULE_LAMBDAS_CLASS_NAME + classIndex);
            lambdaClass = new LambdaClass(currentSourceFileName);
            lambdas.put(enclosingClass, lambdaClass);
            lambdaIndex = 0;
        }
        String lambdaName;
        // We need to differentiate FP value and Async call for debugger purpose, hence using  different names for
        // generated lambdas.
        if (isAsync) {
            lambdaName = encodedFuncName + LAMBDA_PREFIX + lambdaIndex++ + "$";
        } else {
            lambdaName = encodedFuncName + "$lambda" + lambdaIndex++ + "$";
        }
        LambdaFunction lambdaFunction = new LambdaFunction(lambdaName, enclosingClass, inst);
        lambdaClass.lambdaFunctionList.add(lambdaFunction);
        if (lambdaClass.lambdaFunctionList.size() > MAX_GENERATED_LAMBDAS_PER_CLASS) {
            classIndex++;
        }
        return lambdaFunction;
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
}
