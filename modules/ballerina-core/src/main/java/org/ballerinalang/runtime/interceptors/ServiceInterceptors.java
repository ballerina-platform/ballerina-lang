/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.runtime.interceptors;

import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.runtime.config.BLangConfigurationManager;
import org.ballerinalang.runtime.config.interceptors.InterceptorConfig;
import org.ballerinalang.runtime.config.interceptors.ServiceInterceptorConfig;
import org.ballerinalang.runtime.interceptors.ResourceInterceptor.Type;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager class service interceptors.
 */
public class ServiceInterceptors {

    public static final String REQUEST_INTERCEPTOR_NAME = "requestInterceptor";
    public static final String RESPONSE_INTERCEPTOR_NAME = "responseInterceptor";

    private static ServiceInterceptors instance = new ServiceInterceptors();

    private boolean isInitialized = false;
    private Map<String, ServerConnectorInterceptorInfo> serverConnectorInterceptorInfoMap = new HashMap<>();
    private Map<String, Boolean> enabledServerConnectorMap = new HashMap<>();


    private ServiceInterceptors() {
    }

    public static ServiceInterceptors getInstance() {
        return instance;
    }

    public void initialize() {
        if (isInitialized) {
            return;
        }
        if (BLangConfigurationManager.getInstance().getConfiguration().getServiceInterceptors() == null ||
                BLangConfigurationManager.getInstance().getConfiguration().getServiceInterceptors().size() == 0) {
            isInitialized = false;
        }
        for (ServiceInterceptorConfig interceptorConfig :
                BLangConfigurationManager.getInstance().getConfiguration().getServiceInterceptors()) {

            enabledServerConnectorMap.put(interceptorConfig.getServerConnector(), interceptorConfig.isEnable());
            if (!interceptorConfig.isEnable()) {
                continue;
            }
            ServerConnectorInterceptorInfo interceptorInfo = new ServerConnectorInterceptorInfo(interceptorConfig
                    .getServerConnector());
            serverConnectorInterceptorInfoMap.put(interceptorInfo.serverConnector, interceptorInfo);

            if (interceptorConfig.getDeploymentDirectory() == null) {
                throw new BLangRuntimeException("invalid interceptor deployment directory");
            }

            File interceptorDeploymentDirectory = new File(interceptorConfig.getDeploymentDirectory());
            if (!interceptorDeploymentDirectory.exists()) {
                throw new BLangRuntimeException("interceptor deployment directory doesn't exist");
            }
            interceptorInfo.deploymentPath = interceptorDeploymentDirectory.toPath();
            BLangProgramLoader bLangProgramLoader = new BLangProgramLoader();

            // Assumed all archives are semantically valid, before archived.
            bLangProgramLoader.disableSemanticAnalyzer();
            // Building Request interceptors
            if (interceptorConfig.getRequest() != null) {
                interceptorInfo.requestChain = buildFunctionChain(interceptorConfig.getRequest(), interceptorInfo,
                        bLangProgramLoader, Type.REQUEST);
            }
            // Building Response interceptors.
            if (interceptorConfig.getResponse() != null) {
                interceptorInfo.responseChain = buildFunctionChain(interceptorConfig.getResponse(), interceptorInfo,
                        bLangProgramLoader, Type.RESPONSE);
            }
        }
        isInitialized = true;
    }


    public boolean isEnabled(String serviceConnectorID) {
        if (!isInitialized) {
            return false;
        }
        Boolean enabled = enabledServerConnectorMap.get(serviceConnectorID);
        if (enabled == null) {
            return false;
        }
        return enabled;
    }

    public ServerConnectorInterceptorInfo getServerConnectorInterceptorInfo(String id) {
        return this.serverConnectorInterceptorInfoMap.get(id);
    }

    private ArrayList<ResourceInterceptor> buildFunctionChain(List<InterceptorConfig> interceptorConfigs,
                                                              ServerConnectorInterceptorInfo interceptorInfo,
                                                              BLangProgramLoader bLangProgramLoader, Type type) {
        ArrayList<ResourceInterceptor> resourceInterceptors = new ArrayList<>();
        for (InterceptorConfig interceptorConfig : interceptorConfigs) {
            ProgramFile programFile;

            Path ballerinaArchive = Paths.get(interceptorInfo.deploymentPath.toString(), interceptorConfig
                    .getArchiveName());
            // Cache values.
            if (interceptorInfo.programFileMap.containsKey(ballerinaArchive.toString())) {
                programFile = interceptorInfo.programFileMap.get(ballerinaArchive.toString());
            } else {
                programFile = bLangProgramLoader.loadMainProgramFile(interceptorInfo.deploymentPath, ballerinaArchive);
                PackageInfo mainPkgInfo = programFile.getPackageInfo(programFile.getMainPackageName());
                // Init main package.
                Context bContext = new Context(programFile);
                bContext.initFunction = true;
                BLangFunctions.invokeFunction(programFile, mainPkgInfo, mainPkgInfo.getInitFunctionInfo(), bContext);
                if (bContext.getError() != null) {
                    String stackTraceStr = BLangVMErrors.getPrintableStackTrace(bContext.getError());
                    throw new BLangRuntimeException("error: " + stackTraceStr);
                }
                interceptorInfo.programFileMap.put(ballerinaArchive.toString(), programFile);
            }
            PackageInfo packageInfo = programFile.getPackageInfo(interceptorConfig.getPackageName());
            if (packageInfo == null) {
                throw new BLangRuntimeException("no exported package found called " +
                        interceptorConfig.getPackageName());
            }
            resourceInterceptors.add(new ResourceInterceptor(programFile, packageInfo, getInterceptorFunction
                    (packageInfo, type)));
        }
        return resourceInterceptors;
    }

    private FunctionInfo getInterceptorFunction(PackageInfo packageInfo, Type type) {
        String funcName = RESPONSE_INTERCEPTOR_NAME;
        if (type == Type.REQUEST) {
            funcName = REQUEST_INTERCEPTOR_NAME;
        }
        BType[] interceptorInTypes = {BTypes.typeMessage};
        BType[] interceptorOutTypes = {BTypes.typeBoolean, BTypes.typeMessage};

        FunctionInfo function = packageInfo.getFunctionInfo(funcName);
        if (function != null && matchArgTypes(function.getParamTypes(), interceptorInTypes)
                && matchArgTypes(function.getRetParamTypes(), interceptorOutTypes)) {
            return function;
        }
        throw new BLangRuntimeException("no " + (type == Type.REQUEST ? "request" : "response") +
                "Interceptor function found");
    }

    private static boolean matchArgTypes(BType[] actualArgTypes, BType[] expectedArgTypes) {
        boolean matching = false;
        if (actualArgTypes.length == expectedArgTypes.length) {
            matching = true;
            for (int i = 0; i < actualArgTypes.length; i++) {
                // Match only primitive types.
                if (!actualArgTypes[i].equals(expectedArgTypes[i])) {
                    matching = false;
                    break;
                }
            }
        }
        return matching;
    }

}
